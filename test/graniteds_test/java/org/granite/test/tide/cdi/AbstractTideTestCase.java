package org.granite.test.tide.cdi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.enterprise.inject.spi.BeanManager;
import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.FilterRegistration.Dynamic;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRegistration;
import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletResponse;
import javax.servlet.SessionCookieConfig;
import javax.servlet.SessionTrackingMode;
import javax.servlet.descriptor.JspConfigDescriptor;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

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
    
    
    public class MockHttpServletRequest implements HttpServletRequest {
    	
    	private HttpSession session = null;
    	private Map<String, Object> atts = new HashMap<String, Object>();

    	public MockHttpServletRequest(HttpSession session) {
    		this.session = session;
    	}
    	
		@Override
		public AsyncContext getAsyncContext() {
			return null;
		}

		@Override
		public Object getAttribute(String key) {
			return atts.get(key);
		}

		@Override
		public Enumeration<String> getAttributeNames() {
			return null;
		}

		@Override
		public String getCharacterEncoding() {
			return "UTF-8";
		}

		@Override
		public int getContentLength() {
			return 0;
		}

		@Override
		public String getContentType() {
			return null;
		}

		@Override
		public DispatcherType getDispatcherType() {
			return null;
		}

		@Override
		public ServletInputStream getInputStream() throws IOException {
			return null;
		}

		@Override
		public String getLocalAddr() {
			return null;
		}

		@Override
		public String getLocalName() {
			return null;
		}

		@Override
		public int getLocalPort() {
			return 0;
		}

		@Override
		public Locale getLocale() {
			return Locale.US;
		}

		@Override
		public Enumeration<Locale> getLocales() {
			return null;
		}

		@Override
		public String getParameter(String arg0) {
			return null;
		}

		@Override
		public Map<String, String[]> getParameterMap() {
			return Collections.emptyMap();
		}

		@Override
		public Enumeration<String> getParameterNames() {
			return null;
		}

		@Override
		public String[] getParameterValues(String arg0) {
			return null;
		}

		@Override
		public String getProtocol() {
			return "http";
		}

		@Override
		public BufferedReader getReader() throws IOException {
			return null;
		}

		@Override
		public String getRealPath(String arg0) {
			return null;
		}

		@Override
		public String getRemoteAddr() {
			return null;
		}

		@Override
		public String getRemoteHost() {
			return null;
		}

		@Override
		public int getRemotePort() {
			return 0;
		}

		@Override
		public RequestDispatcher getRequestDispatcher(String arg0) {
			return null;
		}

		@Override
		public String getScheme() {
			return null;
		}

		@Override
		public String getServerName() {
			return null;
		}

		@Override
		public int getServerPort() {
			return 80;
		}

		@Override
		public ServletContext getServletContext() {
			return session.getServletContext();
		}

		@Override
		public boolean isAsyncStarted() {
			return false;
		}

		@Override
		public boolean isAsyncSupported() {
			return false;
		}

		@Override
		public boolean isSecure() {
			return false;
		}

		@Override
		public void removeAttribute(String key) {
			atts.remove(key);
		}

		@Override
		public void setAttribute(String key, Object value) {
			atts.put(key, value);			
		}

		@Override
		public void setCharacterEncoding(String arg0) throws UnsupportedEncodingException {
		}

		@Override
		public AsyncContext startAsync() throws IllegalStateException {
			return null;
		}

		@Override
		public AsyncContext startAsync(ServletRequest arg0, ServletResponse arg1) throws IllegalStateException {
			return null;
		}

		@Override
		public boolean authenticate(HttpServletResponse arg0) throws IOException, ServletException {
			return false;
		}

		@Override
		public String getAuthType() {
			return null;
		}

		@Override
		public String getContextPath() {
			return null;
		}

		@Override
		public Cookie[] getCookies() {
			return null;
		}

		@Override
		public long getDateHeader(String arg0) {
			return 0;
		}

		@Override
		public String getHeader(String arg0) {
			return null;
		}

		@Override
		public Enumeration<String> getHeaderNames() {
			return null;
		}

		@Override
		public Enumeration<String> getHeaders(String arg0) {
			return null;
		}

		@Override
		public int getIntHeader(String arg0) {
			return 0;
		}

		@Override
		public String getMethod() {
			return null;
		}

		@Override
		public Part getPart(String arg0) throws IOException, ServletException {
			return null;
		}

		@Override
		public Collection<Part> getParts() throws IOException, ServletException {
			return null;
		}

		@Override
		public String getPathInfo() {
			return null;
		}

		@Override
		public String getPathTranslated() {
			return null;
		}

		@Override
		public String getQueryString() {
			return null;
		}

		@Override
		public String getRemoteUser() {
			return null;
		}

		@Override
		public String getRequestURI() {
			return null;
		}

		@Override
		public StringBuffer getRequestURL() {
			return null;
		}

		@Override
		public String getRequestedSessionId() {
			return null;
		}

		@Override
		public String getServletPath() {
			return null;
		}

		@Override
		public HttpSession getSession() {
			return session;
		}

		@Override
		public HttpSession getSession(boolean arg0) {
			return session;
		}

		@Override
		public Principal getUserPrincipal() {
			return null;
		}

		@Override
		public boolean isRequestedSessionIdFromCookie() {
			return false;
		}

		@Override
		public boolean isRequestedSessionIdFromURL() {
			return false;
		}

		@Override
		public boolean isRequestedSessionIdFromUrl() {
			return false;
		}

		@Override
		public boolean isRequestedSessionIdValid() {
			return false;
		}

		@Override
		public boolean isUserInRole(String arg0) {
			return false;
		}

		@Override
		public void login(String arg0, String arg1) throws ServletException {
		}

		@Override
		public void logout() throws ServletException {
		}
    	
    }
    
    public class MockHttpServletResponse implements HttpServletResponse {

		@Override
		public void flushBuffer() throws IOException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public int getBufferSize() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public String getCharacterEncoding() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getContentType() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Locale getLocale() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public ServletOutputStream getOutputStream() throws IOException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public PrintWriter getWriter() throws IOException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean isCommitted() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void reset() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void resetBuffer() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setBufferSize(int arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setCharacterEncoding(String arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setContentLength(int arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setContentType(String arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setLocale(Locale arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void addCookie(Cookie arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void addDateHeader(String arg0, long arg1) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void addHeader(String arg0, String arg1) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void addIntHeader(String arg0, int arg1) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public boolean containsHeader(String arg0) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public String encodeRedirectURL(String arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String encodeRedirectUrl(String arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String encodeURL(String arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String encodeUrl(String arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getHeader(String arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Collection<String> getHeaderNames() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Collection<String> getHeaders(String arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public int getStatus() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public void sendError(int arg0) throws IOException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void sendError(int arg0, String arg1) throws IOException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void sendRedirect(String arg0) throws IOException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setDateHeader(String arg0, long arg1) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setHeader(String arg0, String arg1) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setIntHeader(String arg0, int arg1) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setStatus(int arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setStatus(int arg0, String arg1) {
			// TODO Auto-generated method stub
			
		}
    	
    }
    
    
    public class MockServletContext implements ServletContext {

    	private Map<String, Object> atts = new HashMap<String, Object>();
    	
		@Override
		public Dynamic addFilter(String arg0, String arg1) {
			return null;
		}

		@Override
		public Dynamic addFilter(String arg0, Filter arg1) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Dynamic addFilter(String arg0, Class<? extends Filter> arg1) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void addListener(String arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public <T extends EventListener> void addListener(T arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void addListener(Class<? extends EventListener> arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public javax.servlet.ServletRegistration.Dynamic addServlet(
				String arg0, String arg1) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public javax.servlet.ServletRegistration.Dynamic addServlet(
				String arg0, Servlet arg1) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public javax.servlet.ServletRegistration.Dynamic addServlet(
				String arg0, Class<? extends Servlet> arg1) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public <T extends Filter> T createFilter(Class<T> arg0)
				throws ServletException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public <T extends EventListener> T createListener(Class<T> arg0)
				throws ServletException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public <T extends Servlet> T createServlet(Class<T> arg0)
				throws ServletException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void declareRoles(String... arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public Object getAttribute(String key) {
			return atts.get(key);
		}

		@Override
		public Enumeration<String> getAttributeNames() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public ClassLoader getClassLoader() {
			return this.getClass().getClassLoader();
		}

		@Override
		public ServletContext getContext(String arg0) {
			return null;
		}

		@Override
		public String getContextPath() {
			return "/test";
		}

		@Override
		public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public int getEffectiveMajorVersion() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public int getEffectiveMinorVersion() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public FilterRegistration getFilterRegistration(String arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getInitParameter(String arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Enumeration<String> getInitParameterNames() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public JspConfigDescriptor getJspConfigDescriptor() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public int getMajorVersion() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public String getMimeType(String arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public int getMinorVersion() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public RequestDispatcher getNamedDispatcher(String arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getRealPath(String arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public RequestDispatcher getRequestDispatcher(String arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public URL getResource(String arg0) throws MalformedURLException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public InputStream getResourceAsStream(String name) {
			return getClass().getResourceAsStream(name);
		}

		@Override
		public Set<String> getResourcePaths(String arg0) {
			return null;
		}

		@Override
		public String getServerInfo() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Servlet getServlet(String arg0) throws ServletException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getServletContextName() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Enumeration<String> getServletNames() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public ServletRegistration getServletRegistration(String arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Map<String, ? extends ServletRegistration> getServletRegistrations() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Enumeration<Servlet> getServlets() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public SessionCookieConfig getSessionCookieConfig() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void log(String arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void log(Exception arg0, String arg1) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void log(String arg0, Throwable arg1) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void removeAttribute(String key) {
			atts.remove(key);			
		}

		@Override
		public void setAttribute(String key, Object value) {
			atts.put(key, value);
			
		}

		@Override
		public boolean setInitParameter(String arg0, String arg1) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void setSessionTrackingModes(Set<SessionTrackingMode> arg0) {
			// TODO Auto-generated method stub
			
		}
    	
    }
}
