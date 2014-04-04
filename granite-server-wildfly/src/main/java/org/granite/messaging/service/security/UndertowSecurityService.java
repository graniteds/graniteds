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

import io.undertow.security.api.SecurityContext;
import io.undertow.server.HttpServerExchange;
import io.undertow.servlet.spec.HttpServletRequestImpl;

import org.granite.context.GraniteContext;
import org.granite.messaging.service.security.SecurityServiceException;
import org.granite.messaging.webapp.HttpGraniteContext;
import org.granite.messaging.webapp.ServletGraniteContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.security.Principal;
import java.util.Map;

/**
 * Created by william on 03/01/14.
 */
public class UndertowSecurityService extends AbstractSecurityService {

    public void configure(Map<String, String> params) {
    }


    @Override
    public void prelogin(HttpSession session, Object request, String servletName) {
        if (session == null) // Cannot prelogin() without a session
            return;

        if (session.getAttribute(AuthenticationContext.class.getName()) instanceof UndertowAuthenticationContext)
            return;

        HttpServerExchange exchange = null;
        if (request instanceof HttpServletRequestImpl)
            exchange = ((HttpServletRequestImpl)request).getExchange();
        else if (request.getClass().getSimpleName().equals("ExchangeHandshakeRequest")) {   // Websocket
            Field f = null;
            try {
                f = request.getClass().getDeclaredField("exchange");
                f.setAccessible(true);
                Object wsExchange = f.get(request);
                f = wsExchange.getClass().getDeclaredField("exchange");
                f.setAccessible(true);
                exchange = (HttpServerExchange)f.get(wsExchange);
            }
            catch (Exception e) {
                throw new RuntimeException("Could not get internal undertow exchange", e);
            }
        }
        SecurityContext securityContext = exchange.getSecurityContext();

        UndertowAuthenticationContext authenticationContext = new UndertowAuthenticationContext(securityContext);
        session.setAttribute(AuthenticationContext.class.getName(), authenticationContext);
    }


    public Principal login(Object credentials, String charset) throws SecurityServiceException {
        String[] decoded = decodeBase64Credentials(credentials, charset);

        ServletGraniteContext graniteContext = (ServletGraniteContext)GraniteContext.getCurrentInstance();
        Principal principal = null;

        if (graniteContext instanceof HttpGraniteContext) {
            HttpServletRequest httpRequest = graniteContext.getRequest();
            HttpServerExchange exchange = ((HttpServletRequestImpl)httpRequest).getExchange();
            SecurityContext securityContext = exchange.getSecurityContext();

            UndertowAuthenticationContext authenticationContext = new UndertowAuthenticationContext(securityContext);
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
            HttpServerExchange exchange = ((HttpServletRequestImpl)httpRequest).getExchange();
            if (exchange.getSecurityContext() == null || exchange.getSecurityContext().getAuthenticatedAccount() == null)
                tryRelogin();

            if (exchange.getSecurityContext() != null && exchange.getSecurityContext().getAuthenticatedAccount() != null)
                principal = exchange.getSecurityContext().getAuthenticatedAccount().getPrincipal();
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
            HttpServletRequest httpRequest = graniteContext.getRequest();
            HttpServerExchange exchange = ((HttpServletRequestImpl)httpRequest).getExchange();
            SecurityContext securityContext = exchange.getSecurityContext();
            securityContext.logout();
        }

        HttpSession session = graniteContext.getSession(false);
        if (session != null) {
            endLogout();

            session.invalidate();
        }
    }


    public static class UndertowAuthenticationContext implements AuthenticationContext {
    	
		private static final long serialVersionUID = 1L;

        private transient final SecurityContext securityContext;
        private transient Principal principal = null;
        private String username = null;

        public UndertowAuthenticationContext(SecurityContext securityContext) {
            this.securityContext = securityContext;
        }

        public Principal authenticate(String username, String password) {
        	if (securityContext == null)
        		throw SecurityServiceException.newAuthenticationFailedException("Invalid authentication");
        	
            if (username.equals(this.username) && principal != null)
                return principal;

            boolean authenticated = securityContext.login(username, password);
            if (authenticated) {
                this.username = username;
                this.principal = securityContext.getAuthenticatedAccount().getPrincipal();
            }
            return principal;
        }

        public Principal getPrincipal() {
            return principal;
        }

        public boolean isUserInRole(String role) {
            return securityContext.getAuthenticatedAccount().getRoles().contains(role);
        }

        public void logout() {
            securityContext.logout();
        }
    }
}
