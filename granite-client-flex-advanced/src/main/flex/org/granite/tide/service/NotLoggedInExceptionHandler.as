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
package org.granite.tide.service {

    import mx.logging.ILogger;
    import mx.logging.Log;
    import mx.messaging.messages.ErrorMessage;

    import org.granite.tide.BaseContext;
    import org.granite.tide.IExceptionHandler;
    import org.granite.tide.IIdentity;
    import org.granite.tide.Tide;

    /**
     * 	@author William DRAI
     */
    public class NotLoggedInExceptionHandler implements IExceptionHandler {
        
        private static var log:ILogger = Log.getLogger("org.granite.tide.service.NotLoggedInExceptionHandler");

		
		public function accepts(emsg:ErrorMessage):Boolean {
			return emsg.faultCode == "Server.Security.NotLoggedIn";
		}
		
		public function handle(serverSession:ServerSession, context:BaseContext, emsg:ErrorMessage):void {
			var identity:IIdentity = context.byType(IIdentity, false) as IIdentity;
			if (identity && identity.loggedIn) {
				// Session expired, directly mark the channel as logged out
				context.meta_logout(serverSession, identity, true);
				identity.loggedIn = false;
				context.raiseEvent(Tide.SESSION_EXPIRED);
			}
		}
    }
}
