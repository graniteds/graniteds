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
package org.granite.tide {

    import flash.events.Event;
    import flash.events.IEventDispatcher;
    import flash.utils.Dictionary;
    import flash.utils.flash_proxy;
    
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
    import org.granite.tide.collections.PersistentCollection;
    import org.granite.tide.events.TideFaultEvent;
    import org.granite.tide.events.TideResultEvent;


    use namespace flash_proxy;
    use namespace object_proxy;
    use namespace meta;


    [Bindable]
    /**
     *	Default Context implementation when using Tide standalone
     */ 
    public dynamic class Context extends BaseContext {
        
        private static var log:ILogger = Log.getLogger("org.granite.tide.Context");


        public function Context(tide:Tide, parentContext:BaseContext = null) {
            super(tide, parentContext);
        }
        
        
        /**
         *	@private
         */
        public override function meta_result(componentName:String, operation:String, ires:IInvocationResult, result:Object, mergeWith:Object = null):void {
            meta_preResult();
            
            log.debug("result {0}", result);
            
            result = meta_mergeExternal(result, mergeWith);
            
            super.meta_result(componentName, operation, ires, result, mergeWith);
            
            log.debug("result merged into local context");
        }
    }
}
