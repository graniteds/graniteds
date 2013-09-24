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
    
    import flash.events.IEventDispatcher;
    
    import mx.messaging.messages.ErrorMessage;

    import org.granite.tide.service.ServerSession;

    /**
	 * 	Interface for exception handlers
	 * 
	 * 	An exception handler should tell which ErrorMessage it handles and do some action when the accepted error happens
	 * 
	 * 	@author William DRAI
	 */
	public interface IExceptionHandler {
	    
	    /**
	     * 	Should return true if this handler is able to manage the specified ErrorMessage
	     *
	     *  @param emsg an error message 
	     *  @return true if ErrorMessage accepted
	     */
	    function accepts(emsg:ErrorMessage):Boolean;
		
		/**
		 * 	Handle the error
		 *
         *  @param serverSession server session
		 * 	@param context the context in which the error occured
		 *  @param emsg the error message
		 */
	    function handle(serverSession:ServerSession, context:BaseContext, emsg:ErrorMessage):void;

	}
}
