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


    /**
     *	Standard Flex event that can dispatched from UI components and is intercepted by the context 
     *  in which the UI component is registered 
     *  This event indicates that the event needs to be dispatched in a particular conversation context 
     *  and may trigger creation of a new context
     *  
     * 	@author William DRAI
     */
    public class TideUIConversationEvent extends TideUIEvent implements IConversationEvent {
        
        /**
         *	Requested conversationId for the event handling
         */
        private var _conversationId:String;
        
        public function get conversationId():String {
        	return _conversationId;
        }
        

        public function TideUIConversationEvent(conversationId:String, eventType:String, ...params:Array) {
            super(eventType, null);
            this.params = params;
            _conversationId = conversationId;
        }

        override public function clone():Event {
            return new TideUIConversationEvent(_conversationId, eventType, params);
        }
    }
}