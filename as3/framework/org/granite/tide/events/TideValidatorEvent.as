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

package org.granite.tide.events {

    import flash.events.Event;
    
    import org.granite.tide.BaseContext;


    /**
     *	Event that is dispatched on the context by validation exception handlers and 
     *  that can be listened by a TideEntityValidator 
     *  
     * 	@author William DRAI
     */
    public class TideValidatorEvent extends TideEvent {
        
        public static const VALID:String = "valid";
        public static const INVALID:String = "invalid";
        
        
        [ArrayElementType("org.granite.tide.validators.IInvalidValue")]
        public var invalidValues:Array;
        
        public var entityProperty:String;
        

        public function TideValidatorEvent(type:String,
                                      context:BaseContext,
                                      entityProperty:String = null,
                                      bubbles:Boolean = false,
                                      cancelable:Boolean = false,
                                      invalidValues:Array = null) {
            super(type, context, bubbles, cancelable);
            this.entityProperty = entityProperty;
            this.invalidValues = invalidValues;
        }

        override public function clone():Event {
            return new TideValidatorEvent(type, context, entityProperty, bubbles, cancelable, invalidValues);
        }
    }
}
