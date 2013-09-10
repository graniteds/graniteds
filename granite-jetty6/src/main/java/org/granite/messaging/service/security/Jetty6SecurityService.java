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

import java.lang.reflect.InvocationTargetException;
import java.security.Principal;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.granite.context.GraniteContext;
import org.granite.messaging.webapp.HttpGraniteContext;
import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.security.UserRealm;

/**
 * @author William DRAI
 */
public class Jetty6SecurityService extends AbstractSecurityService {

    private static final String JETTY6_AUTH = "org.granite.messaging.service.security.Jetty6Auth";


    public Jetty6SecurityService() {
        super();
    }


    public void configure(Map<String, String> params) {
    }


    public void login(Object credentials, String charset) throws SecurityServiceException {
        String[] decoded = decodeBase64Credentials(credentials, charset);

        HttpGraniteContext graniteContext = (HttpGraniteContext)GraniteContext.getCurrentInstance();
        HttpServletRequest httpRequest = graniteContext.getRequest();
        Request request = httpRequest instanceof Request ? (Request)httpRequest : HttpConnection.getCurrentConnection().getRequest();
        UserRealm realm = request.getUserRealm();

        Principal principal = realm.authenticate(decoded[0], decoded[1], request);
        if (principal == null) {
            if (request.getSession(false) != null)
            	request.getSession(false).removeAttribute(JETTY6_AUTH);
            throw SecurityServiceException.newInvalidCredentialsException("Wrong username or password");
        }

        request.setAuthType(AUTH_TYPE);
        request.setUserPrincipal(principal);

        request.getSession().setAttribute(JETTY6_AUTH, principal);
        
        endLogin(credentials, charset);
    }


    public Object authorize(AbstractSecurityContext context) throws Exception {

        startAuthorization(context);

        HttpGraniteContext graniteContext = (HttpGraniteContext)GraniteContext.getCurrentInstance();
        HttpServletRequest httpRequest = graniteContext.getRequest();

        boolean reauth = false;
        Principal principal = httpRequest.getUserPrincipal();
        if (principal == null) {
            HttpSession session = httpRequest.getSession(false);
            principal = session != null ? (Principal)session.getAttribute(JETTY6_AUTH) : null;
            reauth = true;
        }

        if (principal == null && tryRelogin()) {
        	principal = httpRequest.getUserPrincipal();
        	reauth = false;
        }
        
        if (principal == null) {
            if (httpRequest.getRequestedSessionId() != null) {
                HttpSession httpSession = httpRequest.getSession(false);
                if (httpSession == null || httpRequest.getRequestedSessionId().equals(httpSession.getId()))
                    throw SecurityServiceException.newSessionExpiredException("Session expired");
            }
            throw SecurityServiceException.newNotLoggedInException("User not logged in");
        }

        Request request = httpRequest instanceof Request ? (Request)httpRequest : HttpConnection.getCurrentConnection().getRequest();
        UserRealm realm = request.getUserRealm();
        if (reauth)
            realm.reauthenticate(principal);

        // Check destination access
        if (context.getDestination().isSecured()) {
            boolean accessDenied = true;
            for (String role : context.getDestination().getRoles()) {
                if (realm.isUserInRole(principal, role)) {
                    accessDenied = false;
                    break;
                }
            }
            if (accessDenied)
                throw SecurityServiceException.newAccessDeniedException("User not in required role");

            request.setAuthType(AUTH_TYPE);
            request.setUserPrincipal(principal);
        }

        try {
            return endAuthorization(context);
        } catch (InvocationTargetException e) {
            for (Throwable t = e; t != null; t = t.getCause()) {
                if (t instanceof SecurityException)
                    throw SecurityServiceException.newAccessDeniedException(t.getMessage());
            }
            throw e;
        }
    }


    public void logout() throws SecurityServiceException {
        HttpGraniteContext graniteContext = (HttpGraniteContext)GraniteContext.getCurrentInstance();
        HttpServletRequest httpRequest = graniteContext.getRequest();
        Request request = httpRequest instanceof Request ? (Request)httpRequest : HttpConnection.getCurrentConnection().getRequest();
        UserRealm realm = request.getUserRealm();

        realm.disassociate(httpRequest.getUserPrincipal());
        
        endLogout();
    }
}
