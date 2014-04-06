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
package org.granite.test.tide.seam;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletContext;

import org.granite.config.GraniteConfig;
import org.granite.config.ServletGraniteConfig;
import org.granite.config.api.Configuration;
import org.granite.config.api.internal.ConfigurationImpl;
import org.granite.config.flex.Destination;
import org.granite.config.flex.ServicesConfig;
import org.granite.config.flex.ServletServicesConfig;
import org.granite.context.GraniteContext;
import org.granite.gravity.Gravity;
import org.granite.messaging.webapp.HttpGraniteContext;
import org.granite.seam21.Seam21Interceptor;
import org.granite.test.gravity.MockGravity;
import org.granite.tide.invocation.ContextResult;
import org.granite.tide.invocation.ContextUpdate;
import org.granite.tide.invocation.InvocationCall;
import org.granite.tide.invocation.InvocationResult;
import org.granite.tide.seam.SeamServiceFactory;
import org.granite.tide.seam.SeamServiceInvoker;
import org.granite.util.XMap;
import org.jboss.seam.contexts.ServletLifecycle;
import org.jboss.seam.init.Initialization;
import org.jboss.seam.mock.MockHttpServletRequest;
import org.jboss.seam.mock.MockHttpServletResponse;
import org.jboss.seam.mock.MockHttpSession;
import org.jboss.seam.mock.MockServletContext;
import org.junit.After;
import org.junit.Before;

import flex.messaging.messages.AcknowledgeMessage;
import flex.messaging.messages.Message;
import flex.messaging.messages.RemotingMessage;


public class AbstractTideTestCase {
    
    private SeamServiceInvoker invoker = null;
    private Seam21Interceptor interceptor = new Seam21Interceptor();
    private Message requestMessage = new RemotingMessage();
    private Message responseMessage = new AcknowledgeMessage();
    private MockGravity mockGravity = new MockGravity();
    
    
    @Before
    public void setUp() throws Exception {
    	ServletContext servletContext = initServletContext();
        ServletLifecycle.beginApplication(servletContext);
        new Initialization(servletContext).create().init();
        
        servletContext.setAttribute(Gravity.class.getName(), mockGravity);
        
        MockHttpSession session = new MockHttpSession(servletContext) {
        	@Override
        	public String getId() {
        		return "TEST$SESSION";
        	}
        };
        MockHttpServletRequest request = new MockHttpServletRequest(session);
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        Configuration cfg = new ConfigurationImpl();
        cfg.setGraniteConfig("/WEB-INF/granite/granite-config-seam.xml");
        servletContext.setAttribute(ServletGraniteConfig.GRANITE_CONFIG_CONFIGURATION_KEY, cfg);
        GraniteConfig graniteConfig = ServletGraniteConfig.loadConfig(servletContext);
        ServicesConfig servicesConfig = ServletServicesConfig.loadConfig(servletContext);
        HttpGraniteContext.createThreadIntance(graniteConfig, servicesConfig, servletContext, request, response);
        
        interceptor.before(requestMessage);
        
        SeamServiceFactory seamFactory = new SeamServiceFactory();
        seamFactory.configure(new XMap("properties"));
        @SuppressWarnings("unchecked")
        Destination destination = new Destination("seam", Collections.EMPTY_LIST, XMap.EMPTY_XMAP, null, null, null);
        invoker = new SeamServiceInvoker(destination, seamFactory);
    }
    
    protected ServletContext initServletContext() {
    	return new MockServletContext();
    }
    
    @After
    public void tearDown() throws Exception {
        interceptor.after(requestMessage, responseMessage);

        GraniteContext.release();
    }
    
    
    protected Message getLastMessage() {
    	return mockGravity.getLastMessage();
    }
    
    protected InvocationResult invokeComponent(String componentName, String operation, Object[] params) {
        return invokeComponent(componentName, operation, params, null, null, null);
    }
    
    protected InvocationResult invokeComponent(String componentName, String operation, Object[] params, Object[] updates, String[] results, String conversationId) {
    	return invokeComponent(componentName, operation, params, null, updates, results, conversationId);
    }
    
    protected InvocationResult invokeComponent(String componentName, String operation, Object[] params, String[] listeners, Object[] updates, String[] results, String conversationId) {
        RemotingMessage callMessage = new RemotingMessage();
        callMessage.setOperation("invokeComponent");
        Object[] args = new Object[5];
        args[0] = componentName;
        args[1] = null;
        args[2] = operation;
        args[3] = params;
        InvocationCall call = new InvocationCall();
        if (listeners != null)
        	call.setListeners(Arrays.asList(listeners));
        else
            call.setListeners(new ArrayList<String>());
        List<ContextUpdate> cus = new ArrayList<ContextUpdate>();
        if (updates != null) {
            for (int i = 0; i < updates.length; i++) {
                Object[] u = (Object[])updates[i];
                boolean inConv = u.length > 3 ? (Boolean)u[3] : false;
                ContextUpdate cu = new ContextUpdate((String)u[0], (String)u[1], u[2], inConv ? 2 : 1, false);
                cus.add(cu);
            }
        }
        call.setUpdates(cus);
        Object[] res = results != null ? new Object[results.length] : new Object[] {};
        if (results != null) {
            for (int i = 0; i < results.length; i++) {
                int idx = results[i].indexOf(".");
                if (idx > 0)
                    res[i] = new ContextResult(results[i].substring(0, idx), results[i].substring(idx+1));
                else
                    res[i] = new ContextResult(results[i], null);
            }
        }
        call.setResults(res);
        args[4] = call;
        callMessage.setBody(args);
        return (InvocationResult)invoker.invoke(callMessage);
    };
}
