package org.granite.test.tide.ejb;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.openejb.AppContext;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.SecurityServiceInfo;
import org.apache.openejb.assembler.classic.TransactionServiceInfo;
import org.apache.openejb.core.LocalInitialContextFactory;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.openejb.config.ShrinkWrapConfigurationFactory;

public class OpenEJBContainer implements EJBContainer {
	
	private ClassLoader classLoader = null;
	private Assembler assembler = null;
	private InitialContext ctx = null;
	
	public void start(JavaArchive archive) throws Exception {
		ShrinkWrapConfigurationFactory configurationFactory = new ShrinkWrapConfigurationFactory();
		assembler = new Assembler();
		assembler.createTransactionManager(configurationFactory.configureService(TransactionServiceInfo.class));
		assembler.createSecurityService(configurationFactory.configureService(SecurityServiceInfo.class));
		
		AppInfo appInfo = configurationFactory.configureApplication(archive);
		AppContext appContext = assembler.createApplication(appInfo);
		Assembler.installNaming();
		classLoader = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(appContext.getClassLoader());
		
		final Properties properties = new Properties();
		properties.put(Context.INITIAL_CONTEXT_FACTORY, LocalInitialContextFactory.class.getName());
		ctx = new InitialContext(properties);        
	}
	
	public InitialContext getInitialContext() {
		return ctx;
	}

	public void stop() {
        assembler.destroy();
        
        Thread.currentThread().setContextClassLoader(classLoader);
	}
}
