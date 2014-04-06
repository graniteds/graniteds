/*
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2014 GRANITE DATA SERVICES S.A.S.
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
package	org.granite.tide.service
{

    import flash.events.Event;
    import flash.events.EventDispatcher;

    import mx.logging.ILogger;
    import mx.logging.Log;
    import mx.messaging.Channel;
    import mx.messaging.ChannelSet;
    import mx.messaging.channels.AMFChannel;
    import mx.messaging.channels.SecureAMFChannel;
    import mx.rpc.events.FaultEvent;
    import mx.rpc.events.ResultEvent;
    import mx.rpc.http.HTTPService;
    import mx.rpc.remoting.RemoteObject;

    import org.granite.gravity.Consumer;
    import org.granite.gravity.Producer;
    import org.granite.gravity.channels.GravityChannel;
    import org.granite.gravity.channels.SecureGravityChannel;

    [Event(name="configured", type="flash.events.Event")]
	[Event(name="error", type="flash.events.Event")]
	
	public class DynamicServerApp extends EventDispatcher implements IServerApp {
		
		private static var log:ILogger = Log.getLogger("org.granite.tide.service.DynamicServerApp");
		
		public static const CONFIGURED:String = "configured";
		public static const ERROR:String = "error";

        private var _configUrl:String = null;
		private var _initialized:Boolean = false;
		private var _secure:Boolean = false;
        private var _contextRoot:String = null;
        private var _serverName:String = null;
        private var _serverPort:String = "80";

		
		/**
		 * 	Tide constructor used at component instantiation
		 *
		 * 	@param name component name
		 *  @param context current context
		 */
		public function DynamicServerApp(configUrl:String = "config.xml") {
            _configUrl = configUrl;
		}

        public function get secure():Boolean {
            return _secure;
        }

        public function set secure(value:Boolean):void {
            _secure = value;
        }

        public function get serverName():String {
            return _serverName;
        }

        public function set serverName(value:String):void {
            _serverName = value;
        }

        public function get serverPort():String {
            return _serverPort;
        }

        public function set serverPort(value:String):void {
            _serverPort = value;
        }

        public function get contextRoot():String {
            return _contextRoot;
        }

        public function set contextRoot(value:String):void {
            _contextRoot = value;
        }

        public function initialize():void {
            var httpService:HTTPService = new HTTPService();
            httpService.url = _configUrl;
            httpService.method = "GET";
            httpService.useProxy = false;
            httpService.resultFormat = "e4x";
            httpService.addEventListener(ResultEvent.RESULT, resultHandler);
            httpService.addEventListener(FaultEvent.FAULT, faultHandler);
            httpService.send();
        }

		private function resultHandler(event:ResultEvent):void {
			if (event.result.hasOwnProperty("secure"))
				_secure = event.result.secure;
            else if (event.result.hasOwnProperty("protocol"))
                _secure = event.result.protocol == "https";
            _serverName = event.result["server-name"];
            if (event.result.hasOwnProperty("server-port"))
                _serverPort = event.result["server-port"];
			_contextRoot = event.result["context-root"];

			_initialized = true;
			dispatchEvent(new Event(CONFIGURED));
		}
		
		private function faultHandler(event:FaultEvent):void {
			_initialized = false;
			log.error("Server configuration error: " + event.formatToString("FaultEvent", "fault"));
			dispatchEvent(new Event(ERROR));
		}
		
	}
}