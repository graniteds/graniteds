/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of Granite Data Services.
 *
 *   Granite Data Services is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU Library General Public License as published by
 *   the Free Software Foundation; either version 2 of the License, or (at your
 *   option) any later version.
 *
 *   Granite Data Services is distributed in the hope that it will be useful, but
 *   WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *   FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
 *   for more details.
 *
 *   You should have received a copy of the GNU Library General Public License
 *   along with this library; if not, see <http://www.gnu.org/licenses/>.
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
