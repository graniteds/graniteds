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
package org.granite.tide.events {

    import flash.events.Event;


    /**
     *	Standard Flex event that can be dispatched from UI components and is 
     * 	intercepted by the context in which the UI component is registered 
     *  
     * 	@author William DRAI
     */
    public class TideUIEvent extends Event {
        
        public static const TIDE_EVENT:String = "org.granite.tide.events.TideUIEvent";
        
        /**
         * 	Type of the event
         */
        public var eventType:String;
        /**
         *  Array of event parameters
         */
        public var params:Array;
        

        public function TideUIEvent(eventType:String, ...params:Array) {
            super(TIDE_EVENT, true, false);
            this.eventType = eventType;
            this.params = params;
        }

        override public function clone():Event {
            return new TideUIEvent(eventType, params, bubbles, cancelable);
        }
    }
}