package org.granite.test.tide.ejb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.granite.config.GraniteConfig;
import org.granite.config.ServletGraniteConfig;
import org.granite.config.api.Configuration;
import org.granite.config.api.internal.ConfigurationImpl;
import org.granite.config.flex.ServicesConfig;
import org.granite.config.flex.ServletServicesConfig;
import org.granite.gravity.Gravity;
import org.granite.messaging.webapp.HttpGraniteContext;
import org.granite.test.gravity.MockGravity;
import org.granite.test.tide.MockHttpServletRequest;
import org.granite.test.tide.MockHttpServletResponse;
import org.granite.test.tide.MockHttpSession;
import org.granite.test.tide.MockServletContext;
import org.granite.test.tide.ejb.service.Hello2Service;
import org.granite.test.tide.ejb.service.Hello2ServiceBean;
import org.granite.test.tide.ejb.service.HelloMerge2Service;
import org.granite.test.tide.ejb.service.HelloMerge2ServiceBean;
import org.granite.test.tide.ejb.service.HelloMerge3Service;
import org.granite.test.tide.ejb.service.HelloMerge3ServiceBean;
import org.granite.test.tide.ejb.service.HelloMerge4Service;
import org.granite.test.tide.ejb.service.HelloMerge4ServiceBean;
import org.granite.test.tide.ejb.service.HelloMergeService;
import org.granite.test.tide.ejb.service.HelloMergeServiceBean;
import org.granite.test.tide.ejb.service.HelloServiceBean;
import org.granite.tide.ejb.EjbServiceFactory;
import org.granite.tide.invocation.ContextResult;
import org.granite.tide.invocation.ContextUpdate;
import org.granite.tide.invocation.InvocationCall;
import org.granite.tide.invocation.InvocationResult;
import org.granite.util.XMap;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.After;
import org.junit.Before;

import flex.messaging.messages.Message;
import flex.messaging.messages.RemotingMessage;


public class AbstractTideTestCase {
    
	private ServletContext servletContext = null;
	private EJBContainer ejbContainer = null;
    private EjbServiceFactory ejbServiceFactory = null;
    private HttpServletRequest request = null;
    private HttpServletResponse response = null;
    private MockGravity mockGravity = new MockGravity();
    
    
    @Before
    public void setUp() throws Exception {
    	servletContext = initServletContext();
    	   
		final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "tests.jar")
			.addClasses(HelloServiceBean.class, Hello2Service.class, Hello2ServiceBean.class)
			.addClasses(HelloMergeService.class, HelloMergeServiceBean.class)
			.addClasses(HelloMerge2Service.class, HelloMerge2ServiceBean.class)
			.addClasses(HelloMerge3Service.class, HelloMerge3ServiceBean.class)
			.addClasses(HelloMerge4Service.class, HelloMerge4ServiceBean.class)
			.addAsManifestResource(EmptyAsset.INSTANCE, "services-config.properties");
		
		String ejbContainerClassName = System.getProperty("ejb.container.className");
		ejbContainer = (EJBContainer)Class.forName(ejbContainerClassName).newInstance();
		ejbContainer.start(archive);
		
        servletContext.setAttribute(Gravity.class.getName(), mockGravity);
        
        MockHttpSession session = new MockHttpSession("TEST$SESSION", servletContext);
        request = new MockHttpServletRequest(session);
        response = new MockHttpServletResponse();        
        
        Configuration cfg = new ConfigurationImpl();
        cfg.setFlexServicesConfig("/WEB-INF/flex/services-config-ejb.xml");
        cfg.setGraniteConfig("/WEB-INF/granite/granite-config-ejb.xml");
        servletContext.setAttribute(ServletGraniteConfig.GRANITE_CONFIG_CONFIGURATION_KEY, cfg);
        GraniteConfig graniteConfig = ServletGraniteConfig.loadConfig(servletContext);
        ServicesConfig servicesConfig = ServletServicesConfig.loadConfig(servletContext);
        HttpGraniteContext.createThreadIntance(graniteConfig, servicesConfig, servletContext, request, response);
        
        ejbServiceFactory = new EjbServiceFactory();
        XMap props = new XMap();
        props.put("lookup", "java:global/tests/{capitalized.component.name}Bean");
        ejbServiceFactory.configure(props);
        ejbServiceFactory.setInitialContext(ejbContainer.getInitialContext());
    }
    
    protected ServletContext initServletContext() {
    	return new MockServletContext();
    }
    
    @After
    public void tearDown() throws Exception {
    	ejbContainer.stop();
    	ejbContainer = null;
    }
    
    
    protected Message getLastMessage() {
    	return mockGravity.getLastMessage();
    }
    
    protected InvocationResult invokeComponent(String componentName, String componentClassName, String operation, Object[] params) {
        return invokeComponent(componentName, componentClassName, operation, params, null, null, null);
    }
    
    protected InvocationResult invokeComponent(String componentName, String componentClassName, String operation, Object[] params, Object[] updates, String[] results, String conversationId) {
    	return invokeComponent(componentName, componentClassName, operation, params, null, updates, results, conversationId);
    }
    
    protected InvocationResult invokeComponent(String componentName, String componentClassName, String operation, Object[] params, String[] listeners, Object[] updates, String[] results, String conversationId) {
        RemotingMessage callMessage = new RemotingMessage();
        callMessage.setDestination("ejb");
        callMessage.setOperation("invokeComponent");
        Object[] args = new Object[5];
        args[0] = componentName;
        args[1] = componentClassName;
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
        return (InvocationResult)ejbServiceFactory.getServiceInstance(callMessage).invoke(callMessage);
    }
}
