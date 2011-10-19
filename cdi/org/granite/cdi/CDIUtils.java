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