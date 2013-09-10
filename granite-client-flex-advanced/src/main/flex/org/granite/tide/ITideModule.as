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
    	
	/**
	 * 	ITideModule defines the basic interface for Tide modules
	 * 	Tide modules just have to implement the init method which can register components, exception handlers...
	 * 
	 * 	Tide modules are similar to the components.xml in JBoss Seam
	 * 
	 * 	@author William DRAI
	 */
	public interface ITideModule {
	    
	    /**
	     *	Module initialization method
	     * 
	     * 	@param tide the Tide singleton
	     */ 
	    function init(tide:Tide):void;
	}
}
