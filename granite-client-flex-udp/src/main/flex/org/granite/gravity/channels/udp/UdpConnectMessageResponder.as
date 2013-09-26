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

    import mx.messaging.messages.IMessage;
    
    import org.granite.gravity.channels.StreamMessageResponder;

	[ExcludeClass]
    /**
     * @author Franck WOLFF
     */
    public class UdpConnectMessageResponder extends StreamMessageResponder {

        private var _udpStream:UdpGravityStreamTunnel = null;

        public function UdpConnectMessageResponder(message:IMessage, stream:UdpGravityStreamTunnel):void {
            super(message, stream);
			_udpStream = stream;
        }

        override public function internalResult(ack:IMessage):void {
			_udpStream.internalConnectResult(message, ack);
        }

		override public function internalStatus(err:IMessage):void {
			_udpStream.internalConnectStatus(message, err);
        }
    }
}
