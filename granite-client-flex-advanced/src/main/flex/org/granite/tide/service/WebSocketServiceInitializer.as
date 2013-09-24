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
package org.granite.tide.service {

	import flash.events.Event;
	import mx.messaging.Channel;

	import org.granite.gravity.websocket.WebSocketChannel;
	import org.granite.tide.Tide;
	import org.granite.tide.service.DefaultServiceInitializer;

	public class WebSocketServiceInitializer extends DefaultServiceInitializer {

		protected var _embedded:Boolean = false;

		public function WebSocketServiceInitializer(contextRoot:String = "", graniteUrlMapping:String = "/graniteamf/amf.txt", gravityUrlMapping:String = "/websocketamf/amf", secure:Boolean = false, embedded:Boolean = false) {
			super(contextRoot, graniteUrlMapping, gravityUrlMapping, secure);
			_embedded = embedded;

			Tide.getInstance().addEventListener("GDSSessionIdChanged", function(event:Event):void {
				for each (var channel:WebSocketChannel in gravityChannelSet.channels) {
					channel.sessionId = Tide.getInstance().sessionId;
				}
			});
		}

		protected override function newGravityChannel(id:String, uri:String):Channel {
			var scheme:String = _secure ? "wss" : "ws";
			if (_embedded)
				return new WebSocketChannel(id, scheme + "://" + _serverName + ":" + _serverPort);
			return new WebSocketChannel(id, scheme + "://" + _serverName + ":" + _serverPort + _contextRoot + _gravityUrlMapping);
		}
	}
}
