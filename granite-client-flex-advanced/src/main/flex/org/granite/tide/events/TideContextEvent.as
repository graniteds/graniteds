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
     * 	Generic context event. All events dispatched internally by Tide on the context are TideContextEvents 
     *  and all observers can listen to this type :
     * 
     *  <pre>
     *  [Observer("myEvent")]
     *  public function eventHandler(event:TideContextEvent):void {
     *     ...
     *  }
     *  </pre>
     * 
     * 	@author William DRAI
     */
    public class TideContextEvent extends TideEvent {
        
        public var params:Array;
        

        public function TideContextEvent(type:String,
                                      context:BaseContext,
                                      params:Array,
                                      bubbles:Boolean = false,
                                      cancelable:Boolean = false) {
            super(type, context, bubbles, cancelable);
            this.params = params;
        }

        override public function clone():Event {
            return new TideContextEvent(type, context, params, bubbles, cancelable);
        }
    }
}