/*
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2015 GRANITE DATA SERVICES S.A.S.
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
package org.granite.tide.seam.validators {
    
    import mx.logging.Log;
    import mx.logging.ILogger;
    
    import mx.events.PropertyChangeEvent;
    import mx.collections.ArrayCollection;

    import mx.validators.Validator;
    import mx.validators.ValidationResult;

    import org.granite.tide.IEntity;
    import org.granite.tide.IEntityManager;
    import org.granite.tide.TideMessage;
    import org.granite.tide.seam.framework.StatusMessages;
    


    /**
     * 	Validator component that listens to StatusMessageEvent coming from server validation 
     *  and dispatches them as client validator events
     * 
     * 	@author William DRAI
     */
    public class TideControlValidator extends Validator {
        
        private static var log:ILogger = Log.getLogger("org.granite.tide.seam.validators.TideControlValidator");
        
        private var statusMessages:StatusMessages = StatusMessages.instance();
        
        private var lastServerValue:Object = null;
        
        
        public function TideControlValidator():void {
        	statusMessages.controlMessages.addEventListener(PropertyChangeEvent.PROPERTY_CHANGE, propertyChangeHandler);
        }
        
        
        private function propertyChangeHandler(event:PropertyChangeEvent):void {
        	if (event.property == source.id) {
        		lastServerValue = source[property];
        		validate(event.newValue);
        	}
       	}
		
		
        override protected function doValidation(value:Object):Array {
			if (value is String)
				return super.doValidation(value);
			
			var results:Array = [];
            
            var fromServer:Boolean = false;
            var messages:ArrayCollection = null;
            if (value is ArrayCollection) {
            	messages = value as ArrayCollection;
            	fromServer = true;
            }
            else if (statusMessages && statusMessages.controlMessages && source)
            	messages = statusMessages.controlMessages[source.id] as ArrayCollection;
            
            var vr:ValidationResult = null;
            if (messages && (fromServer || value == lastServerValue)) {
	            for each (var msg:TideMessage in messages) {
	            	vr = new ValidationResult(true, source.id, "controlMessage", msg.summary);
	            	results.push(vr);
	            }
	        }

            return results;
        }
    }
}