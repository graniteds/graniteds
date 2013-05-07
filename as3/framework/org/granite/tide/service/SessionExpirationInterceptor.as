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
	
	import flash.events.TimerEvent;
	import flash.utils.Timer;
	
	import mx.messaging.messages.ErrorMessage;
	import mx.messaging.messages.IMessage;
	
	import org.granite.tide.BaseContext;
	import org.granite.tide.IExceptionHandler;
	import org.granite.tide.IIdentity;
	import org.granite.tide.IMessageInterceptor;
	import org.granite.tide.Tide;
	import org.granite.tide.spring.Identity;

    /**
     *  
     * 	@author William DRAI
     */
    public class SessionExpirationInterceptor implements IMessageInterceptor, IExceptionHandler {
		
		public static const SERVER_TIME_TAG:String = "org.granite.time";
		public static const SESSION_EXP_TAG:String = "org.granite.sessionExp";
		
		public function accepts(emsg:ErrorMessage):Boolean {
			return emsg.faultCode == "Server.Security.NotLoggedIn";
		}
		
		public function handle(context:BaseContext, emsg:ErrorMessage):void {
			var identity:IIdentity = context.byType(IIdentity) as IIdentity;
			if (identity != null && identity.loggedIn) {
				identity.loggedIn = false;
				
				// Session expired, directly mark the channel as logged out
				context.meta_tide.sessionExpired(context);
			}
		}
        
		private var _expirationTimer:Timer;
		
        public function before(message:IMessage):void {
		}
		
		public function after(message:IMessage):void {
			if (_expirationTimer != null && _expirationTimer.running) {
				_expirationTimer.reset();
				_expirationTimer = null;
			}
			
			if (!message.headers.hasOwnProperty(SESSION_EXP_TAG))
				return;
			
			var serverTime:Number = message.headers[SERVER_TIME_TAG] as Number;
			var sessionExpirationDelay:Number = message.headers[SESSION_EXP_TAG] as Number;
			rescheduleSessionExpirationTask(serverTime, sessionExpirationDelay);
		}
		
		private function rescheduleSessionExpirationTask(serverTime:Number, sessionExpirationDelay:Number):void {
			var clientOffset:Number = serverTime - new Date().time;
			_expirationTimer = new Timer(clientOffset + sessionExpirationDelay*1000 + 1500);
			_expirationTimer.addEventListener(TimerEvent.TIMER, sessionExpirationHandler, false, 0, true);
			_expirationTimer.start();
		}
		
		private function sessionExpirationHandler(event:TimerEvent):void {
			var identity:IIdentity = Tide.getInstance().getContext().byType(IIdentity) as IIdentity;
			if (identity != null)
				identity.isLoggedIn();
		}
    }
}
