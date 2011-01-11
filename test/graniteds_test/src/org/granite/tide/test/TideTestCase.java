package org.granite.tide.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.granite.config.GraniteConfig;
import org.granite.config.ServletGraniteConfig;
import org.granite.config.flex.Destination;
import org.granite.config.flex.ServicesConfig;
import org.granite.config.flex.ServletServicesConfig;
import org.granite.context.GraniteContext;
import org.granite.gravity.AsyncChannelRunner;
import org.granite.gravity.Channel;
import org.granite.gravity.Gravity;
import org.granite.gravity.GravityConfig;
import org.granite.gravity.adapters.ServiceAdapter;
import org.granite.messaging.webapp.HttpGraniteContext;
import org.granite.seam21.Seam21Interceptor;
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
import flex.messaging.messages.AsyncMessage;
import flex.messaging.messages.Message;
import flex.messaging.messages.RemotingMessage;


public class TideTestCase {
    
    private SeamServiceInvoker invoker = null;
    private Seam21Interceptor interceptor = new Seam21Interceptor();
    private Message requestMessage = new RemotingMessage();
    private Message responseMessage = new AcknowledgeMessage();
    private MockGravity mockGravity = new MockGravity();
    
    
    @Before
    public void setUp() throws Exception {
        MockServletContext servletContext = new MockServletContext();
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
    
    @After
    public void tearDown() throws Exception {
        interceptor.after(requestMessage, responseMessage);
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
    }
    
    
    public static class MockGravity implements Gravity {
    	
    	private Message lastMessage;
    	
    	public Message getLastMessage() {
    		return lastMessage;
    	}
    	
		public boolean access(String arg0) {
			return false;
		}

		public boolean cancel(AsyncChannelRunner arg0) {
			return false;
		}

		public void execute(AsyncChannelRunner arg0) {
		}

		public Channel getChannel(String channelId) {
			return null;
		}

		public GraniteConfig getGraniteConfig() {
			return null;
		}

		public GravityConfig getGravityConfig() {
			return null;
		}

		public ServiceAdapter getServiceAdapter(String arg0, String arg1) {
			return null;
		}

		public ServicesConfig getServicesConfig() {
			return null;
		}

		public Message handleMessage(Message arg0) {
			return null;
		}

		public Message handleMessage(Message arg0, boolean arg1) {
			return null;
		}

		public GraniteContext initThread() {
			return null;
		}

		public boolean isStarted() {
			return false;
		}

		public Message publishMessage(AsyncMessage message) {
			lastMessage = message;
			return null;
		}

		public Message publishMessage(Channel channelId, AsyncMessage message) {
			lastMessage = message;
			return null;
		}

		public void reconfigure(GravityConfig arg0, GraniteConfig arg1) {
		}

		public void releaseThread() {
		}

		public Channel removeChannel(String arg0) {
			return null;
		}

		public void start() throws Exception {
		}

		public void stop() throws Exception {
		}

		public void stop(boolean arg0) throws Exception {
		}
    };
}
