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
	
	import mx.messaging.messages.ErrorMessage;
	
    /**
     * @author Franck WOLFF
     */
	public class UdpChannelFaultEvent extends UdpChannelEvent {
		
    	public static const FAULT:String = "channelFault";    
		
	    public var faultCode:String;
	    public var faultDetail:String;
	    public var faultString:String;
    	public var rootCause:Object;

		public function UdpChannelFaultEvent(
			type:String,
			bubbles:Boolean = false,
			cancelable:Boolean = false,
			channel:UdpGravityChannel = null,
			code:String = null,
			level:String = null,
			description:String = null) {
			
			super(type, bubbles, cancelable, channel);

			faultCode = code;
        	faultString = level;
        	faultDetail = description;
		}

		public static function createEvent(
			channel:UdpGravityChannel,
			code:String = null,
			level:String = null,
			description:String = null):UdpChannelFaultEvent {
	        
			return new UdpChannelFaultEvent(UdpChannelFaultEvent.FAULT, false, false, channel, code, level, description);
	    }

		override public function clone():Event {
	        var faultEvent:UdpChannelFaultEvent = new UdpChannelFaultEvent(
				type, bubbles, cancelable, channel, faultCode, faultString, faultDetail
			);
	        faultEvent.rootCause = rootCause;
	        return faultEvent;
	    }

		override public function toString():String {
	        return formatToString(
				"UdpChannelFaultEvent", "faultCode", "faultString", "faultDetail",
	            "channelId", "type", "bubbles", "cancelable", "eventPhase"
			);
	    }

		public function createErrorMessage():ErrorMessage {
	        var result:ErrorMessage = new ErrorMessage();
	        result.faultCode = faultCode;
	        result.faultString = faultString;
	        result.faultDetail = faultDetail;
	        result.rootCause = rootCause;
	        return result;
	    }
	}
}