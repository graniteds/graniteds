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
package org.granite.tide.udp {

    import mx.logging.ILogger;
    import mx.logging.Log;
    import mx.messaging.Channel;

    import org.granite.gravity.channels.udp.UdpGravityChannel;

    import org.granite.gravity.channels.udp.UdpGravityChannel;
    import org.granite.gravity.channels.udp.UdpSecureGravityChannel;

    import org.granite.tide.service.IChannelBuilder;
    import org.granite.tide.service.IServerApp;

    public class UdpChannelBuilder implements IChannelBuilder {

        private static var log:ILogger = Log.getLogger("org.granite.tide.udp.UdpChannelBuilder");

        public static const UDP:String = "udp";

        protected var _gravityUrlMapping:String = "/gravityamf/amf.txt";


        public function set gravityUrlMapping(gravityUrlMapping:String):void {
            _gravityUrlMapping = gravityUrlMapping;
        }

        public function build(type:String, server:IServerApp, options:Object = null):Channel {
            if (type != UDP)
                return null;

            var gravityUrlMapping:String = options && options.gravityUrlMapping ? options.gravityUrlMapping : _gravityUrlMapping;
            var uri:String = (server.secure ? "https" : "http") + "://" + server.serverName + ":" + server.serverPort + server.contextRoot + gravityUrlMapping;
            var channel:UdpGravityChannel = server.secure ? new UdpGravityChannel("udpgravityamf", uri) : new UdpSecureGravityChannel("udpgravityamf", uri);
            if (options) {
                if (options.udpLocalAddress)
                    channel.defaultLocalAddress = options.udpLocalAddress;
                if (options.udpLocalPort)
                    channel.defaultLocalPort = options.udpLocalPort;
            }
            return channel;
        }
    }
}
