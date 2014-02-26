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
package org.granite.messaging.webapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import org.granite.clustering.TransientReference;
import org.granite.clustering.TransientReferenceHolder;
import org.granite.config.GraniteConfig;
import org.granite.config.GraniteConfigListener;
import org.granite.config.flex.ServicesConfig;
import org.granite.context.GraniteContext;

/**
 * @author Franck WOLFF
 */
public class ServletGraniteContext extends GraniteContext {

    private final ServletContext servletContext;

    protected InitialisationMap initialisationMap = null;
    protected ApplicationMap applicationMap = null;
    protected SessionMap sessionMap = null;
    protected HttpServletRequest request = null;
    protected HttpServletResponse response = null;
    protected HttpSession session = null;


    public static ServletGraniteContext createThreadInstance(
        GraniteConfig graniteConfig,
        ServicesConfig servicesConfig,
        ServletContext context,
        String sessionId,
        String clientType) {

        ServletGraniteContext graniteContext = new ServletGraniteContext(graniteConfig, servicesConfig, context, sessionId, clientType);
        setCurrentInstance(graniteContext);
        return graniteContext;
    }
    
    public static ServletGraniteContext createThreadInstance(
        GraniteConfig graniteConfig,
        ServicesConfig servicesConfig,
        ServletContext context,
        HttpSession session,
        String clientType) {

        ServletGraniteContext graniteContext = new ServletGraniteContext(graniteConfig, servicesConfig, context, session, clientType);
        setCurrentInstance(graniteContext);
        return graniteContext;
    }


    protected ServletGraniteContext(
        GraniteConfig graniteConfig,
        ServicesConfig servicesConfig,
        ServletContext servletContext,
        String sessionId,
        String clientType) {

        super(graniteConfig, servicesConfig, sessionId, clientType);
        this.servletContext = servletContext;
    }
    
    protected ServletGraniteContext(
        GraniteConfig graniteConfig,
        ServicesConfig servicesConfig,
        ServletContext servletContext,
        HttpSession session,
        String clientType) {

        super(graniteConfig, servicesConfig, session.getId(), clientType);
        this.servletContext = servletContext;
        this.session = session;
    }

    public ServletContext getServletContext() {
        return servletContext;
    }
    
    public HttpServletRequest getRequest() {
    	if (request == null)
    		request = new BasicRequest();
    	return request;
    }

    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }
    
    public HttpServletResponse getResponse() {
    	if (response == null)
    		response = new BasicResponse();
    	return response;
    }
    
    public HttpSession getSession(boolean create) {
    	return getSession();
    }

    public HttpSession getSession() {
    	if (session != null)
    		return session;
    	
    	if (getSessionId() == null)
    		return null;
    	
    	// Lookup session in session map when using embedded Jetty
    	@SuppressWarnings("unchecked")
		Map<String, HttpSession> sessionMap = (Map<String, HttpSession>)servletContext.getAttribute(GraniteConfigListener.GRANITE_SESSION_MAP);
        return sessionMap != null ? sessionMap.get(getSessionId()) : null;
    }

    @Override
	public Object getSessionLock() {
		return null;
	}


	@Override
    public Map<String, String> getInitialisationMap() {
        if (initialisationMap == null)
            initialisationMap = new InitialisationMap(servletContext);
        return initialisationMap;
    }

    @Override
    public Map<String, Object> getApplicationMap() {
        if (applicationMap == null)
            applicationMap = new ApplicationMap(servletContext);
        return applicationMap;
    }

	@Override
    public Map<String, Object> getSessionMap() {
		return null;
    }
    @Override
	public Map<String, Object> getSessionMap(boolean create) {
        if (sessionMap == null && getSession() != null)
            sessionMap = new SessionMap(getSession());
        return sessionMap;
	}

    @Override
    public Map<String, Object> getRequestMap() {
        return null;
    }
    
    
    private class BasicRequest implements HttpServletRequest {
    	
    	private Map<String, Object> attributes = new HashMap<String, Object>();
    	
    	public ServletContext getServletContext() {
    		return servletContext;
    	}

    	public Object getAttribute(String key) {
    		return attributes.get(key);
    	}

    	public void removeAttribute(String key) {
    		attributes.remove(key);
    	}

    	public void setAttribute(String key, Object value) {
    		attributes.put(key, value);
    	}

    	public Enumeration<String> getAttributeNames() {
    		return new Hashtable<String, Object>(attributes).keys();
    	}

    	public HttpSession getSession() {
    		return ServletGraniteContext.this.getSession();
    	}

    	public HttpSession getSession(boolean create) {
    		return ServletGraniteContext.this.getSession(create);
    	}

    	public String getRequestedSessionId() {
    		return null;
    	}

    	public boolean isRequestedSessionIdFromCookie() {
    		return false;
    	}

    	public boolean isRequestedSessionIdFromURL() {
    		return false;
    	}

    	public boolean isRequestedSessionIdFromUrl() {
    		return false;
    	}

    	public boolean isRequestedSessionIdValid() {
    		return false;
    	}

    	public Principal getUserPrincipal() {
    		return null;
    	}

    	public boolean isUserInRole(String arg0) {
    		return false;
    	}

    	public void login(String arg0, String arg1) throws ServletException {
    	}

    	public void logout() throws ServletException {
    	}
    	
    	public String getCharacterEncoding() {
    		return null;
    	}

    	public int getContentLength() {
    		return 0;
    	}

    	public String getContentType() {
    		return null;
    	}

    	public DispatcherType getDispatcherType() {
    		return null;
    	}

    	public ServletInputStream getInputStream() throws IOException {
    		return null;
    	}

    	public String getLocalAddr() {
    		return null;
    	}

    	public String getLocalName() {
    		return null;
    	}

    	public int getLocalPort() {
    		return 0;
    	}

    	public Locale getLocale() {
    		return null;
    	}

    	public Enumeration<Locale> getLocales() {
    		return null;
    	}

    	public String getParameter(String arg0) {
    		return null;
    	}

    	public Map<String, String[]> getParameterMap() {
    		return null;
    	}

    	public Enumeration<String> getParameterNames() {
    		return null;
    	}

    	public String[] getParameterValues(String arg0) {
    		return null;
    	}

    	public String getProtocol() {
    		return null;
    	}

    	public BufferedReader getReader() throws IOException {
    		return null;
    	}

    	public String getRealPath(String arg0) {
    		return null;
    	}

    	public String getRemoteAddr() {
    		return null;
    	}

    	public String getRemoteHost() {
    		return null;
    	}

    	public int getRemotePort() {
    		return 0;
    	}

    	public RequestDispatcher getRequestDispatcher(String arg0) {
    		return null;
    	}

    	public String getScheme() {
    		return null;
    	}

    	public String getServerName() {
    		return null;
    	}

    	public int getServerPort() {
    		return 0;
    	}

    	public AsyncContext getAsyncContext() {
    		return null;
    	}

    	public boolean isAsyncStarted() {
    		return false;
    	}

    	public boolean isAsyncSupported() {
    		return false;
    	}

    	public boolean isSecure() {
    		return false;
    	}

    	public void setCharacterEncoding(String arg0) throws UnsupportedEncodingException {
    	}

    	public AsyncContext startAsync() throws IllegalStateException {
    		return null;
    	}

    	public AsyncContext startAsync(ServletRequest request, ServletResponse response) throws IllegalStateException {
    		return null;
    	}

    	public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
    		return false;
    	}

    	public String getAuthType() {
    		return null;
    	}

    	public String getContextPath() {
    		return null;
    	}

    	public Cookie[] getCookies() {
    		return null;
    	}

    	public long getDateHeader(String name) {
    		return 0;
    	}

    	public String getHeader(String name) {
    		return null;
    	}

    	public Enumeration<String> getHeaderNames() {
    		return null;
    	}

    	public Enumeration<String> getHeaders(String name) {
    		return null;
    	}

    	public int getIntHeader(String name) {
    		return 0;
    	}

    	public String getMethod() {
    		return null;
    	}

    	public Part getPart(String name) throws IOException, ServletException {
    		return null;
    	}

    	public Collection<Part> getParts() throws IOException, ServletException {
    		return null;
    	}

    	public String getPathInfo() {
    		return null;
    	}

    	public String getPathTranslated() {
    		return null;
    	}

    	public String getQueryString() {
    		return null;
    	}

    	public String getRemoteUser() {
    		return null;
    	}

    	public String getRequestURI() {
    		return null;
    	}

    	public StringBuffer getRequestURL() {
    		return null;
    	}

    	public String getServletPath() {
    		return null;
    	}    
    }
    
    private class BasicResponse implements HttpServletResponse {

		public void flushBuffer() throws IOException {
		}

		public int getBufferSize() {
			return 0;
		}

		public String getCharacterEncoding() {
			return null;
		}

		public String getContentType() {
			return null;
		}

		public Locale getLocale() {
			return null;
		}

		public ServletOutputStream getOutputStream() throws IOException {
			return null;
		}

		public PrintWriter getWriter() throws IOException {
			return null;
		}

		public boolean isCommitted() {
			return false;
		}

		public void reset() {
		}

		public void resetBuffer() {
		}

		public void setBufferSize(int size) {
		}

		public void setCharacterEncoding(String charset) {
		}

		public void setContentLength(int length) {
		}

		public void setContentType(String contentType) {
		}

		public void setLocale(Locale locale) {
		}

		public void addCookie(Cookie cookie) {
		}

		public void addDateHeader(String name, long value) {
		}

		public void addHeader(String name, String value) {
		}

		public void addIntHeader(String name, int value) {
		}

		public boolean containsHeader(String name) {
			return false;
		}

		public String encodeRedirectURL(String url) {
			return null;
		}

		public String encodeRedirectUrl(String url) {
			return null;
		}

		public String encodeURL(String url) {
			return null;
		}

		public String encodeUrl(String url) {
			return null;
		}

		public String getHeader(String name) {
			return null;
		}

		public Collection<String> getHeaderNames() {
			return null;
		}

		public Collection<String> getHeaders(String name) {
			return null;
		}

		public int getStatus() {
			return 0;
		}

		public void sendError(int code) throws IOException {
		}

		public void sendError(int code, String msg) throws IOException {
		}

		public void sendRedirect(String url) throws IOException {
		}

		public void setDateHeader(String name, long value) {
		}

		public void setHeader(String name, String value) {
		}

		public void setIntHeader(String name, int value) {
		}

		public void setStatus(int code) {
		}

		public void setStatus(int code, String msg) {
		}
    	
    }
}


abstract class BaseContextMap<T,U> extends AbstractMap<T,U> {

    protected static final String KEY_STRING_ERROR = "Key should be a non null String: ";

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(Map<? extends T, ? extends U> t) {
        throw new UnsupportedOperationException();
    }

    @Override
    public U remove(Object key) {
        throw new UnsupportedOperationException();
    }

    static class Entry<T,U> implements Map.Entry<T,U> {

        private final T key;
        private final U value;

        Entry(T key, U value) {
            this.key = key;
            this.value = value;
        }

        public T getKey() {
            return key;
        }

        public U getValue() {
            return value;
        }

        public U setValue(U value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int hashCode() {
            return ((key == null ? 0 : key.hashCode()) ^ (value == null ? 0 : value.hashCode()));
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this)
                return true;

            if (obj == null || !(obj instanceof Map.Entry<?, ?>))
                return false;

            Map.Entry<?, ?> input = (Map.Entry<?, ?>)obj;
            Object inputKey = input.getKey();
            Object inputValue = input.getValue();

            if (inputKey == key || (inputKey != null && inputKey.equals(key))) {
                if (inputValue == value || (inputValue != null && inputValue.equals(value)))
                    return true;
            }
            return false;
        }
    }
}

class InitialisationMap extends BaseContextMap<String, String> {

    private ServletContext servletContext = null;

    InitialisationMap(ServletContext servletContext) {
        if (servletContext == null)
            throw new NullPointerException("servletContext is null");
        this.servletContext = servletContext;
    }

    @Override
    public String get(Object key) {
        if (!(key instanceof String))
            return null;
        return servletContext.getInitParameter(key.toString());
    }

    @Override
    public String put(String key, String value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Map.Entry<String, String>> entrySet() {
        Set<Map.Entry<String, String>> entries = new HashSet<Map.Entry<String, String>>();
        for (Enumeration<?> e = servletContext.getInitParameterNames(); e.hasMoreElements();) {
            String key = (String)e.nextElement();
            entries.add(new Entry<String, String>(key, servletContext.getInitParameter(key)));
        }
        return entries;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof InitialisationMap))
            return false;
        return super.equals(obj);
    }
}

class ApplicationMap extends BaseContextMap<String, Object> {

    private ServletContext servletContext = null;

    ApplicationMap(ServletContext servletContext) {
        if (servletContext == null)
            throw new NullPointerException("servletContext is null");
        this.servletContext = servletContext;
    }

    @Override
    public Object get(Object key) {
        if (!(key instanceof String))
            return null;
       return servletContext.getAttribute(key.toString());
    }

    @Override
    public Object put(String key, Object value) {
        if (key == null)
            throw new IllegalArgumentException(KEY_STRING_ERROR + key);
        Object result = servletContext.getAttribute(key);
        servletContext.setAttribute(key, value);
        return (result);
    }

    @Override
    public Object remove(Object key) {
        if (!(key instanceof String))
            return null;
        Object result = servletContext.getAttribute(key.toString());
        servletContext.removeAttribute(key.toString());
        return result;
    }

    @Override
    public Set<Map.Entry<String, Object>> entrySet() {
        Set<Map.Entry<String, Object>> entries = new HashSet<Map.Entry<String, Object>>();
        for (Enumeration<?> e = servletContext.getAttributeNames(); e.hasMoreElements();) {
            String key = (String)e.nextElement();
            entries.add(new Entry<String, Object>(key, servletContext.getAttribute(key)));
        }
        return entries;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof ApplicationMap))
            return false;
        return super.equals(obj);
    }
}

class SessionMap extends BaseContextMap<String, Object> {

    private HttpServletRequest request = null;
    private HttpSession session = null;

    SessionMap(HttpSession session) {
        if (session == null)
            throw new NullPointerException("session is null");
    	this.session = session;
    }
    
    SessionMap(HttpServletRequest request) {
        if (request == null)
            throw new NullPointerException("request is null");
        this.request = request;
    }

    @Override
    public Object get(Object key) {
        if (!(key instanceof String))
            return null;
        Object value = getSession().getAttribute(key.toString());
        if (value instanceof TransientReferenceHolder)
        	return ((TransientReferenceHolder)value).get();
        return value;
    }

    @Override
    public Object put(String key, Object value) {
        if (key == null)
            throw new IllegalArgumentException(KEY_STRING_ERROR + key);
        HttpSession session = getSession();
        Object result = session.getAttribute(key);
        if (result instanceof TransientReferenceHolder)
        	result = ((TransientReferenceHolder)result).get();
        if (value != null && value.getClass().isAnnotationPresent(TransientReference.class))
        	value = new TransientReferenceHolder(value);
        session.setAttribute(key, value);
        return result;
    }

    @Override
    public Object remove(Object key) {
        if (!(key instanceof String))
            return null;
        HttpSession session = getSession();
        Object result = session.getAttribute(key.toString());
        if (result instanceof TransientReferenceHolder)
        	result = ((TransientReferenceHolder)result).get();
        session.removeAttribute(key.toString());
        return result;
    }

    @Override
    public Set<Map.Entry<String, Object>> entrySet() {
        Set<Map.Entry<String, Object>> entries = new HashSet<Map.Entry<String, Object>>();
        HttpSession session = getSession();
        for (Enumeration<?> e = session.getAttributeNames(); e.hasMoreElements(); ) {
            String key = (String)e.nextElement();
            Object value = session.getAttribute(key);
            if (value instanceof TransientReferenceHolder)
            	value = ((TransientReferenceHolder)value).get();
            entries.add(new Entry<String, Object>(key, value));
        }
        return entries;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof SessionMap))
            return false;
        return super.equals(obj);
    }

    private HttpSession getSession() {
    	if (request != null)
    		return request.getSession(true);
    	return session;
    }
}

