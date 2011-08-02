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

package org.granite.messaging.service.security;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.security.Principal;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpSession;

import org.apache.catalina.Context;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.Realm;
import org.apache.catalina.Server;
import org.apache.catalina.ServerFactory;
import org.apache.catalina.Service;
import org.apache.catalina.Session;
import org.apache.catalina.authenticator.Constants;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.RequestFacade;
import org.granite.context.GraniteContext;
import org.granite.messaging.webapp.HttpGraniteContext;

/**
 * @author Franck WOLFF
 */
public class TomcatSecurityService extends AbstractSecurityService {

    private final Field requestField;
    private Engine engine = null;

    public TomcatSecurityService() {
        super();
        try {
            // We need to access the org.apache.catalina.connector.Request field from
            // a org.apache.catalina.connector.RequestFacade. Unfortunately there is
            // no public getter for this field (and I don't want to create a Valve)...
            requestField = RequestFacade.class.getDeclaredField("request");
            requestField.setAccessible(true);
        } catch (Exception e) {
            throw new RuntimeException("Could not get 'request' field in Tomcat RequestFacade", e);
        }
    }

    protected Field getRequestField() {
        return requestField;
    }

    protected Engine getEngine() {
        return engine;
    }

	public void configure(Map<String, String> params) {
        String serviceId = params.get("service");

        Server server = ServerFactory.getServer();
        if (server == null)
            throw new NullPointerException("Could not get Tomcat server");

        Service service = null;
        if (serviceId != null)
            service = server.findService(serviceId);
        else {
            Service[] services = server.findServices();
            if (services != null && services.length > 0)
                service = services[0];
        }
        if (service == null)
            throw new NullPointerException("Could not find Tomcat service for: " + (serviceId != null ? serviceId : "(default)"));

        engine = (Engine)service.getContainer();
        if (engine == null)
            throw new NullPointerException("Could not find Tomcat container for: " + (serviceId != null ? serviceId : "(default)"));
    }

    public void login(Object credentials) throws SecurityServiceException {
        String[] decoded = decodeBase64Credentials(credentials);

        HttpGraniteContext context = (HttpGraniteContext)GraniteContext.getCurrentInstance();
        HttpServletRequest httpRequest = context.getRequest();
        Realm realm = getRealm(httpRequest);

        Principal principal = realm.authenticate(decoded[0], decoded[1]);
        if (principal == null)
            throw SecurityServiceException.newInvalidCredentialsException("Wrong username or password");

        Request request = getRequest(httpRequest);
        request.setAuthType(AUTH_TYPE);
        request.setUserPrincipal(principal);

        Session session = request.getSessionInternal(true);
        session.setAuthType(AUTH_TYPE);
        session.setPrincipal(principal);
        session.setNote(Constants.SESS_USERNAME_NOTE, decoded[0]);
        session.setNote(Constants.SESS_PASSWORD_NOTE, decoded[1]);
        
    	endLogin(credentials);
    }

    public Object authorize(AbstractSecurityContext context) throws Exception {

        startAuthorization(context);
        
        HttpGraniteContext graniteContext = (HttpGraniteContext)GraniteContext.getCurrentInstance();
        HttpServletRequest httpRequest = graniteContext.getRequest();
        Request request = getRequest(httpRequest);
        Session session = request.getSessionInternal(false);
        
        Principal principal = null;
        if (session != null) {
            request.setAuthType(session.getAuthType());
        	principal = session.getPrincipal();
        	if (principal == null && tryRelogin())
        		principal = session.getPrincipal();
        }

        request.setUserPrincipal(principal);

        if (context.getDestination().isSecured()) {
            if (principal == null) {
                if (httpRequest.getRequestedSessionId() != null) {
                    HttpSession httpSession = httpRequest.getSession(false);
                    if (httpSession == null || httpRequest.getRequestedSessionId().equals(httpSession.getId()))
                        throw SecurityServiceException.newSessionExpiredException("Session expired");
                }
                throw SecurityServiceException.newNotLoggedInException("User not logged in");
            }

            Realm realm = getRealm(httpRequest);
            boolean accessDenied = true;
            for (String role : context.getDestination().getRoles()) {
                if (realm.hasRole(principal, role)) {
                    accessDenied = false;
                    break;
                }
            }
            if (accessDenied)
                throw SecurityServiceException.newAccessDeniedException("User not in required role");
        }

        try {
            return endAuthorization(context);
        } catch (InvocationTargetException e) {
            for (Throwable t = e; t != null; t = t.getCause()) {
                // Don't create a dependency to javax.ejb in SecurityService...
                if (t instanceof SecurityException ||
                    "javax.ejb.EJBAccessException".equals(t.getClass().getName()))
                    throw SecurityServiceException.newAccessDeniedException(t.getMessage());
            }
            throw e;
        }
    }

    public void logout() throws SecurityServiceException {
        HttpGraniteContext context = (HttpGraniteContext)GraniteContext.getCurrentInstance();

        Session session = getSession(context.getRequest(), false);
        if (session != null && session.getPrincipal() != null) {
            session.setAuthType(null);
            session.setPrincipal(null);
            session.removeNote(Constants.SESS_USERNAME_NOTE);
            session.removeNote(Constants.SESS_PASSWORD_NOTE);
            
            endLogout();
            
            session.expire();
        }
    }

    protected Principal getPrincipal(HttpServletRequest httpRequest) {
        Request request = getRequest(httpRequest);
        Session session = request.getSessionInternal(false);
        return (session != null ? session.getPrincipal() : null);
    }

    protected Session getSession(HttpServletRequest httpRequest, boolean create) {
        Request request = getRequest(httpRequest);
        return request.getSessionInternal(create);
    }

    protected Request getRequest(HttpServletRequest request) {
        while (request instanceof HttpServletRequestWrapper)
            request = (HttpServletRequest)((HttpServletRequestWrapper)request).getRequest();
        try {
            return (Request)requestField.get(request);
        } catch (Exception e) {
            throw new RuntimeException("Could not get tomcat request", e);
        }
    }

    protected Context getContext(HttpServletRequest request) {
        String serverName = request.getServerName();
        String contextPath = request.getContextPath();

        Host host = (Host)engine.findChild(serverName);
        if (host == null) {
            // if it cannot find host, then use the default host.
            host = (Host)engine.findChild(engine.getDefaultHost());
            if (host == null)
                throw new NullPointerException("Could not find Tomcat host for: " + serverName + " or: " + engine.getDefaultHost());
        }

        Context context = (Context)host.findChild(contextPath);
        if (context == null)
            throw new NullPointerException("Could not find Tomcat context for: " + contextPath);
        return context;
    }

    protected Realm getRealm(HttpServletRequest request) {
        Context context = getContext(request);
        Realm realm = context.getRealm();
        if (realm == null)
            throw new NullPointerException("Could not find Tomcat realm for: " + context.getPath());

        return realm;
    }
}
