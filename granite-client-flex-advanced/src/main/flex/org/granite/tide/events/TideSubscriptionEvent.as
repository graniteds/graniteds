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
     *	Event that is dispatched when a component is subscribed or unsubscribed to its attached topic 
     *  
     * 	@author William DRAI
     */
    public class TideSubscriptionEvent extends Event {
    	
    	public static const TOPIC_SUBSCRIBED:String = "topicSubscribed";
		public static const TOPIC_SUBSCRIBED_FAULT:String = "topicSubscribedFault";
    	public static const TOPIC_UNSUBSCRIBED:String = "topicUnsubscribed";
		public static const TOPIC_UNSUBSCRIBED_FAULT:String = "topicUnsubscribedFault";
        

        public function TideSubscriptionEvent(type:String):void {
            super(type, false, false);
        }
    }
}