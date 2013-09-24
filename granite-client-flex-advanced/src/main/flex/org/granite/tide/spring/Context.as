/*
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
package org.granite.tide.spring {

    import flash.utils.flash_proxy;

    import mx.collections.ArrayCollection;
    import mx.collections.IList;
    import mx.events.CollectionEvent;
    import mx.events.CollectionEventKind;
    import mx.events.PropertyChangeEvent;
    import mx.logging.ILogger;
    import mx.logging.Log;
    import mx.messaging.messages.ErrorMessage;
    import mx.rpc.AbstractOperation;
    import mx.rpc.AsyncToken;
    import mx.rpc.events.ResultEvent;
    import mx.utils.object_proxy;

    import org.granite.meta;
    import org.granite.tide.Tide;
    import org.granite.tide.BaseContext;
    import org.granite.tide.IComponent;
    import org.granite.tide.Component;
    import org.granite.tide.IEntity;
    import org.granite.tide.IExpression;
    import org.granite.tide.IInvocationCall;
    import org.granite.tide.IInvocationResult;
    import org.granite.tide.IPropertyHolder;
    import org.granite.tide.impl.ComponentProperty;
    import org.granite.tide.service.ServerSession;
    import org.granite.tide.invocation.InvocationCall;
    import org.granite.tide.invocation.InvocationResult;
    import org.granite.tide.invocation.ContextUpdate;

    use namespace flash_proxy;
    use namespace object_proxy;
    use namespace meta;


    [Bindable]
	/**
	 * 	Implementation of the Tide context for Spring services
	 * 
     * 	@author William DRAI
     */
    public dynamic class Context extends BaseContext {
        
        private static var log:ILogger = Log.getLogger("org.granite.tide.spring.Context");

        private var _updates:ArrayCollection = new ArrayCollection();
        private var _pendingUpdates:ArrayCollection = new ArrayCollection();
        

		public function Context(tide:Spring, parentContext:BaseContext = null) {
			super(tide, parentContext);
		}

        
		/**
		 * 	@private
		 * 	@return current list of updates that will be sent to the server
		 */
        public function get meta_updates():IList {
            return _updates;
        }
        
        /**
		 * 	@private
         *	Reinitialize the context
         * 
         *  @param force force complete destruction of context
         */
        public override function meta_clear(force:Boolean = false):void {
            _updates.removeAll();
            _pendingUpdates.removeAll();
            
            super.meta_clear(force);
            
            _updates.removeAll();
            _pendingUpdates.removeAll();
        }
        
        
        /**
		 * 	@private
         *  Add update to current context 
         *  Note: always move the update in last position in case the value can depend on previous updates
         * 
         *  @param componentName name of the component/context variable
         *  @param expr EL expression to evaluate
         *  @param value value to send to server
		 *  @param typed component name represents a typed component instance  
         */
        public override function meta_addUpdate(componentName:String, expr:String, value:*, typed:Boolean = false):void {
            if (!meta_tracking)
                return;

            if (_tide.getComponentRemoteSync(componentName) != Tide.SYNC_BIDIRECTIONAL)
                return;
            
            var val:Object = value;
            if (val is IPropertyHolder)
                val = IPropertyHolder(val).object;

            var found:Boolean = false;
            for (var i:int = 0; i < _updates.length; i++) {
                var u:ContextUpdate = _updates.getItemAt(i) as ContextUpdate;
                if (u.componentName == componentName && u.expression == expr) {
                    u.value = val;
                    if (i < _updates.length-1) {
                        found = false;
                        _updates.removeItemAt(i);    // Remove here to add it in last position
                        i--;
                    }
                    else
                        found = true;
                }
                else if (u.componentName == componentName && u.expression != null && (expr == null || u.expression.indexOf(expr) == 0)) {
                    _updates.removeItemAt(i);
                    i--;
                }
                else if (u.componentName == componentName && expr != null && (u.expression == null || expr.indexOf(u.expression) == 0))
                    found = true;
            }

            if (!found) {
                log.debug("add new update {0}.{1}", componentName, expr);
                _updates.addItem(new ContextUpdate(componentName, expr, val, _tide.getComponentScope(componentName)));
            }
        }

        
        /**
         * 	@private
         * 
         * 	Reset current call context and returns saved context
         * 
         *  @return saved call context
         */
        public override function meta_saveAndResetCallContext():Object {
        	var savedCallContext:Object = { 
        		updates: new ArrayCollection(_updates.toArray()), 
        		pendingUpdates: new ArrayCollection(_pendingUpdates.toArray())
        	};
        	
            _updates.removeAll();
            _pendingUpdates.removeAll();
            
            return savedCallContext;
        }
        
        /**
         *	@private
         * 
         * 	Restore call context
         * 
         *  @param callContext object containing the current call context
         */ 
        public override function meta_restoreCallContext(callContext:Object):void {
        	_updates = callContext.updates as ArrayCollection;
        	_pendingUpdates = callContext.pendingUpdates as ArrayCollection;
		}         	

        
        /**
		 * 	@private
         *  Calls a remote component
         * 	
         *  @param component the target component
         *  @op name of the called metho
         *  @arg method parameters
         * 
         *  @return the operation token
         */
        public override function meta_callComponent(serverSession:ServerSession, component:IComponent, op:String, args:Array, withContext:Boolean = true):AsyncToken {
            log.debug("callComponent {0}.{1}", component.meta_name, op);
            
            var token:AsyncToken = super.meta_callComponent(serverSession, component, op, args, withContext);
			
            if (withContext) {
                _pendingUpdates = new ArrayCollection(_updates.toArray());
                _updates.removeAll();
            }
            
            return token;
        }
        
        
        public override function meta_prepareCall(serverSession:ServerSession, operation:AbstractOperation, withContext:Boolean = true):IInvocationCall {
		    var call:InvocationCall = null;
		    if (withContext)
		        call = new InvocationCall(null, _updates, null);
		    else
		        call = new InvocationCall();
		    return call;
        }
        

        public override function meta_result(componentName:String, operation:String, ires:IInvocationResult, result:Object, mergeWith:Object = null):void {
            meta_preResult();
            
            _pendingUpdates.removeAll();
            
            log.debug("result {0}", result);
            
        	for (var key:String in object['flash']) {
        		object['flash'][key] = null;
        		delete object['flash'][key];
        	}
            
            var mergeExternal:Boolean = true;
            if (ires != null) {
                var invocationResult:InvocationResult = InvocationResult(ires);
                mergeExternal = invocationResult.merge;
                
                meta_setTracking(false);
				
				if (invocationResult.updates)
					meta_handleUpdates(false, invocationResult.updates);
                
                var rmap:IList = invocationResult.results;
                if (rmap) {
                    for (var k:int = 0; k < rmap.length; k++) {
                        var r:ContextUpdate = rmap.getItemAt(k) as ContextUpdate;
                        var val:Object = r.value;
        
                        log.debug("update expression {0}: {1}", r, val);
        				
                        var compName:String = r.componentName;
                        
                        var obj:Object = meta_getInstanceNoProxy(compName);
                        var p:Array = r.expression != null ? r.expression.split(".") : [];
                        if (p.length > 1) {
                            for (var i:int = 0; i < p.length-1; i++)
                                obj = obj[p[i] as String];
                        }
                        else if (p.length == 0)
                            _tide.setComponentRemoteSync(compName, Tide.SYNC_BIDIRECTIONAL);
        
                        var previous:Object = null;
                        var propName:String = null;
                        if (p.length > 0) {
                            propName = p[p.length-1] as String
                                   
                            if (obj is IPropertyHolder)
                                previous = IPropertyHolder(obj).object[propName];
                            else if (obj != null)
                                previous = obj[propName];
                        }
                        else
                            previous = obj;
                        
                        // Don't merge with temporary properties
                        if (previous is ComponentProperty || previous is Component)
                            previous = null;
                        
                        val = meta_mergeExternal(val, previous);
    					
                        if (propName != null) {
                            if (obj is IPropertyHolder) {
                                var evt:ResultEvent = new ResultEvent(ResultEvent.RESULT, false, true, val);
                                IPropertyHolder(obj).meta_propertyResultHandler(propName, evt);
                            }
                            else if (obj != null)
                                obj[propName] = val;
                        }
                        else
                            this[r.componentName] = val;
                    }
                }
            }
            
            // Merges final result object
            if (result) {
	            if (mergeExternal)
	            	result = meta_mergeExternal(result, mergeWith);
	            else
	            	log.debug("skipped merge of remote result");
	            if (ires != null)
	                InvocationResult(ires).result = result;
	        }
            
			meta_setTracking(true);
            
        	// Dispatch received data update events
            if (ires != null && InvocationResult(ires).updates)
            	meta_handleUpdateEvents(InvocationResult(ires).updates);
            
            super.meta_result(componentName, operation, ires, result, mergeWith);
            
            log.debug("result merged into local context");
        }
		
        /**
		 * 	@private
         *  Manages a remote call fault
         * 
         *  @param componentName name of the target component
         *  @param operation name of the called operation
         *  @param emsg error message
         */
        public override function meta_fault(componentName:String, operation:String, emsg:ErrorMessage):void {
            _pendingUpdates.removeAll();

            super.meta_fault(componentName, operation, emsg);
        }
        
        
        /**
		 * 	@private
         *  Implements managed entity functionality
         *  All entities delegate setters to this context method
         * 
         *  @param entity managed entity
         *  @param propName property name
         *  @param oldValue previous value
         *  @param newValue new value
         */ 
        public override function meta_setEntityProperty(entity:IEntity, propName:String, oldValue:*, newValue:*):void {
            super.meta_setEntityProperty(entity, propName, oldValue, newValue);

            meta_addUpdates(entity);
        }
            
        /**
		 * 	@private
         *  Handles updates on managed collections
         * 
         *  @param event collection event
         */ 
        public override function meta_collectionChangeHandler(event:CollectionEvent):void {
            super.meta_collectionChangeHandler(event);

            if (event.kind == CollectionEventKind.ADD || event.kind == CollectionEventKind.REMOVE)
                meta_addUpdates(event.target);
        }
            
        /**
		 * 	@private
         *  Handles updates on managed collections
         * 
         *  @param event collection event
         */ 
        public override function meta_entityCollectionChangeHandler(event:CollectionEvent):void {
            super.meta_entityCollectionChangeHandler(event);

            if (event.items && event.items.length > 0 && event.items[0] is IEntity)
                meta_addUpdates(event.target);
            else if (event.kind == CollectionEventKind.UPDATE && event.items && event.items.length > 0) {
                var pce:PropertyChangeEvent = event.items[0];
                if (pce.source is IEntity)
                    meta_addUpdates(event.target);
            }
        }
            
        /**
		 * 	@private
         *  Handles updates on managed maps
         * 
         *  @param event collection event
         */ 
        public override function meta_mapChangeHandler(event:CollectionEvent):void {
            super.meta_mapChangeHandler(event);

            if (event.kind == CollectionEventKind.ADD || event.kind == CollectionEventKind.REMOVE)
                meta_addUpdates(event.target);
        }
            
        /**
		 * 	@private
         *  Handles updates on managed collections
         * 
         *  @param event collection event
         */ 
        public override function meta_entityMapChangeHandler(event:CollectionEvent):void {
            super.meta_entityMapChangeHandler(event);

            if (event.items && event.items.length > 0 && event.items[0] is Array && event.items[0][1] is IEntity)
                meta_addUpdates(event.target);
            else if (event.kind == CollectionEventKind.UPDATE && event.items && event.items.length > 0) {
                var pce:PropertyChangeEvent = event.items[0];
                if (pce.source is IEntity)
                    meta_addUpdates(event.target);
            }
        }


        /**
		 * 	@private
         *  Add updates to referencing objects of provided object
         *
         *  @param entity object
         */ 
        private function meta_addUpdates(entity:Object):void {
            if (!meta_tracking || meta_finished)
                return;
            
            var ref:IExpression = meta_getReference(entity);
            if (ref)
                meta_addUpdate(ref.componentName, ref.expression, meta_evaluate(ref));
        }
    }
}
