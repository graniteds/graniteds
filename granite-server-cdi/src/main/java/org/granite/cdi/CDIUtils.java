/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2014 GRANITE DATA SERVICES S.A.S.
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
package org.granite.cdi;

import javax.enterprise.inject.spi.BeanManager;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.servlet.ServletContext;


public class CDIUtils {
	
    public static BeanManager lookupBeanManager(ServletContext servletContext) {
		BeanManager manager = (BeanManager)servletContext.getAttribute(BeanManager.class.getName());
		if (manager != null)
			return manager;		
		manager = (BeanManager)servletContext.getAttribute("org.jboss.weld.environment.servlet.javax.enterprise.inject.spi.BeanManager");
		if (manager != null)
			return manager;
		
		InitialContext ic = null;
	    try {
			ic = new InitialContext();
	    	// Standard JNDI binding
	    	return (BeanManager)ic.lookup("java:comp/BeanManager");
	    }
	    catch (NameNotFoundException e) {
	    	if (ic == null)
	    		throw new RuntimeException("No InitialContext");
	    	
	    	// Weld/Tomcat
	    	try {
	    		return (BeanManager)ic.lookup("java:comp/env/BeanManager"); 
	    	}
	    	catch (Exception e1) { 	    	
	    		// JBoss 5/6 (maybe obsolete in Weld 1.0+)
		    	try {
		    		return (BeanManager)ic.lookup("java:app/BeanManager");
		    	}
	    	    catch (Exception e2) {
	    	    	throw new RuntimeException("Could not find Bean Manager", e2);
	    	    }
	    	}
	    }
	    catch (Exception e) {
	    	throw new RuntimeException("Could not find Bean Manager", e);
	    }
    }
}