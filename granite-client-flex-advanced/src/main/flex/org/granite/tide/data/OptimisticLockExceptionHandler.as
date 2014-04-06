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
package org.granite.tide.data {

    import flash.utils.flash_proxy;
    
    import mx.logging.ILogger;
    import mx.logging.Log;
    import mx.messaging.messages.ErrorMessage;
    import mx.utils.object_proxy;

    import org.granite.tide.BaseContext;
    import org.granite.tide.IEntity;
    import org.granite.tide.IExceptionHandler;
    import org.granite.tide.service.ServerSession;

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

        public function handle(serverSession:ServerSession, context:BaseContext, emsg:ErrorMessage):void {
            log.debug("optimistic lock error received {0}", emsg.toString());
            
            // Save the call context because data has not been requested by the current user 
            var savedCallContext:Object = null;
            try {
                savedCallContext = context.meta_saveAndResetCallContext();

                var entity:Object = emsg.extendedData ? emsg.extendedData.entity as IEntity : null;
                // Received entity should be the correct version from the database
                if (entity)
                    context.meta_mergeExternalData(entity, null, true);
            }
	      	finally {
	      	    context.meta_restoreCallContext(savedCallContext);
            }
        }
    }
}
