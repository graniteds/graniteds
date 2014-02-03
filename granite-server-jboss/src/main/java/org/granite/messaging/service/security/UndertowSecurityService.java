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
import org.granite.messaging.webapp.HttpGraniteContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.lang.reflect.InvocationTargetException;
import java.security.Principal;
import java.util.Map;

/**
 * Created by william on 03/01/14.
 */
public class UndertowSecurityService extends AbstractSecurityService {

    public void configure(Map<String, String> params) {
    }


    public void login(Object credentials, String charset) throws SecurityServiceException {
        String[] decoded = decodeBase64Credentials(credentials, charset);

        HttpGraniteContext context = (HttpGraniteContext)GraniteContext.getCurrentInstance();
        HttpServletRequest httpRequest = context.getRequest();

        HttpServerExchange exchange = ((HttpServletRequestImpl)httpRequest).getExchange();
        SecurityContext securityContext = exchange.getAttachment(SecurityContext.ATTACHMENT_KEY);
        boolean loggedIn = securityContext.login(decoded[0], decoded[1]);
        if (!loggedIn)
            throw SecurityServiceException.newInvalidCredentialsException("Wrong username or password");

        endLogin(credentials, charset);
    }

    public Object authorize(AbstractSecurityContext context) throws Exception {

        startAuthorization(context);

        HttpGraniteContext graniteContext = (HttpGraniteContext)GraniteContext.getCurrentInstance();
        HttpServletRequest httpRequest = graniteContext.getRequest();
        HttpServerExchange exchange = ((HttpServletRequestImpl)httpRequest).getExchange();
        HttpSession session = httpRequest.getSession(false);

        Principal principal = null;
        if (session != null) {
            if (exchange.getAttachment(SecurityContext.ATTACHMENT_KEY) == null)
                tryRelogin();
        }

        if (context.getDestination().isSecured()) {
            if (principal == null) {
                if (httpRequest.getRequestedSessionId() != null) {
                    HttpSession httpSession = httpRequest.getSession(false);
                    if (httpSession == null || httpRequest.getRequestedSessionId().equals(httpSession.getId()))
                        throw SecurityServiceException.newSessionExpiredException("Session expired");
                }
                throw SecurityServiceException.newNotLoggedInException("User not logged in");
            }

            boolean accessDenied = true;
            for (String role : context.getDestination().getRoles()) {
                if (httpRequest.isUserInRole(role)) {
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
        HttpGraniteContext graniteContext = (HttpGraniteContext)GraniteContext.getCurrentInstance();
        HttpServletRequest httpRequest = graniteContext.getRequest();
        HttpServerExchange exchange = ((HttpServletRequestImpl)httpRequest).getExchange();
        SecurityContext securityContext = exchange.getAttachment(SecurityContext.ATTACHMENT_KEY);

        securityContext.logout();

        HttpSession session = httpRequest.getSession(false);
        if (session != null) {
            endLogout();

            session.invalidate();
        }
    }
}
