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

package org.granite.tide.ejb;

import java.io.Serializable;

import org.granite.context.GraniteContext;
import org.granite.messaging.webapp.HttpGraniteContext;


/**
 * @author William DRAI
 */
public class EjbIdentity implements Serializable {

    private static final long serialVersionUID = 1L;
    
        
    public String isLoggedIn() {
    	GraniteContext context = GraniteContext.getCurrentInstance();
    	if (context != null && ((HttpGraniteContext)context).getRequest().getUserPrincipal() != null)
    		return ((HttpGraniteContext)context).getRequest().getUserPrincipal().getName();    	
    	return null;
    }
    
    
    public boolean hasRole(String role) {
    	GraniteContext context = GraniteContext.getCurrentInstance();
    	if (context == null || !(context instanceof HttpGraniteContext))
    		return false;
    	
    	return ((HttpGraniteContext)context).getRequest().isUserInRole(role);
    }
}
