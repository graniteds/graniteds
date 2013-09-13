/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of Granite Data Services.
 *
 *   Granite Data Services is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU Library General Public License as published by
 *   the Free Software Foundation; either version 2 of the License, or (at your
 *   option) any later version.
 *
 *   Granite Data Services is distributed in the hope that it will be useful, but
 *   WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *   FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
 *   for more details.
 *
 *   You should have received a copy of the GNU Library General Public License
 *   along with this library; if not, see <http://www.gnu.org/licenses/>.
 */
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
