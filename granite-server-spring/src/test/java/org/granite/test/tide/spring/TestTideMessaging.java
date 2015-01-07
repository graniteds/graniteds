/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2015 GRANITE DATA SERVICES S.A.S.
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
package org.granite.test.tide.spring;

import java.lang.reflect.Field;
import java.util.HashMap;

import javax.inject.Inject;
import javax.servlet.ServletContext;

import org.granite.context.GraniteContext;
import org.granite.context.SimpleGraniteContext;
import org.granite.gravity.DefaultGravityFactory;
import org.granite.gravity.Gravity;
import org.granite.gravity.GravityConfig;
import org.granite.gravity.GravityInternal;
import org.granite.gravity.GravityManager;
import org.granite.gravity.adapters.JMSServiceAdapter;
import org.granite.messaging.jmf.DefaultSharedContext;
import org.granite.spring.SpringGraniteConfig;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import flex.messaging.messages.AsyncMessage;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={ "/org/granite/test/tide/spring/test-context-graniteds-activemq.xml" })
public class TestTideMessaging extends AbstractJUnit4SpringContextTests {
	
	@Inject
	SpringGraniteConfig springConfig;
	
	@Inject
	private ServletContext servletContext;
	
	@Before
	public void setup() throws Exception {
        GravityConfig gravityConfig = new GravityConfig(springConfig.getGraniteConfig());
        springConfig.getGraniteConfig().setSharedContext(new DefaultSharedContext());
		Gravity gravity = new DefaultGravityFactory().newGravity(gravityConfig, springConfig.getServicesConfig(), springConfig.getGraniteConfig());
        gravity.start();
        servletContext.setAttribute(Gravity.class.getName(), gravity);
        SimpleGraniteContext.createThreadInstance(springConfig.getGraniteConfig(), springConfig.getServicesConfig(), new HashMap<String, Object>());
	}
	
	@Test
    public void testConfigGDS1043() throws Exception {
        GravityInternal gravity = (GravityInternal)GravityManager.getGravity(servletContext);
		
		JMSServiceAdapter adapter = (JMSServiceAdapter)gravity.getServiceAdapter(AsyncMessage.class.getName(), "testTopic");
		
		Field field = JMSServiceAdapter.class.getDeclaredField("textMessages");
		field.setAccessible(true);
		Boolean textMessages = (Boolean)field.get(adapter);
		
		Assert.assertTrue("TextMessages", textMessages);
    }
	
	@After
	public void tearDown() {
		GraniteContext.release();
	}
}
