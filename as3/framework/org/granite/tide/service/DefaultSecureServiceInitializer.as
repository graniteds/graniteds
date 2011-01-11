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
    import mx.messaging.Channel;
    import mx.messaging.ChannelSet;
	import mx.messaging.channels.SecureAMFChannel;
    import org.granite.gravity.channels.SecureGravityChannel;
    

    /**
     * 	@author William DRAI
     */
    public class DefaultSecureServiceInitializer extends DefaultServiceInitializer {
        
        private static var log:ILogger = Log.getLogger("org.granite.tide.service.DefaultSecureServiceInitializer");

        

		/**
		 * 	Tide constructor used at component instantiation
		 *
		 * 	@param name component name
		 *  @param context current context
		 */
        public function DefaultSecureServiceInitializer(contextRoot:String = "", graniteUrlMapping:String = null, gravityUrlMapping:String = null) {
            super(contextRoot, graniteUrlMapping, gravityUrlMapping);
        }
        
        protected override function get protocol():String {
        	return "https";
        }
        
        protected override function newAMFChannel(id:String, uri:String):Channel {
        	return new SecureAMFChannel(id, uri);
        }
        
        protected override function newGravityChannel(id:String, uri:String):Channel {
        	return new SecureGravityChannel(id, uri);
        }
    }
}
