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
import org.granite.gravity.GravityManager;
import org.granite.gravity.adapters.ActiveMQServiceAdapter;
import org.granite.gravity.adapters.JMSServiceAdapter;
import org.granite.spring.SpringGraniteConfig;
import org.granite.test.tide.MockChannelFactory;
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
public class TideMessagingTest extends AbstractJUnit4SpringContextTests {
	
	@Inject
	SpringGraniteConfig springConfig;
	
	@Inject
	private ServletContext servletContext;
	
	@Before
	public void setup() throws Exception {
        GravityConfig gravityConfig = new GravityConfig(springConfig.getGraniteConfig(), new MockChannelFactory());        
		Gravity gravity = new DefaultGravityFactory().newGravity(gravityConfig, springConfig.getServicesConfig(), springConfig.getGraniteConfig());
        gravity.start();
        servletContext.setAttribute(Gravity.class.getName(), gravity);
        SimpleGraniteContext.createThreadInstance(springConfig.getGraniteConfig(), springConfig.getServicesConfig(), new HashMap<String, Object>());
	}
	
	@Test
    public void testConfigGDS1043() throws Exception {
		Gravity gravity = GravityManager.getGravity(servletContext);
		
		ActiveMQServiceAdapter adapter = (ActiveMQServiceAdapter)gravity.getServiceAdapter(AsyncMessage.class.getName(), "testTopic");
		
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
