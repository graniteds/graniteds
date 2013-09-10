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

package org.granite.tide.events {
	
	/**
	 *  Event interceptors are triggered before and after Tide events are dispatched to observers
	 *  An interceptor can prevent dispatch of an event by calling preventDefault()
	 * 
	 * 	@author William DRAI
	 */
	public interface IEventInterceptor {
	    
	    /**
	     *  Called before event is dispatched
	     *   
	     *  @param event event to be dispatched
	     */ 
	    function beforeDispatch(event:TideContextEvent):void;
	    
	    /**
	     *  Called after event has been dispatched
	     *  
	     *  @param event event that has been dispatched
	     */  
	    function afterDispatch(event:TideContextEvent):void;	    	    
	}
}
