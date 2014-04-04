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
package org.granite.messaging.service.security;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.security.Principal;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpSession;

import org.apache.catalina.Context;
import org.apache.catalina.Realm;
import org.apache.catalina.Session;
import org.apache.catalina.authenticator.Constants;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.RequestFacade;
import org.granite.context.GraniteContext;
import org.granite.messaging.webapp.HttpGraniteContext;
import org.granite.messaging.webapp.ServletGraniteContext;

/**
 * @author Franck WOLFF
 */
public class Tomcat7SecurityService extends AbstractSecurityService {

    private final Field requestField;

    public Tomcat7SecurityService() {
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


    public void configure(Map<String, String> params) {
    }


    @Override
    public void prelogin(HttpSession session, Object httpRequest, String servletName) {
        if (session == null) // Cannot prelogin() without a session
            return;

        if (session.getAttribute(AuthenticationContext.class.getName()) instanceof Tomcat7AuthenticationContext)
            return;

        if (httpRequest.getClass().getName().equals("org.apache.tomcat.websocket.server.WsHandshakeRequest")) {
            try {
                Field f = httpRequest.getClass().getDeclaredField("request");
                f.setAccessible(true);
                httpRequest = f.get(httpRequest);
            }
            catch (Exception e) {
                throw new RuntimeException("Could not unwrap Tomcat request from ws handshake", e);
            }
        }
        Request request = getRequest((HttpServletRequest)httpRequest);
        Realm realm = getRealm(request);

        Tomcat7AuthenticationContext authorizationContext = new Tomcat7AuthenticationContext(realm);
        session.setAttribute(AuthenticationContext.class.getName(), authorizationContext);
    }

    
    public Principal login(Object credentials, String charset) throws SecurityServiceException {
        String[] decoded = decodeBase64Credentials(credentials, charset);

        ServletGraniteContext graniteContext = (ServletGraniteContext)GraniteContext.getCurrentInstance();
        Principal principal = null;
        Request request = null;

        if (graniteContext instanceof HttpGraniteContext) {
            HttpServletRequest httpRequest = graniteContext.getRequest();
            request = getRequest(httpRequest);
            Realm realm = getRealm(request);

            Tomcat7AuthenticationContext authenticationContext = new Tomcat7AuthenticationContext(realm);
            principal = authenticationContext.authenticate(decoded[0], decoded[1]);
            if (principal != null)
                graniteContext.getSession().setAttribute(AuthenticationContext.class.getName(), authenticationContext);
        }
        else {
            AuthenticationContext authenticationContext = (AuthenticationContext)graniteContext.getSession().getAttribute(AuthenticationContext.class.getName());
            if (authenticationContext != null)
                principal = authenticationContext.authenticate(decoded[0], decoded[1]);
        }

        if (principal == null)
            throw SecurityServiceException.newInvalidCredentialsException("Wrong username or password");

        if (graniteContext instanceof HttpGraniteContext) {
            request.setAuthType(AUTH_TYPE);
            request.setUserPrincipal(principal);

            Session session = request.getSessionInternal();
            session.setAuthType(AUTH_TYPE);
            session.setPrincipal(principal);
            session.setNote(Constants.SESS_USERNAME_NOTE, decoded[0]);
            session.setNote(Constants.SESS_PASSWORD_NOTE, decoded[1]);
        }

        endLogin(credentials, charset);

        return principal;
    }

    public Object authorize(AbstractSecurityContext context) throws Exception {

        startAuthorization(context);

        ServletGraniteContext graniteContext = (ServletGraniteContext)GraniteContext.getCurrentInstance();
        HttpServletRequest httpRequest = null;
        AuthenticationContext authenticationContext = null;
        Principal principal = null;

        if (graniteContext instanceof HttpGraniteContext) {
            httpRequest = graniteContext.getRequest();
            Request request = getRequest(httpRequest);
            Session session = request.getSessionInternal(false);

            if (session != null) {
                request.setAuthType(session.getAuthType());
                principal = session.getPrincipal();
                if (principal == null && tryRelogin())
                    principal = session.getPrincipal();
            }
            request.setUserPrincipal(principal);
        }
        else {
            HttpSession session = graniteContext.getSession(false);
            if (session != null) {
                authenticationContext = (AuthenticationContext)session.getAttribute(AuthenticationContext.class.getName());
                if (authenticationContext != null)
                    principal = authenticationContext.getPrincipal();
            }
        }

        if (context.getDestination().isSecured()) {
            if (principal == null) {
                if (httpRequest != null && httpRequest.getRequestedSessionId() != null) {
                    HttpSession httpSession = httpRequest.getSession(false);
                    if (httpSession == null || !httpRequest.getRequestedSessionId().equals(httpSession.getId()))
                        throw SecurityServiceException.newSessionExpiredException("Session expired");
                }
                throw SecurityServiceException.newNotLoggedInException("User not logged in");
            }

            if (httpRequest == null && authenticationContext == null)
                throw SecurityServiceException.newNotLoggedInException("No authorization context");

            boolean accessDenied = true;
            for (String role : context.getDestination().getRoles()) {
                if (httpRequest != null && httpRequest.isUserInRole(role)) {
                    accessDenied = false;
                    break;
                }
                if (authenticationContext != null && authenticationContext.isUserInRole(role)) {
                    accessDenied = false;
                    break;
                }
            }
            if (accessDenied)
                throw SecurityServiceException.newAccessDeniedException("User not in required role");
        }

        try {
            return endAuthorization(context);
        } 
        catch (InvocationTargetException e) {
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
        ServletGraniteContext graniteContext = (ServletGraniteContext)GraniteContext.getCurrentInstance();
        if (graniteContext instanceof HttpGraniteContext) {
            Session session = getSession(graniteContext.getRequest(), false);
            if (session != null && session.getPrincipal() != null) {
                session.setAuthType(null);
                session.setPrincipal(null);
                session.removeNote(Constants.SESS_USERNAME_NOTE);
                session.removeNote(Constants.SESS_PASSWORD_NOTE);

                endLogout();

                session.expire();
            }
        }
        else {
            HttpSession session = graniteContext.getSession();
            if (session != null) {
                session.removeAttribute(AuthenticationContext.class.getName());

                endLogout();

                session.invalidate();
            }
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
        }
        catch (Exception e) {
            throw new RuntimeException("Could not get tomcat request", e);
        }
    }

    protected Realm getRealm(Request request) {
        String serverName = request.getServerName();
        String contextPath = request.getContextPath();
        
        Context context = request.getContext();
        if (context == null)
            throw new NullPointerException("Could not find Tomcat context for: " + contextPath);
        Realm realm = context.getRealm();
        if (realm == null)
            throw new NullPointerException("Could not find Tomcat realm for: " + serverName + "" + contextPath);

        return realm;
    }


    public static class Tomcat7AuthenticationContext implements AuthenticationContext {
    	
		private static final long serialVersionUID = 1L;

        private transient final Realm realm;
        private transient Principal principal;
        
        public Tomcat7AuthenticationContext(Realm realm) {
            this.realm = realm;
        }

        public Principal authenticate(String username, String password) {
        	if (realm == null)
        		throw SecurityServiceException.newAuthenticationFailedException("Invalid authentication");
        	
            principal = realm.authenticate(username, password);
            return principal;
        }

        public Principal getPrincipal() {
            return principal;
        }

        public boolean isUserInRole(String role) {
            return realm.hasRole(null, principal, role);
        }

        public void logout() {
        }
    }

}
