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