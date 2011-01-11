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

package org.granite.tide {
    
    import flash.events.IEventDispatcher;
    
    import mx.messaging.messages.ErrorMessage;
    
	
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
		 * 	@param context the context in which the error occured
		 *  @param emsg the error message
		 */
	    function handle(context:BaseContext, emsg:ErrorMessage):void;

	}
}
