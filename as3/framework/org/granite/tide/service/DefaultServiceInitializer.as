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
    import org.granite.gravity.channels.GravityChannel;
    

    /**
     * 	@author William DRAI
     */
    public class DefaultServiceInitializer implements IServiceInitializer {
        
        private static var log:ILogger = Log.getLogger("org.granite.tide.service.DefaultServiceInitializer");

		private var _graniteChannelSet:ChannelSet;
		private var _gravityChannelSet:ChannelSet;
		
		private var _contextRoot:String = "";
		private var _serverName:String = "{server.name}";
		private var _serverPort:String = "{server.port}";
		private var _graniteUrlMapping:String = "/graniteamf/amf.txt";		// .txt for stupid bug in IE8
		private var _gravityUrlMapping:String = "/gravityamf/amf.txt";
        

		/**
		 * 	Tide constructor used at component instantiation
		 *
		 * 	@param name component name
		 *  @param context current context
		 */
        public function DefaultServiceInitializer(contextRoot:String = "", graniteUrlMapping:String = null, gravityUrlMapping:String = null) {
            super();
            _contextRoot = contextRoot;
            if (graniteUrlMapping != null)
            	_graniteUrlMapping = graniteUrlMapping;
            if (gravityUrlMapping != null)
            	_gravityUrlMapping = gravityUrlMapping;
        }
        
        public function set contextRoot(contextRoot:String):void {
        	_contextRoot = contextRoot;
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
        	return "http";
        }
        
        protected function newAMFChannel(id:String, uri:String):Channel {
        	return new AMFChannel(id, uri);
        }
        
        protected function newGravityChannel(id:String, uri:String):Channel {
        	return new GravityChannel(id, uri);
        }
        
        private function get graniteChannelSet():ChannelSet {
        	if (_graniteChannelSet == null) {
				_graniteChannelSet = new ChannelSet();
				_graniteChannelSet.addChannel(newAMFChannel("graniteamf", protocol + "://" + _serverName + ":" + _serverPort + _contextRoot + _graniteUrlMapping));
        	}
        	return _graniteChannelSet;
        }
        
        private function get gravityChannelSet():ChannelSet {
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
