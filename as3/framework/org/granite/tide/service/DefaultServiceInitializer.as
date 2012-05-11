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

package org.granite.tide.service {

    import mx.logging.ILogger;
    import mx.logging.Log;
    import mx.rpc.remoting.RemoteObject;
    import org.granite.gravity.Consumer;
    import org.granite.gravity.Producer;
    import mx.messaging.Channel;
    import mx.messaging.ChannelSet;
	import mx.messaging.channels.AMFChannel;
	import mx.messaging.channels.SecureAMFChannel;
	import org.granite.tide.Tide;
    import org.granite.gravity.channels.GravityChannel;
	import org.granite.gravity.channels.SecureGravityChannel;
    

    /**
     * 	@author William DRAI
     */
    public class DefaultServiceInitializer implements IServiceInitializer {
        
        private static var log:ILogger = Log.getLogger("org.granite.tide.service.DefaultServiceInitializer");

		private var _graniteChannelSet:ChannelSet;
		private var _gravityChannelSet:ChannelSet;
		
		protected var _secure:Boolean = false;
		protected var _contextRoot:String = "";
		protected var _serverName:String = "{server.name}";
		protected var _serverPort:String = "{server.port}";
		protected var _graniteUrlMapping:String = "/graniteamf/amf.txt";		// .txt for stupid bug in IE8
		protected var _gravityUrlMapping:String = "/gravityamf/amf.txt";
        

		/**
		 * 	Tide constructor used at component instantiation
		 *
		 * 	@param name component name
		 *  @param context current context
		 */
        public function DefaultServiceInitializer(contextRoot:String = "", graniteUrlMapping:String = null, gravityUrlMapping:String = null, secure:Boolean = false) {
            super();
			var application:Object = Tide.currentApplication();
			
			if (application.url && application.url.indexOf("https") == 0)
				_secure = true;
			else
				_secure = secure;
			
			if (!contextRoot && application.url) {
				var idx:int = application.url.indexOf("://");
				if (idx > 0) {
					idx = application.url.indexOf("/", idx+3);
					if (idx > 0) {
						var idx2:int = application.url.indexOf("/", idx+1);
						if (idx2 > 0 && idx2 > idx)
							_contextRoot = application.url.substring(idx, idx2);
					}
				}
			}
			else
				_contextRoot = contextRoot;

            if (graniteUrlMapping != null)
            	_graniteUrlMapping = graniteUrlMapping;
            if (gravityUrlMapping != null)
            	_gravityUrlMapping = gravityUrlMapping;
        }
        
        public function set contextRoot(contextRoot:String):void {
        	_contextRoot = contextRoot;
        }

		public function set secure(secure:Boolean):void {
			_secure = secure;
		}
        
        public function set serverName(serverName:String):void {
        	_serverName = serverName;
        }
        
        public function set serverPort(serverPort:String):void {
        	_serverPort = serverPort;
        }
        
        public function set graniteUrlMapping(graniteUrlMapping:String):void {
        	_graniteUrlMapping = graniteUrlMapping;
        }
        
        public function set gravityUrlMapping(gravityUrlMapping:String):void {
        	_gravityUrlMapping = gravityUrlMapping;
        }
        
        protected function get protocol():String {
        	return _secure ? "https" : "http";
        }
        
        protected function newAMFChannel(id:String, uri:String):Channel {
			if (_secure)
				return new SecureAMFChannel(id, uri);
        	return new AMFChannel(id, uri);
        }
        
        protected function newGravityChannel(id:String, uri:String):Channel {
			if (_secure)
				return new SecureGravityChannel(id, uri);
        	return new GravityChannel(id, uri);
        }
        
		protected function get graniteChannelSet():ChannelSet {
        	if (_graniteChannelSet == null) {
				_graniteChannelSet = new ChannelSet();
				_graniteChannelSet.addChannel(newAMFChannel("graniteamf", protocol + "://" + _serverName + ":" + _serverPort + _contextRoot + _graniteUrlMapping));
        	}
        	return _graniteChannelSet;
        }
        
        protected function get gravityChannelSet():ChannelSet {
        	if (_gravityChannelSet == null) {
				_gravityChannelSet = new ChannelSet();
				_gravityChannelSet.addChannel(newGravityChannel("gravityamf", protocol + "://" + _serverName + ":" + _serverPort + _contextRoot + _gravityUrlMapping));
        	}
        	return _gravityChannelSet;
        }
        
        public function initialize(service:Object):void {
        	if (service is RemoteObject)
				service.channelSet = graniteChannelSet;
			else if (service is Consumer || service is Producer)
				service.channelSet = gravityChannelSet;
        }
    }
}
