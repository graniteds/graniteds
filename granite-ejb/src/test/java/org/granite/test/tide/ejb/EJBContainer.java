package org.granite.test.tide.ejb;

import javax.naming.InitialContext;

import org.jboss.shrinkwrap.api.spec.JavaArchive;

public interface EJBContainer {

	public void start(JavaArchive archive) throws Exception;
	
	public InitialContext getInitialContext();
	
	public void stop() throws Exception;
}
