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
package org.granite.tide {
    
    import mx.messaging.messages.ErrorMessage;
	
	import org.granite.tide.events.TideFaultEvent;
    import org.granite.tide.service.ServerSession;

    /**
	 * 	Interface for extended exception handlers
	 * 
	 * 	An exception handler should tell which ErrorMessage it handles and do some action when the accepted error happens
	 *  Extended exception handlers can get the origin TideFaultEvent
	 * 
	 *  Note: this should be deprecated in the next major release and merged in the standard IExceptionHandler interface
	 * 
	 * 	@author William DRAI
	 */
	public interface IExtendedExceptionHandler extends IExceptionHandler {
	    
		/**
		 * 	Handle the error
		 *
         *  @param serverSession server session
         * 	@param context the context in which the error occured
		 *  @param emsg the error message
		 *  @param event the full fault event
		 */
	    function handleEx(serverSession:ServerSession, context:BaseContext, emsg:ErrorMessage, event:TideFaultEvent):void;

	}
}
