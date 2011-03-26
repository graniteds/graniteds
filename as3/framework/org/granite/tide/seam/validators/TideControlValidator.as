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
				return super.doValidation();
			
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