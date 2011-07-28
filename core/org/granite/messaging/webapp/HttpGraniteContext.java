/*
  GRANITE DATA SERVICES
  Copyright (C) 2011 GRANITE DATA SERVICES S.A.S.

  This file is part of Granite Data Services.

  Granite Data Services is free software; you can redistribute it and/or modify
  it under the terms of the GNU Library General Public License as published by
  the Free Software Foundation; either version 2 of the License, or (at your
  option) any later version.

  Granite Data Services is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
  for more details.

  You should have received a copy of the GNU Library General Public License
  along with this library; if not, see <http://www.gnu.org/licenses/>.
*/

package org.granite.messaging.webapp;

import java.util.AbstractMap;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.granite.config.GraniteConfig;
import org.granite.config.flex.ServicesConfig;
import org.granite.context.GraniteContext;
import org.granite.util.TransientReference;
import org.granite.util.TransientReferenceHolder;

/**
 * @author Franck WOLFF
 */
public class HttpGraniteContext extends GraniteContext {

	private static final String SESSION_LOCK_KEY = HttpGraniteContext.class.getName() + ".LOCK";
	
    private final ServletContext context;
    private final HttpServletRequest request;
    private final HttpServletResponse response;

    private InitialisationMap initialisationMap = null;
    private ApplicationMap applicationMap = null;
    private SessionMap sessionMap = null;
    private RequestMap requestMap = null;


    public static HttpGraniteContext createThreadIntance(
        GraniteConfig graniteConfig,
        ServicesConfig servicesConfig,
        ServletContext context,
        HttpServletRequest request,
        HttpServletResponse response) {

        HttpGraniteContext graniteContext = new HttpGraniteContext(graniteConfig, servicesConfig, context, request, response);
        setCurrentInstance(graniteContext);
        return graniteContext;
    }


    private HttpGraniteContext(
        GraniteConfig graniteConfig,
        ServicesConfig servicesConfig,
        ServletContext context,
        HttpServletRequest request,
        HttpServletResponse response) {

        super(graniteConfig, servicesConfig);
        this.context = context;
        this.request = request;
        this.response = response;
    }

    public ServletContext getServletContext() {
        return context;
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    public HttpServletResponse getResponse() {
        return response;
    }

    public HttpSession getSession(boolean create) {
        return request.getSession(create);
    }

    public HttpSession getSession() {
        return request.getSession(true);
    }

    @Override
	public synchronized Object getSessionLock() {
		Object lock = request.getSession(true).getAttribute(SESSION_LOCK_KEY);
		if (lock == null) {
			lock = new Boolean(true);
			request.getSession(true).setAttribute(SESSION_LOCK_KEY, lock);
		}
		return lock;
	}


	@Override
    public Map<String, String> getInitialisationMap() {
        if (initialisationMap == null)
            initialisationMap = new InitialisationMap(context);
        return initialisationMap;
    }

    @Override
    public Map<String, Object> getApplicationMap() {
        if (applicationMap == null)
            applicationMap = new ApplicationMap(context);
        return applicationMap;
    }

	@Override
    public Map<String, Object> getSessionMap() {
		return getSessionMap(true);
    }
    @Override
	public Map<String, Object> getSessionMap(boolean create) {
        if (sessionMap == null && (create || request.getSession(false) != null))
            sessionMap = new SessionMap(request);
        return sessionMap;
	}

    @Override
    public Map<String, Object> getRequestMap() {
        if (requestMap == null)
            requestMap = new RequestMap(request);
        return requestMap;
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
        return request.getSession(true);
    }
}

class RequestMap extends BaseContextMap<String, Object> {

    private HttpServletRequest request = null;

    RequestMap(HttpServletRequest request) {
        if (request == null)
            throw new NullPointerException("request is null");
        this.request = request;
    }

    @Override
    public Object get(Object key) {
        if (!(key instanceof String))
            return null;
        return request.getAttribute(key.toString());
    }

    @Override
    public Object put(String key, Object value) {
        if (key == null)
            throw new IllegalArgumentException(KEY_STRING_ERROR + key);
        Object result = request.getAttribute(key);
        request.setAttribute(key, value);
        return result;
    }

    @Override
    public Object remove(Object key) {
        if (!(key instanceof String))
            return null;
        Object result = request.getAttribute(key.toString());
        request.removeAttribute(key.toString());
        return result;
    }

    @Override
    public Set<Map.Entry<String, Object>> entrySet() {
        Set<Map.Entry<String, Object>> entries = new HashSet<Map.Entry<String, Object>>();
        for (Enumeration<?> e = request.getAttributeNames(); e.hasMoreElements();) {
            String key = (String)e.nextElement();
            entries.add(new Entry<String, Object>(key, request.getAttribute(key)));
        }
        return entries;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof RequestMap))
            return false;
        return super.equals(obj);
    }
}
