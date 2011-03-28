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
    
    import mx.rpc.AsyncToken;
    import mx.rpc.Fault;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.rpc.ComponentResponder;
    

    /**
     * 	Event that is provided to Tide fault handlers and that holds the fault object.
     * 
     * 	@author William DRAI
     */
    public class TideFaultEvent extends TideEvent {
        
        public static const FAULT:String = "fault";

        public var asyncToken:AsyncToken;
		public var componentResponder:ComponentResponder;
        public var fault:Fault;
		public var extendedData:Object;

        public function TideFaultEvent(type:String,
                                      context:BaseContext,
                                      bubbles:Boolean = false,
                                      cancelable:Boolean = false,
                                      asyncToken:AsyncToken = null,
									  componentResponder:ComponentResponder = null,
                                      fault:Fault = null,
									  extendedData:Object = null) {
            super(type, context, bubbles, cancelable);
            this.asyncToken = asyncToken;
			this.componentResponder = componentResponder;
            this.fault = fault;
			this.extendedData = extendedData;
        }

        override public function clone():Event {
            return new TideFaultEvent(type, context, bubbles, cancelable, asyncToken, componentResponder, fault, extendedData);
        }
		
		public function retry():AsyncToken {
			return context.meta_tide.reinvokeComponent(componentResponder);
		}
    }
}
