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

    import mx.logging.ILogger;
    import mx.logging.Log;
    import mx.messaging.Channel;
    import mx.messaging.channels.AMFChannel;
    import mx.messaging.channels.SecureAMFChannel;
    
    import org.granite.gravity.channels.GravityChannel;
    import org.granite.gravity.channels.SecureGravityChannel;
    import org.granite.gravity.websocket.WebSocketChannel;

    /**
     * 	@author William DRAI
     */
    public class DefaultChannelBuilder implements IChannelBuilder {
        
        private static var log:ILogger = Log.getLogger("org.granite.tide.service.DefaultChannelBuilder");

        public static const DEFAULT:String = "default";
        public static const LONG_POLLING:String = "long-polling";
        public static const WEBSOCKET:String = "websocket";
        public static const WEBSOCKET_EMBEDDED:String = "websocket-embedded";

		protected var _graniteUrlMapping:String = "/graniteamf/amf.txt";		// .txt for stupid bug in IE8
		protected var _gravityUrlMapping:String = "/gravityamf/amf.txt";
        protected var _websocketUrlMapping:String = "/websocketamf/amf";


		/**
		 * 	Tide constructor used at component instantiation
		 *
		 * 	@param name component name
		 *  @param context current context
		 */

        public function set graniteUrlMapping(graniteUrlMapping:String):void {
        	_graniteUrlMapping = graniteUrlMapping;
        }
        
        public function set gravityUrlMapping(gravityUrlMapping:String):void {
        	_gravityUrlMapping = gravityUrlMapping;
        }

        public function set websocketUrlMapping(websocketUrlMapping:String):void {
            _websocketUrlMapping = websocketUrlMapping;
        }

        public function build(type:String, server:IServerApp, options:Object = null):Channel {
            var graniteUrlMapping:String = options && options.graniteUrlMapping ? options.graniteUrlMapping : _graniteUrlMapping;
            var gravityUrlMapping:String = options && options.gravityUrlMapping ? options.gravityUrlMapping : _gravityUrlMapping;
            var websocketUrlMapping:String = options && options.websocketUrlMapping ? options.websocketUrlMapping : _websocketUrlMapping;

            var uri:String, scheme:String;
            if (type == DEFAULT) {
                uri = (server.secure ? "https" : "http") + "://" + server.serverName + ":" + server.serverPort + server.contextRoot + graniteUrlMapping;
                return server.secure ? new SecureAMFChannel("graniteamf", uri) : new AMFChannel("graniteamf", uri);
            }
            else if (type == LONG_POLLING) {
                uri = (server.secure ? "https" : "http") + "://" + server.serverName + ":" + server.serverPort + server.contextRoot + gravityUrlMapping;
                return server.secure ? new SecureGravityChannel("gravityamf", uri) : new GravityChannel("gravityamf", uri);
            }
            else if (type == WEBSOCKET) {
                scheme = server.secure ? "wss" : "ws";
                return new WebSocketChannel("websocketamf", scheme + "://" + server.serverName + ":" + server.serverPort + server.contextRoot + websocketUrlMapping);
            }
            else if (type == WEBSOCKET_EMBEDDED) {
                scheme = server.secure ? "wss" : "ws";
                return new WebSocketChannel("websocketamf", scheme + "://" + server.serverName + ":" + server.serverPort);
            }
            return null;
        }
    }
}
