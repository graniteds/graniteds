/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
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

import flex.messaging.messages.Message;
import flex.messaging.messages.RemotingMessage;
import org.granite.config.GraniteConfig;
import org.granite.config.ServletGraniteConfig;
import org.granite.config.api.Configuration;
import org.granite.config.api.internal.ConfigurationImpl;
import org.granite.config.flex.ServicesConfig;
import org.granite.config.flex.ServletServicesConfig;
import org.granite.gravity.Gravity;
import org.granite.messaging.webapp.HttpGraniteContext;
import org.granite.spring.ServerFilter;
import org.granite.test.gravity.MockGravity;
import org.granite.tide.TideServiceInvoker;
import org.granite.tide.invocation.ContextResult;
import org.granite.tide.invocation.ContextUpdate;
import org.granite.tide.invocation.InvocationCall;
import org.granite.tide.invocation.InvocationResult;
import org.granite.tide.spring.SpringServiceFactory;
import org.granite.util.XMap;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.servlet.ServletContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode=ClassMode.AFTER_EACH_TEST_METHOD)
public class AbstractTideInterceptorTestCase implements ApplicationContextAware {
    
    private MockGravity mockGravity = new MockGravity();
    @Autowired
    private ServletContext servletContext;
    private ApplicationContext applicationContext;
    
    public void setApplicationContext(ApplicationContext applicationContext) {
    	this.applicationContext = applicationContext;
    }
    
    @Before
    public void setUp() throws Exception {
        Configuration cfg = new ConfigurationImpl();
        cfg.setGraniteConfig("/WEB-INF/granite/granite-config-spring.xml");
        cfg.setFlexServicesConfig("/WEB-INF/flex/services-config-spring.xml");
        servletContext.setAttribute(ServletGraniteConfig.GRANITE_CONFIG_CONFIGURATION_KEY, cfg);
        GraniteConfig graniteConfig = ServletGraniteConfig.loadConfig(servletContext);
        ServicesConfig servicesConfig = ServletServicesConfig.loadConfig(servletContext);
    	servletContext.setAttribute(Gravity.class.getName(), mockGravity);
        mockGravity.setGraniteConfig(graniteConfig);
    }

    protected void resetLastMessage() {
        mockGravity.resetLastMessage();
    }
    
    protected Message getLastMessage() {
    	return mockGravity.getLastMessage();
    }
}
