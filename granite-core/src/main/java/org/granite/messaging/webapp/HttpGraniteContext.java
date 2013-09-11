/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of Granite Data Services.
 *
 *   Granite Data Services is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU Library General Public License as published by
 *   the Free Software Foundation; either version 2 of the License, or (at your
 *   option) any later version.
 *
 *   Granite Data Services is distributed in the hope that it will be useful, but
 *   WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *   FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
 *   for more details.
 *
 *   You should have received a copy of the GNU Library General Public License
 *   along with this library; if not, see <http://www.gnu.org/licenses/>.
 */

package org.granite.messaging.webapp;

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

/**
 * @author Franck WOLFF
 */
public class HttpGraniteContext extends ServletGraniteContext {

	private static final String SESSION_LOCK_KEY = HttpGraniteContext.class.getName() + ".LOCK";
	
    private final HttpServletRequest request;
    private final HttpServletResponse response;

    private RequestMap requestMap = null;


    public static HttpGraniteContext createThreadIntance(
        GraniteConfig graniteConfig,
        ServicesConfig servicesConfig,
        ServletContext servletContext,
        HttpServletRequest request,
        HttpServletResponse response) {

        HttpGraniteContext graniteContext = new HttpGraniteContext(graniteConfig, servicesConfig, servletContext, request, response);
        setCurrentInstance(graniteContext);
        return graniteContext;
    }


    protected HttpGraniteContext(
        GraniteConfig graniteConfig,
        ServicesConfig servicesConfig,
        ServletContext servletContext,
        HttpServletRequest request,
        HttpServletResponse response) {
    	
        super(graniteConfig, servicesConfig, servletContext, (String)null, request.getHeader("GDSClientType"));
        this.request = request;
        this.response = response;
    }

    @Override
    public HttpServletRequest getRequest() {
        return request;
    }

    @Override
    public HttpServletResponse getResponse() {
        return response;
    }
    
    @Override
    public String getSessionId() {
    	return request.getSession(false) != null ? request.getSession().getId() : null;
    }

    @Override
    public HttpSession getSession(boolean create) {
        return request.getSession(create);
    }

    @Override
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
