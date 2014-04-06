/*
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2014 GRANITE DATA SERVICES S.A.S.
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
package org.granite.tide.ejb {

    import flash.events.Event;
    import flash.events.IEventDispatcher;
    import flash.utils.Dictionary;
    import flash.utils.flash_proxy;
    import flash.utils.getQualifiedClassName;
    
    import mx.collections.ArrayCollection;
    import mx.collections.ArrayList;
    import mx.collections.IList;
    import mx.collections.ItemResponder;
    import mx.collections.ListCollectionView;
    import mx.controls.Alert;
    import mx.core.IUID;
    import mx.core.UIComponent;
    import mx.events.CollectionEvent;
    import mx.events.CollectionEventKind;
    import mx.events.FlexEvent;
    import mx.events.PropertyChangeEvent;
    import mx.events.PropertyChangeEventKind;
    import mx.events.ValidationResultEvent;
    import mx.logging.ILogger;
    import mx.logging.Log;
    import mx.messaging.events.ChannelFaultEvent;
    import mx.messaging.messages.ErrorMessage;
    import mx.rpc.AbstractOperation;
    import mx.rpc.AsyncToken;
    import mx.rpc.events.FaultEvent;
    import mx.rpc.events.ResultEvent;
    import mx.utils.ObjectProxy;
    import mx.utils.ObjectUtil;
    import mx.utils.object_proxy;
    import mx.validators.ValidationResult;
    
    import org.granite.collections.IPersistentCollection;
    import org.granite.events.SecurityEvent;
    import org.granite.meta;
    import org.granite.tide.BaseContext;
    import org.granite.tide.IComponent;
    import org.granite.tide.IIdentity;
    import org.granite.tide.IEntity;
    import org.granite.tide.IEntityManager;
    import org.granite.tide.IExpression;
    import org.granite.tide.IInvocationCall;
    import org.granite.tide.IInvocationResult;
    import org.granite.tide.IPropertyHolder;
    import org.granite.tide.impl.ComponentProperty;
    import org.granite.tide.invocation.InvocationCall;
    import org.granite.tide.invocation.InvocationResult;
    import org.granite.tide.collections.PersistentCollection;
    import org.granite.tide.events.TideFaultEvent;
    import org.granite.tide.events.TideResultEvent;
    import org.granite.tide.invocation.ContextEvent;
    import org.granite.tide.invocation.ContextResult;
    import org.granite.tide.invocation.ContextUpdate;
    import org.granite.tide.Component;
    import org.granite.tide.service.ServerSession;

    use namespace flash_proxy;
    use namespace object_proxy;
    use namespace meta;

	
    [Bindable]
	/**
	 * 	Implementation of the Tide context for EJB3 services
	 * 
     * 	@author William DRAI
	 */
    public dynamic class Context extends BaseContext {
        
        private static var log:ILogger = Log.getLogger("org.granite.tide.ejb.Context");


		public function Context(tide:Ejb, parentContext:BaseContext = null) {
			super(tide, parentContext);
		}
        
        
        public override function meta_prepareCall(serverSession:ServerSession, operation:AbstractOperation, withContext:Boolean = true):IInvocationCall {
		    var call:IInvocationCall = null;
		    if (withContext) {
		        call = new InvocationCall(_tide.newListeners);
		        _tide.newListeners.removeAll();
		    }
		    else
		        call = new InvocationCall();
		    return call;
        }
        

        public override function meta_result(componentName:String, operation:String, ires:IInvocationResult, result:Object, mergeWith:Object = null):void {
            meta_preResult();
            
            log.debug("result {0}", result);

            meta_setTracking(false);
            
            var mergeExternal:Boolean = true;
            if (ires) {
            	var invocationResult:InvocationResult = InvocationResult(ires);
                mergeExternal = invocationResult.merge;
				
				if (invocationResult.updates)
					meta_handleUpdates(false, invocationResult.updates);
				
                var resultMap:IList = invocationResult.results;

                if (resultMap) {
                    for (var k:int = 0; k < resultMap.length; k++) {
                        var r:ContextUpdate = resultMap.getItemAt(k) as ContextUpdate;
                        var val:Object = r.value;
        
                        log.debug("update expression {0}: {1}", r, val);
        
                        var compName:String = r.componentName;

                        _tide.setComponentGlobal(compName, true);
                        
                        var previous:Object = meta_getInstanceNoProxy(compName);
                        
                        // Don't merge with temporary properties
                        if (previous is ComponentProperty || previous is Component)
                            previous = null;
                        
                        var res:ContextResult = new ContextResult(r.componentName);
        
                        val = meta_mergeExternal(val, previous, res);
    
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
	        }
            if (ires != null)
                InvocationResult(ires).result = result;

            meta_setTracking(true);
            
            if (ires) {
            	// Dispatch received data update events
            	if (InvocationResult(ires).updates)
            		meta_handleUpdateEvents(InvocationResult(ires).updates);
            	
                // Dispatch received context events
                var events:IList = invocationResult.events;
                if (events) {
	                for (var ie:uint = 0; ie < events.length; ie++) {
	                    var event:ContextEvent = events.getItemAt(ie) as ContextEvent;
	                    meta_internalRaiseEvent(event.eventType, event.params);
	                }
                }
            }

            super.meta_result(componentName, operation, ires, result, mergeWith);
            
            log.debug("result merged into local context");
        }
    }
}
