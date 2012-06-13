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
    import mx.messaging.channels.AMFChannel;
    import mx.messaging.channels.SecureAMFChannel;
    import mx.messaging.messages.ErrorMessage;
    import mx.rpc.remoting.RemoteObject;
    
    import org.granite.gravity.Consumer;
    import org.granite.gravity.Producer;
    import org.granite.gravity.channels.GravityChannel;
    import org.granite.gravity.channels.SecureGravityChannel;
    import org.granite.tide.BaseContext;
    import org.granite.tide.IExceptionHandler;
    import org.granite.tide.IIdentity;
    import org.granite.tide.Tide;
    import org.granite.tide.events.TideFaultEvent;
    

    /**
     * 	@author William DRAI
     */
    public class NotLoggedInExceptionHandler implements IExceptionHandler {
        
        private static var log:ILogger = Log.getLogger("org.granite.tide.service.NotLoggedInExceptionHandler");

		
		public function accepts(emsg:ErrorMessage):Boolean {
			return emsg.faultCode == "Server.Security.NotLoggedIn";
		}
		
		public function handle(context:BaseContext, emsg:ErrorMessage):void {
			var identity:IIdentity = context.byType(IIdentity, false) as IIdentity;
			if (identity && identity.loggedIn) {
				// Session expired, directly mark the channel as logged out
				context.meta_logout(identity, true);
				identity.loggedIn = false;
				context.raiseEvent(Tide.SESSION_EXPIRED);
			}
		}
    }
}
