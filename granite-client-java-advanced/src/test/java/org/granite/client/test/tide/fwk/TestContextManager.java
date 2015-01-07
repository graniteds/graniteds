/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2015 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of the Granite Data Services Platform.
 *
 *                               ***
 *
 *   Community License: GPL 3.0
 *
 *   This file is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published
 *   by the Free Software Foundation, either version 3 of the License,
 *   or (at your option) any later version.
 *
 *   This file is distributed in the hope that it will be useful, but
 *   WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *                               ***
 *
 *   Available Commercial License: GraniteDS SLA 1.0
 *
 *   This is the appropriate option if you are creating proprietary
 *   applications and you are not prepared to distribute and share the
 *   source code of your application under the GPL v3 license.
 *
 *   Please visit http://www.granitedataservices.com/license for more
 *   details.
 */
package org.granite.client.test.tide.fwk;

import java.lang.reflect.Field;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.granite.client.tide.Context;
import org.granite.client.tide.Module;
import org.granite.client.tide.impl.ComponentImpl;
import org.granite.client.tide.impl.SimpleContextManager;
import org.granite.client.tide.server.Component;
import org.granite.client.tide.server.ServerSession;
import org.junit.Assert;
import org.junit.Test;

public class TestContextManager {

	@Module
	public static class App {
		
		@Singleton
		public static ServerSession serverSession() throws Exception {
			return new ServerSession();
		}
		
		@Singleton
		@Named("bla")
		public static Component bla() {
			return new ComponentImpl();
		}
		
		@Singleton @Named
		public static Component blo(ServerSession serverSession) {
			return new ComponentImpl(serverSession);
		}
		
		@Singleton @Named
		public static Component blo2(ServerSession serverSession) {
			return new ComponentImpl(serverSession);
		}
	}
	
	@Test
	public void testConfig() throws Exception {
		
		SimpleContextManager contextManager = new SimpleContextManager();
		contextManager.initModules(App.class.getName());
		Context context = contextManager.getContext();
		
		Assert.assertTrue("Named component", context.byName("bla") instanceof ComponentImpl);
		Assert.assertTrue("Implicit named", context.byName("blo") instanceof ComponentImpl);
		Assert.assertNull("Not found", context.byName("blu"));
		Assert.assertTrue("Typed component", context.byType(ServerSession.class) != null);
		Assert.assertSame("Component init", context.byType(ServerSession.class), getField(context.byName("blo"), "serverSession"));
		Assert.assertSame("Component init 2", context.byType(ServerSession.class), getField(context.byName("blo2"), "serverSession"));
	}


	@Module
	public static class App2 {
		
		@Singleton @Named
		public static ServerSession serverSession1() throws Exception {
			return new ServerSession();
		}
		
		@Singleton @Named
		public static ServerSession serverSession2() throws Exception {
			return new ServerSession();
		}
		
		@Singleton @Named
		public static Component blo1(@Named("serverSession1") ServerSession serverSession) {
			return new ComponentImpl(serverSession);
		}
		
		@Singleton @Named
		public static Component blo2(@Named("serverSession2") ServerSession serverSession) {
			return new ComponentImpl(serverSession);
		}
	}
	
	@Test
	public void testConfig2() throws Exception {
		
		SimpleContextManager contextManager = new SimpleContextManager();
		contextManager.initModules(App2.class.getName());
		Context context = contextManager.getContext();
		
		Assert.assertTrue("Implicit named", context.byName("blo1") instanceof ComponentImpl);
		Assert.assertSame("Component init", context.byName("serverSession1"), getField(context.byName("blo1"), "serverSession"));
		Assert.assertSame("Component init 2", context.byName("serverSession2"), getField(context.byName("blo2"), "serverSession"));
	}
	
	
	private static class Bean {
		
		@Inject @Named
		public Component bla;
		
		@Inject @Named
		public ComponentImpl blo;
		
		@Inject @Named("blo2")
		public Component blox;
		
		@Inject
		public ServerSession serverSession;
	}
	
	@Test
	public void testInject() throws Exception {
		
		SimpleContextManager contextManager = new SimpleContextManager();
		contextManager.initModules(App.class.getName());
		Context context = contextManager.getContext();
		
		Bean bean = new Bean();
		context.set(bean);
		
		Assert.assertTrue("Named component", bean.bla != null);
		Assert.assertTrue("Implicit named", bean.blo != null);
		Assert.assertTrue("Typed component", bean.serverSession != null);
		Assert.assertSame("Component init", bean.serverSession, getField(bean.blo, "serverSession"));
		Assert.assertSame("Component init 2", bean.serverSession, getField(bean.blox, "serverSession"));
	}
	

	@Module
	public static class App3 {
		
		@Singleton
		public static ServerSession serverSession() throws Exception {
			return new ServerSession();
		}
		
		@Named
		public static Component blo(ServerSession serverSession) {
			return new ComponentImpl(serverSession);
		}
		
		@Named
		public static Component blo2(ServerSession serverSession) {
			return new ComponentImpl(serverSession);
		}
	}
	
	@Test
	public void testConfigScopes() throws Exception {
		
		SimpleContextManager contextManager = new SimpleContextManager();
		contextManager.initModules(App3.class.getName());
		Context globalContext = contextManager.getContext();
		Context context = contextManager.getContext("bla");
		
		Assert.assertTrue("Implicit named", context.byName("blo") instanceof ComponentImpl);
		Assert.assertSame("Component init", globalContext.byType(ServerSession.class), getField(context.byName("blo"), "serverSession"));
		Assert.assertSame("Component init 2", globalContext.byType(ServerSession.class), getField(context.byName("blo2"), "serverSession"));
	}
	
	
	private Object getField(Object obj, String name) throws Exception {
		Field field = obj.getClass().getDeclaredField(name);
		field.setAccessible(true);
		return field.get(obj);
	}
}
