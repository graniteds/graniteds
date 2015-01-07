/*
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2015 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of the Granite Data Services Platform.
 *
 *   Granite Data Services is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Lesser General Public
 *   License as published by the Free Software Foundation; either
 *   version 2.1 of the License, or (at your option) any later version.
 *
 *   Granite Data Services is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 *   General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public
 *   License along with this library; if not, write to the Free Software
 *   Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301,
 *   USA, or see <http://www.gnu.org/licenses/>.
 */
package org.granite.rpc.remoting.mxml {

    import flash.events.Event;
    
    import mx.logging.Log;
    import mx.logging.ILogger;

    import mx.rpc.remoting.mxml.RemoteObject;
    import mx.rpc.events.FaultEvent;

    import org.granite.events.SecurityEvent;


    /**
     *	Subclass of RemoteObject that wraps security errors.
     *  
     * 	@author Franck WOLFF
     */
    public dynamic class SecureRemoteObject extends RemoteObject {
        
        private static var log:ILogger = Log.getLogger("org.granite.rpc.remoting.mxml.SecureRemoteObject");


        public function SecureRemoteObject(destination:String = null) {
            super(destination);
        }


        override public function addEventListener(type:String,
                                                  listener:Function,
                                                  useCapture:Boolean = false,
                                                  priority:int = 0,
                                                  useWeakReference:Boolean = false):void {
            if (type == SecurityEvent.ALL) {
                super.addEventListener(SecurityEvent.INVALID_CREDENTIALS, listener, useCapture, priority, useWeakReference);
                super.addEventListener(SecurityEvent.NOT_LOGGED_IN, listener, useCapture, priority, useWeakReference);
                super.addEventListener(SecurityEvent.SESSION_EXPIRED, listener, useCapture, priority, useWeakReference);
                super.addEventListener(SecurityEvent.ACCESS_DENIED, listener, useCapture, priority, useWeakReference);
            }
            else
                super.addEventListener(type, listener, useCapture, priority, useWeakReference);
        }


        override public function dispatchEvent(event:Event):Boolean {

            // Flex3 compatibility: 'Server.Security.InvalidCredentials' -> 'Channel.Authentication.Error'...
            if (event is FaultEvent && FaultEvent(event).fault.faultCode != null && (
                    FaultEvent(event).fault.faultCode.search("Server.Security.") == 0 ||
                    FaultEvent(event).fault.faultCode.search("Channel.Authentication.Error") == 0)) {

                log.debug("dispatchEvent (original): {0}", event);

                var type:String = null;

                switch (FaultEvent(event).fault.faultCode) {
                    case "Server.Security.InvalidCredentials":
                    case "Channel.Authentication.Error":
                        type = SecurityEvent.INVALID_CREDENTIALS;
                        break;
                    case "Server.Security.NotLoggedIn":
                        type = SecurityEvent.NOT_LOGGED_IN;
                        break;
                    case "Server.Security.SessionExpired":
                        type = SecurityEvent.SESSION_EXPIRED;
                        break;
                    case "Server.Security.AccessDenied":
                        type = SecurityEvent.ACCESS_DENIED;
                        break;
                    default:
                        throw new Error("Unknown security fault code: " + FaultEvent(event).fault.faultCode);
                }

                event = new SecurityEvent(type);

                log.debug("dispatchEvent (converted): {0}", event);
            }

            return super.dispatchEvent(event);
        }
    }
}