package org.granite.test.tide.cdi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.enterprise.inject.spi.BeanManager;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletRequestEvent;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.granite.cdi.CDIInterceptor;
import org.granite.config.GraniteConfig;
import org.granite.config.ServletGraniteConfig;
import org.granite.config.api.Configuration;
import org.granite.config.api.internal.ConfigurationImpl;
import org.granite.config.flex.Destination;
import org.granite.config.flex.ServicesConfig;
import org.granite.config.flex.ServletServicesConfig;
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
import org.jboss.weld.mock.MockHttpSession;
import org.jboss.weld.servlet.WeldListener;
import org.junit.After;
import org.junit.Before;

import flex.messaging.messages.AcknowledgeMessage;
import flex.messaging.messages.Message;
import flex.messaging.messages.RemotingMessage;


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
        Destination destination = new Destination("cdi", Collections.EMPTY_LIST, XMap.EMPTY_XMAP, null, null, null);
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
}
