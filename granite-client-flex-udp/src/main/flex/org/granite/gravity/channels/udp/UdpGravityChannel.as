/*
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2015 GRANITE DATA SERVICES S.A.S.
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

    import flash.net.DatagramSocket;
    
    import mx.logging.ILogger;
    import mx.logging.Log;
    import mx.messaging.MessageResponder;
    import mx.messaging.messages.IMessage;
    import mx.utils.URLUtil;
    
    import org.granite.gravity.channels.GravityChannel;
    import org.granite.gravity.channels.GravityStreamTunnel;

	
    /**
     * Channel implementation for UDP based communication. Note that UDP is only used for data
	 * pushed from the server: all other operations (ping, authentication, subscriptions, etc.)
	 * are performed through a standard (unencrypted) HTTP channel. A <code>UdpSecureGravityChannel</code>
	 * is also provided in order to perform these operations through an HTTPS (encrypted) channel. 
     *  
     * @author Franck WOLFF
	 * 
	 * @see UdpSecureGravityChannel
     */
    public class UdpGravityChannel extends GravityChannel {
        
        private static var log:ILogger = Log.getLogger("org.granite.enterprise.gravity.channels.UdpGravityChannel");
		
		public static const UDP_CONNECT:String = "udpChannelConnect";
		public static const UDP_DISCONNECT:String = "udpChannelDisconnect";

		private var _defaultLocalPort:int = 0;
		private var _defaultLocalAddress:String = "0.0.0.0"; // For IPv6, use "::"

        public function UdpGravityChannel(id:String, uri:String) {
            super(id, uri);
        }
		
		public function get defaultLocalPort():int {
			return _defaultLocalPort;
		}
		
		public function set defaultLocalPort(value:int):void {
			if (value < 0)
				value = 0;
			_defaultLocalPort = value;
		}
		
		public function get defaultLocalAddress():String {
			return _defaultLocalAddress;
		}
		
		public function set defaultLocalAddress(value:String):void {
			_defaultLocalAddress = value;
		}
		
		public function get localPort():int {
			var socket:DatagramSocket = (tunnel as UdpGravityStreamTunnel).datagramSocket; 
			return (socket != null && socket.bound ? socket.localPort : -1);
		}
		
		public function get localAddress():String {
			var socket:DatagramSocket = (tunnel as UdpGravityStreamTunnel).datagramSocket; 
			return (socket != null && socket.bound ? socket.localAddress : null);
		}
		
		public function get remotePort():int {
			var socket:DatagramSocket = (tunnel as UdpGravityStreamTunnel).datagramSocket; 
			return (socket != null && socket.connected ? socket.remotePort : -1);
		}
		
		public function get remoteAddress():String {
			var socket:DatagramSocket = (tunnel as UdpGravityStreamTunnel).datagramSocket; 
			return (socket != null && socket.connected ? socket.remoteAddress : null);
		}
        
        override protected function newTunnel():GravityStreamTunnel {
        	return new UdpGravityStreamTunnel(this);
        }
		
		internal function sendFromCommand(messageResponder:MessageResponder):void {
			internalSend(messageResponder);
		}
		
		internal function callResponder(responder:MessageResponder, response:IMessage):void {
			internalCallResponder(responder, response);
		}
		
		internal function get serverIp():String {
			return URLUtil.getServerName(uri);
		}
    }
}
