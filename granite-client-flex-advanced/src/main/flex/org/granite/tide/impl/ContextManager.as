/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of the Granite Data Services Platform.
 *
 *                               ***
 *
 *   Community License: GPL 3.0
 *
 *   This file is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published
 *   by the Free Software Foundation, either version 3 of the License,
 *   or (at your option) any later version.
 *
 *   This file is distributed in the hope that it will be useful, but
 *   WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *                               ***
 *
 *   Available Commercial License: GraniteDS SLA 1.0
 *
 *   This is the appropriate option if you are creating proprietary
 *   applications and you are not prepared to distribute and share the
 *   source code of your application under the GPL v3 license.
 *
 *   Please visit http://www.granitedataservices.com/license for more
 *   details.
 */

package org.granite.tide.impl { 
	
	import flash.display.DisplayObject;
	import flash.display.DisplayObjectContainer;
	import flash.display.LoaderInfo;
	import flash.errors.IllegalOperationError;
	import flash.events.Event;
	import flash.events.EventDispatcher;
	import flash.events.TimerEvent;
	import flash.net.LocalConnection;
	import flash.utils.Dictionary;
	import flash.utils.IExternalizable;
	import flash.utils.Proxy;
	import flash.utils.Timer;
	import flash.utils.flash_proxy;
	import flash.utils.getDefinitionByName;
	import flash.utils.getQualifiedClassName;
	
	import mx.binding.BindabilityInfo;
	import mx.binding.utils.BindingUtils;
	import mx.collections.ArrayCollection;
	import mx.collections.IList;
	import mx.collections.Sort;
	import mx.controls.Alert;
	import mx.core.Application;
	import mx.core.IUIComponent;
	import mx.core.mx_internal;
	import mx.events.PropertyChangeEvent;
	import mx.logging.ILogger;
	import mx.logging.Log;
	import mx.managers.SystemManager;
	import mx.messaging.config.ServerConfig;
	import mx.messaging.events.ChannelFaultEvent;
	import mx.messaging.events.MessageEvent;
	import mx.messaging.messages.AsyncMessage;
	import mx.messaging.messages.ErrorMessage;
	import mx.messaging.messages.IMessage;
	import mx.rpc.AbstractOperation;
	import mx.rpc.AsyncToken;
	import mx.rpc.Fault;
	import mx.rpc.events.FaultEvent;
	import mx.rpc.events.InvokeEvent;
	import mx.rpc.events.ResultEvent;
	import mx.utils.ObjectProxy;
	import mx.utils.ObjectUtil;
	import mx.utils.StringUtil;
	
	import org.granite.tide.BaseContext;
	import org.granite.tide.IComponent;
	import org.granite.tide.IContextManager;
	import org.granite.tide.Tide;
    

	[Bindable]
    /**
	 * 	Tide is the base implementation of the Tide application manager singleton
	 *
     * 	@author William DRAI
     */
	public class ContextManager extends EventDispatcher implements IContextManager {
        
        private static var log:ILogger = Log.getLogger("org.granite.tide.impl.ContextManager");
        
	    private var _tide:Tide;
	    
		private var _contextClass:Class = BaseContext;
		
		private var _currentContextId:uint = 1;
		
		private var _ctx:Dictionary = new Dictionary();
		private var _contextsToDestroy:Array = new Array();
		
		
		public function ContextManager(tide:Tide, contextClass:Class) {
			_tide = tide;
	        _contextClass = contextClass;
	        initDefaultContext();
		}
		
		
		/**
		 *  @private
		 * 	Init default context
		 */
		protected function initDefaultContext():BaseContext {
			_ctx = new Dictionary();
            var ctx:BaseContext = new _contextClass(_tide, null);
	        ctx.meta_init(null, this);
            
            _ctx[Tide.DEFAULT_CONTEXT] = ctx;
            return ctx;
		}
		
		/**
		 *	Return a context from its id
		 *  
		 * 	@param contextId context id
		 *  @param create should create when not existing
		 *  @return context
		 */ 
		public function getContext(contextId:String = null, parentContextId:String = null, create:Boolean = true):BaseContext {
		    var ctx:BaseContext = _ctx[contextId != null ? contextId : Tide.DEFAULT_CONTEXT];
		    if (ctx == null && create) {
		    	var parentCtx:BaseContext = parentContextId == null ? getContext() : BaseContext(_ctx[parentContextId]);
		        ctx = new _contextClass(_tide, parentCtx);
		        ctx.meta_init(contextId, this);
		        _ctx[contextId != null ? contextId : Tide.DEFAULT_CONTEXT] = ctx;
		        ctx.raiseEvent(Tide.CONTEXT_CREATE);
		    }
		    return ctx;
		}
		
		/**
		 *  @private
		 *  Create a new context if it does not exist
		 * 
		 *  @param contextId the requested context id
		 *  @return the context
		 */
		public function newContext(contextId:String = null, parentContextId:String = null):BaseContext {
		    var ctx:BaseContext = contextId != null ? _ctx[contextId] as BaseContext : null;
		    if (ctx != null && ctx.meta_finished) {
    		    ctx.meta_clear();
    	        delete _ctx[contextId];
            	removeFromContextsToDestroy(contextId);
		    	ctx = null;
		    }
		    if (ctx == null) {
				var parentCtx:BaseContext = parentContextId != null ? _ctx[parentContextId] as BaseContext : getContext();
		        ctx = new _contextClass(_tide, parentCtx);
		        ctx.meta_init(contextId, this);
		        if (contextId != null)
		            _ctx[contextId] = ctx;
		        ctx.raiseEvent(Tide.CONTEXT_CREATE);
		    }
		    return ctx;
		}
		
		/**
		 * 	@private
		 * 	Destroy a context
		 * 
		 *  @param contextId context id
		 *  @param force force complete destruction of context
		 */
		public function destroyContext(contextId:String, force:Boolean = false):void {
		    var ctx:BaseContext = contextId != null ? _ctx[contextId] : null;
		    if (ctx != null) {
	        	// Destroy child contexts
				var contextIdsToDestroy:Array = [];
				for each (var c:BaseContext in _ctx) {
					if (c.meta_parentContext === ctx)
						contextIdsToDestroy.push(c.contextId);
				}
				for each (var cid:String in contextIdsToDestroy)
					destroyContext(cid, force);
        		
		    	removeFromContextsToDestroy(contextId);
		        ctx.raiseEvent(Tide.CONTEXT_DESTROY);
    		    _ctx[contextId].meta_clear(force);
    	        delete _ctx[contextId];
    	    }
		}		
		
		/**
		 * 	Returns the list of conversation contexts
		 * 
		 *  @return conversation contexts
		 */
		public function getAllContexts():Array {
			var contexts:Array = new Array();
			for each (var ctx:BaseContext in _ctx) {
				if (!ctx.meta_isGlobal())
					contexts.push(ctx);
			}
			return contexts;
		}		
		
		/**
		 * 	Execute a function for each conversation context
		 * 
		 *  @param parentContext parent context
		 *  @param callback callback function
		 *  @param token token passed to the function
		 */
		public function forEachChildContext(parentContext:BaseContext, callback:Function, token:Object = null):void {
			for each (var ctx:BaseContext in _ctx) {
				if (ctx.meta_parentContext == parentContext) {
					if (token)
						callback(ctx, token);
					else
						callback(ctx);
				}
			}
		}		
		
		/**
		 * 	@private
		 * 	Destroy all contexts
         * 
         *  @param force force complete destruction of contexts (all event listeners...), used for testing
		 */
		public function destroyContexts(force:Boolean = false):void {
            _contextsToDestroy = new Array();
            
            var globalCtx:BaseContext = BaseContext(_ctx[Tide.DEFAULT_CONTEXT]);
			var contextIdsToDestroy:Array = [];
			for each (var ctx:BaseContext in _ctx) {
			    if (ctx.contextId != Tide.DEFAULT_CONTEXT && ctx.meta_parentContext === globalCtx)
					contextIdsToDestroy.push(ctx.contextId);
			}
			for each (var contextId:String in contextIdsToDestroy)
				destroyContext(ctx.contextId, force);
			
			globalCtx.meta_clear(force);
		}
		
		/**
		 * 	@private
		 * 	Destroy finished contexts and reset current pending contexts
		 */
		public function destroyFinishedContexts():void {
		    for each (var contextId:String in _contextsToDestroy)
		        destroyContext(contextId);
		    _contextsToDestroy = new Array();
		}
	    
	    
		/**
		 * 	@private
		 * 	Remove context from the list of contexts to destroy
		 * 	
		 * 	@param contextId context id
		 */
	    public function removeFromContextsToDestroy(contextId:String):void {
	        if (_contextsToDestroy == null)
	            return;
	        
	        var idx:int = _contextsToDestroy.indexOf(contextId);
	        if (idx >= 0)
	            _contextsToDestroy.splice(idx, 1);
	    }
	    
		/**
		 * 	@private
		 * 	Add context to the list of contexts to destroy
		 * 	
		 * 	@param contextId context id
		 */
	    public function addToContextsToDestroy(contextId:String):void {
	    	if (_contextsToDestroy.indexOf(contextId) >= 0)
	    		return;
            _contextsToDestroy.push(contextId);
	    }
		
		
		/**
		 * 	@private
		 *  
		 * 	Unregister listeners for component in all contexts
		 * 
		 *  @param name component name
		 *  @param component component
		 */
		public function unregisterComponent(name:String, component:Object):void {
			for each (var ctx:BaseContext in _ctx)
				ctx.meta_unregisterComponent(name, component);
		}
		
		/**
		 * 	@private
		 *  
		 * 	Destroys component instances in all contexts
		 * 
		 *  @param name component name
		 */
		public function destroyComponentInstances(name:String):void {
		    for each (var ctx:BaseContext in _ctx)
		        ctx.meta_destroy(name, true);
		}
		
		
		/**
		 * 	@private
		 * 
		 * 	Defines new context for existing id
		 * 
		 *  @param previousContextId existing id
		 *  @param context new context
		 */
        public function updateContextId(previousContextId:String, context:BaseContext):void {
            if (previousContextId != null)
            	delete _ctx[previousContextId];
            _ctx[context.contextId] = context;
        }
	}
}
