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
     *	Base class for Tide events
     *  
     * 	@author William DRAI
     */
    public class TideEvent extends Event {
        
        /**
         *	The context in which the event is dispatched
         */ 
        public var context:BaseContext;


        public function TideEvent(type:String,
                                      context:BaseContext,
                                      bubbles:Boolean = false,
                                      cancelable:Boolean = false) {
            super(type, bubbles, cancelable);
            this.context = context;
        }

        override public function clone():Event {
            return new TideEvent(type, context, bubbles, cancelable);
        }
    }
}