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
	
	import org.granite.tide.events.TideFaultEvent;
    
	
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
		 * 	@param context the context in which the error occured
		 *  @param emsg the error message
		 *  @param event the full fault event
		 */
	    function handleEx(context:BaseContext, emsg:ErrorMessage, event:TideFaultEvent):void;

	}
}
