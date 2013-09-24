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
package org.granite.gravity.channels.udp {

	import flash.events.Event;
	
    /**
     * @author Franck WOLFF
     */
	public class UdpChannelEvent extends Event {
		
    	public static const CONNECT:String = "udpChannelConnect";

		public static const DISCONNECT:String = "udpChannelDisconnect";
		
    	public var channel:UdpGravityChannel;
		
		public function UdpChannelEvent(type:String, bubbles:Boolean = false, cancelable:Boolean = false, channel:UdpGravityChannel = null) {
			super(type, bubbles, cancelable);
			
			this.channel = channel;
		}

		public static function createEvent(type:String, channel:UdpGravityChannel = null):UdpChannelEvent {
	        return new UdpChannelEvent(type, false, false, channel);
	    }

		/**
	     * @private
	     */
	    public function get channelId():String {
	        if (channel != null)
	            return channel.id;
	        return null;
	    }

		override public function clone():Event {
	        return new UdpChannelEvent(type, bubbles, cancelable, channel);
	    }

		override public function toString():String {
	        return formatToString("UdpChannelEvent", "channelId", "type", "bubbles", "cancelable", "eventPhase");
	    }
	}
}