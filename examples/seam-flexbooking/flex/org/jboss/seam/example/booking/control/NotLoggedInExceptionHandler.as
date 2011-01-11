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

package org.jboss.seam.example.booking.control {

    import mx.messaging.messages.ErrorMessage;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.IExceptionHandler;
    
	public class NotLoggedInExceptionHandler implements IExceptionHandler {
       
        public static const NOT_LOGGED_IN:String = "Server.Security.NotLoggedIn";

        public function accepts(emsg:ErrorMessage):Boolean {
            return emsg.faultCode == NOT_LOGGED_IN;
        }

        public function handle(context:BaseContext, emsg:ErrorMessage):void {
            if (context.identity.loggedIn) {
                context.statusMessages.clearMessages();
                context.statusMessages.addMessage("WARN", "Session expired, please log in");
                context.identity.loggedIn = false;
             }
        }
    }
}
