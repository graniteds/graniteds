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
package org.granite.test.tide.cdi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import javax.enterprise.inject.spi.BeanManager;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletRequestEvent;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

import org.granite.cdi.CDIInterceptor;
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
import org.granite.test.gravity.MockGravity;
import org.granite.test.tide.MockHttpServletRequest;
import org.granite.test.tide.MockHttpServletResponse;
import org.granite.test.tide.MockServletContext;
import org.granite.tide.cdi.CDIServiceFactory;
import org.granite.tide.cdi.CDIServiceInvoker;
import org.granite.tide.invocation.ContextResult;
import org.granite.tide.invocation.ContextUpdate;
import org.granite.tide.invocation.InvocationCall;
import org.granite.tide.invocation.InvocationResult;
import org.granite.util.XMap;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.jboss.weld.servlet.WeldListener;
import org.junit.After;
import org.junit.Before;

import flex.messaging.messages.AcknowledgeMessage;
import flex.messaging.messages.Message;
import flex.messaging.messages.RemotingMessage;


@SuppressWarnings("deprecation")
public class AbstractTideTestCase {
    
	private ServletContext servletContext = null;
	private Weld weld = null;
	protected WeldContainer container = null;
	private WeldListener weldListener = new WeldListener();
    private CDIServiceInvoker invoker = null;
    private CDIInterceptor interceptor = null;
    private HttpServletRequest request = null;
    private HttpServletResponse response = null;
    private Message requestMessage = new RemotingMessage();
    private Message responseMessage = new AcknowledgeMessage();
    private MockGravity mockGravity = new MockGravity();
    
    
    @Before
    public void setUp() throws Exception {
    	servletContext = initServletContext();
    	
    	weld = new Weld();
    	container = weld.initialize();
    	servletContext.setAttribute(BeanManager.class.getName(), container.getBeanManager());
    	
    	weldListener.contextInitialized(new ServletContextEvent(servletContext));
        
        servletContext.setAttribute(Gravity.class.getName(), mockGravity);
        
        MockHttpSession session = new MockHttpSession("TEST$SESSION", servletContext);
        request = new MockHttpServletRequest(session);
        response = new MockHttpServletResponse();        
        
        Configuration cfg = new ConfigurationImpl();
        cfg.setFlexServicesConfig("/WEB-INF/flex/services-config-cdi.xml");
        cfg.setGraniteConfig("/WEB-INF/granite/granite-config-cdi.xml");
        servletContext.setAttribute(ServletGraniteConfig.GRANITE_CONFIG_CONFIGURATION_KEY, cfg);
        GraniteConfig graniteConfig = ServletGraniteConfig.loadConfig(servletContext);
        ServicesConfig servicesConfig = ServletServicesConfig.loadConfig(servletContext);
        HttpGraniteContext.createThreadIntance(graniteConfig, servicesConfig, servletContext, request, response);
        
        weldListener.requestInitialized(new ServletRequestEvent(servletContext, request));
 
        interceptor = new CDIInterceptor();
        interceptor.before(requestMessage);
        
        CDIServiceFactory cdiFactory = new CDIServiceFactory();
        cdiFactory.configure(new XMap("properties"));
        @SuppressWarnings("unchecked")
        Destination destination = new Destination("server", Collections.EMPTY_LIST, XMap.EMPTY_XMAP, null, null, null);
        invoker = new CDIServiceInvoker(destination, cdiFactory);
    }
    
    protected ServletContext initServletContext() {
    	return new MockServletContext();
    }
    
    @After
    public void tearDown() throws Exception {
        interceptor.after(requestMessage, responseMessage);
        
        weldListener.requestDestroyed(new ServletRequestEvent(servletContext, request));
        
    	weldListener.contextDestroyed(new ServletContextEvent(servletContext));
    	
        weld.shutdown();

        GraniteContext.release();
    }
    
    
    protected Message getLastMessage() {
    	return mockGravity.getLastMessage();
    }
    
    protected InvocationResult invokeComponent(String componentName, Class<?> componentClass, String operation, Object[] params) {
        return invokeComponent(componentName, componentClass, operation, params, null, null, null);
    }
    
    protected InvocationResult invokeComponent(String componentName, Class<?> componentClass, String operation, Object[] params, Object[] updates, String[] results, String conversationId) {
    	return invokeComponent(componentName, componentClass, operation, params, null, updates, results, conversationId);
    }
    
    protected InvocationResult invokeComponent(String componentName, Class<?> componentClass, String operation, Object[] params, String[] listeners, Object[] updates, String[] results, String conversationId) {
        RemotingMessage callMessage = new RemotingMessage();
        callMessage.setOperation("invokeComponent");
        Object[] args = new Object[5];
        args[0] = componentName;
        args[1] = componentClass != null ? componentClass.getName() : null;
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
    
    
	private static final class MockHttpSession implements HttpSession {
    	
    	private final long creationTime = System.currentTimeMillis();
    	private int maxInactiveInterval = 60;
    	private long lastAccessedTime = -1L;
    	private final String sessionId;
    	private final ServletContext servletContext;
    	private final Hashtable<String, Object> attributes = new java.util.Hashtable<String, Object>();
    	private boolean valid = true;
    	
    	public MockHttpSession(String sessionId, ServletContext servletContext) {
    		this.sessionId = sessionId;
    		this.servletContext = servletContext;
    	}

		@Override
		public Object getAttribute(String name) {
			return attributes.get(name);
		}

		@Override
		public Enumeration<String> getAttributeNames() {
			return attributes.keys();
		}

		@Override
		public long getCreationTime() {
			return creationTime;
		}

		@Override
		public String getId() {
			return sessionId;
		}

		@Override
		public long getLastAccessedTime() {
			return lastAccessedTime;
		}

		@Override
		public int getMaxInactiveInterval() {
			return maxInactiveInterval;
		}

		@Override
		public ServletContext getServletContext() {
			return servletContext;
		}

		@Override
		public HttpSessionContext getSessionContext() {
			throw new UnsupportedOperationException();
		}
		
		@Override
		public Object getValue(String name) {
			return getAttribute(name);
		}

		@Override
		public String[] getValueNames() {
			return attributes.keySet().toArray(new String[attributes.keySet().size()]);
		}
		
		@Override
		public void invalidate() {
		     attributes.clear();
		     valid = false;
		}

		@Override
		public boolean isNew() {
			return false;
		}

		@Override
		public void putValue(String name, Object value) {
			attributes.put(name, value);
		}

		@Override
		public void removeAttribute(String name) {
			attributes.remove(name);
		}

		@Override
		public void removeValue(String name) {
			attributes.remove(name);
		}
		
		@Override
		public void setAttribute(String name, Object value) {
			if (value == null)
				attributes.remove(name);
			else
				attributes.put(name, value);
		}
		
		@Override
		public void setMaxInactiveInterval(int maxInactiveInterval) {
			this.maxInactiveInterval = maxInactiveInterval;			
		}
		
		@SuppressWarnings("unused")
		public void access() {
			lastAccessedTime = System.currentTimeMillis();
		}
		
		@SuppressWarnings("unused")
		public boolean isValid() {
			return valid;
		}
    	
    }
}
