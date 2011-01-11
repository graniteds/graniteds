/*
  GRANITE DATA SERVICES
  Copyright (C) 2011 GRANITE DATA SERVICES S.A.S.

  This file is part of Granite Data Services.

  Granite Data Services is free software; you can redistribute it and/or modify
  it under the terms of the GNU Library General Public License as published by
  the Free Software Foundation; either version 2 of the License, or (at your
  option) any later version.

  Granite Data Services is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
  for more details.

  You should have received a copy of the GNU Library General Public License
  along with this library; if not, see <http://www.gnu.org/licenses/>.
*/

package org.granite.tide.data {

    import flash.utils.flash_proxy;
    
    import mx.logging.ILogger;
    import mx.logging.Log;
    import mx.collections.IList;
    import mx.collections.errors.ItemPendingError;
    import mx.messaging.messages.ErrorMessage;
    import mx.rpc.events.ResultEvent;
    import mx.utils.ObjectProxy;
    import mx.utils.ObjectUtil;
    import mx.utils.object_proxy;
    import mx.controls.Alert;
    
    import org.granite.collections.IPersistentCollection;
    import org.granite.meta;
    import org.granite.tide.BaseContext;
    import org.granite.tide.IComponent;
    import org.granite.tide.IEntity;
    import org.granite.tide.IEntityManager;
    import org.granite.tide.IExceptionHandler;
    import org.granite.tide.IPropertyHolder;

    use namespace flash_proxy;
    use namespace object_proxy;
    

    /**
     * @author William DRAI
     */
    public class OptimisticLockExceptionHandler implements IExceptionHandler {
        
        private static var log:ILogger = Log.getLogger("org.granite.tide.data.OptimisticLockExceptionHandler");
        
        public static const OPTIMISTIC_LOCK:String = "Persistence.OptimisticLock"; 
        
        
        public function accepts(emsg:ErrorMessage):Boolean {
            return emsg.faultCode == OPTIMISTIC_LOCK;
        }

        public function handle(context:BaseContext, emsg:ErrorMessage):void {
            log.debug("optimistic lock error received {0}", emsg.toString());
            
            // Save the call context because data has not been requested by the current user 
            var savedCallContext:Object = context.meta_saveAndResetCallContext();
            
            var receivedSessionId:String = context.meta_tide.sessionId + "_error";
            var entity:Object = emsg.extendedData ? emsg.extendedData.entity as IEntity : null;
            // Received entity should be the correct version from the database
            if (entity)
                context.meta_mergeExternalData(entity, null, receivedSessionId);
	      	
	      	context.meta_restoreCallContext(savedCallContext);
        }
    }
}
