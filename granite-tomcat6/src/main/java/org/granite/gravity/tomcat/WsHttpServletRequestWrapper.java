/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.granite.gravity.tomcat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import org.apache.catalina.websocket.Constants;
import org.apache.tomcat.util.res.StringManager;

/**
 * Wrapper for the HttpServletRequest object that allows the underlying request
 * object to be invalidated.
 */
public class WsHttpServletRequestWrapper implements HttpServletRequest {

    private static final StringManager sm = StringManager.getManager(Constants.Package);

    private HttpServletRequest request;

    public WsHttpServletRequestWrapper(HttpServletRequest request) {
        this.request = request;
    }

    private HttpServletRequest getRequest() {
        if (request == null) {
            throw new IllegalStateException(sm.getString("wrapper.invalid"));
        }
        return request;
    }

    protected void invalidate() {
        request = null;
    }

    public Object getAttribute(String name) {
        return getRequest().getAttribute(name);
    }

    public Enumeration<String> getAttributeNames() {
        return getRequest().getAttributeNames();
    }

    public String getCharacterEncoding() {
        return getRequest().getCharacterEncoding();
    }

    public void setCharacterEncoding(String env)
            throws UnsupportedEncodingException {
        getRequest().setCharacterEncoding(env);
    }

    public int getContentLength() {
        return getRequest().getContentLength();
    }

    public String getContentType() {
        return getRequest().getContentType();
    }

    public ServletInputStream getInputStream() throws IOException {
        return getRequest().getInputStream();
    }

    public String getParameter(String name) {
        return getRequest().getParameter(name);
    }

    public Enumeration<String> getParameterNames() {
        return getRequest().getParameterNames();
    }

    public String[] getParameterValues(String name) {
        return getRequest().getParameterValues(name);
    }

    public Map<String, String[]> getParameterMap() {
        return getRequest().getParameterMap();
    }

    public String getProtocol() {
        return getRequest().getProtocol();
    }

    public String getScheme() {
        return getRequest().getScheme();
    }

    public String getServerName() {
        return getRequest().getServerName();
    }

    public int getServerPort() {
        return getRequest().getServerPort();
    }

    public BufferedReader getReader() throws IOException {
        return getRequest().getReader();
    }

    public String getRemoteAddr() {
        return getRequest().getRemoteAddr();
    }

    public String getRemoteHost() {
        return getRequest().getRemoteHost();
    }

    public void setAttribute(String name, Object o) {
        getRequest().setAttribute(name, o);
    }

    public void removeAttribute(String name) {
        getRequest().removeAttribute(name);
    }

    public Locale getLocale() {
        return getRequest().getLocale();
    }

    public Enumeration<Locale> getLocales() {
        return getRequest().getLocales();
    }

    public boolean isSecure() {
        return getRequest().isSecure();
    }

    public RequestDispatcher getRequestDispatcher(String path) {
        return getRequest().getRequestDispatcher(path);
    }

    @Deprecated
    public String getRealPath(String path) {
        return getRequest().getRealPath(path);
    }

    public int getRemotePort() {
        return getRequest().getRemotePort();
    }

    public String getLocalName() {
        return getRequest().getLocalName();
    }

    public String getLocalAddr() {
        return getRequest().getLocalAddr();
    }

    public int getLocalPort() {
        return getRequest().getLocalPort();
    }

    public ServletContext getServletContext() {
        return getRequest().getServletContext();
    }

    public AsyncContext startAsync() throws IllegalStateException {
        return getRequest().startAsync();
    }

    public AsyncContext startAsync(ServletRequest servletRequest,
            ServletResponse servletResponse) throws IllegalStateException {
        return getRequest().startAsync(servletRequest, servletResponse);
    }

    public boolean isAsyncStarted() {
        return getRequest().isAsyncStarted();
    }

    public boolean isAsyncSupported() {
        return getRequest().isAsyncSupported();
    }

    public AsyncContext getAsyncContext() {
        return getRequest().getAsyncContext();
    }

    public DispatcherType getDispatcherType() {
        return getRequest().getDispatcherType();
    }

    public String getAuthType() {
        return getRequest().getAuthType();
    }

    public Cookie[] getCookies() {
        return getRequest().getCookies();
    }

    public long getDateHeader(String name) {
        return getRequest().getDateHeader(name);
    }

    public String getHeader(String name) {
        return getRequest().getHeader(name);
    }

    public Enumeration<String> getHeaders(String name) {
        return getRequest().getHeaders(name);
    }

    public Enumeration<String> getHeaderNames() {
        return getRequest().getHeaderNames();
    }

    public int getIntHeader(String name) {
        return getRequest().getIntHeader(name);
    }

    public String getMethod() {
        return getRequest().getMethod();
    }

    public String getPathInfo() {
        return getRequest().getPathInfo();
    }

    public String getPathTranslated() {
        return getRequest().getPathTranslated();
    }

    public String getContextPath() {
        return getRequest().getContextPath();
    }

    public String getQueryString() {
        return getRequest().getQueryString();
    }

    public String getRemoteUser() {
        return getRequest().getRemoteUser();
    }

    public boolean isUserInRole(String role) {
        return getRequest().isUserInRole(role);
    }

    public Principal getUserPrincipal() {
        return getRequest().getUserPrincipal();
    }

    public String getRequestedSessionId() {
        return getRequest().getRequestedSessionId();
    }

    public String getRequestURI() {
        return getRequest().getRequestURI();
    }

    public StringBuffer getRequestURL() {
        return getRequest().getRequestURL();
    }

    public String getServletPath() {
        return getRequest().getServletPath();
    }

    public HttpSession getSession(boolean create) {
        return getRequest().getSession(create);
    }

    public HttpSession getSession() {
        return getRequest().getSession();
    }

    public boolean isRequestedSessionIdValid() {
        return getRequest().isRequestedSessionIdValid();
    }

    public boolean isRequestedSessionIdFromCookie() {
        return getRequest().isRequestedSessionIdFromCookie();
    }

    public boolean isRequestedSessionIdFromURL() {
        return getRequest().isRequestedSessionIdFromURL();
    }

    @Deprecated
    public boolean isRequestedSessionIdFromUrl() {
        return getRequest().isRequestedSessionIdFromUrl();
    }

    public boolean authenticate(HttpServletResponse response)
            throws IOException, ServletException {
        return getRequest().authenticate(response);
    }

    public void login(String username, String password) throws ServletException {
        getRequest().login(username, password);
    }

    public void logout() throws ServletException {
        getRequest().logout();
    }

    public Collection<Part> getParts() throws IOException, ServletException {
        return getRequest().getParts();
    }

    public Part getPart(String name) throws IOException, ServletException {
        return getRequest().getPart(name);
    }
}
