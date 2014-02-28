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
package org.granite.seam21.security;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.security.auth.login.LoginException;

import org.granite.logging.Logger;
import org.granite.messaging.service.security.AbstractSecurityContext;
import org.granite.messaging.service.security.AbstractSecurityService;
import org.granite.messaging.service.security.SecurityServiceException;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.Events;
import org.jboss.seam.international.StatusMessage;
import org.jboss.seam.international.StatusMessages;
import org.jboss.seam.security.AuthorizationException;
import org.jboss.seam.security.Identity;
import org.jboss.seam.security.NotLoggedInException;

/**
 * @author Venkat DANDA
 */
public class Seam21SecurityService extends AbstractSecurityService {

	private static final Logger log = Logger.getLogger(Seam21SecurityService.class);
	
    public void configure(Map<String, String> params) {
    }

    @SuppressWarnings("deprecation")
	public Principal login(Object credentials, String charset) throws SecurityServiceException {
        String[] decoded = decodeBase64Credentials(credentials, charset);
        
        Contexts.getSessionContext().set("org.granite.seam.login", Boolean.TRUE);
        
        Identity identity = Identity.instance();
        
        // Unauthenticate if username has changed (otherwise the previous session/principal is reused)
        if (identity.isLoggedIn(false) && !decoded[0].equals(identity.getUsername())) {
        	try {
        		Method method = identity.getClass().getDeclaredMethod("unAuthenticate");
        		method.setAccessible(true);
        		method.invoke(identity);
        	} catch (Exception e) {
        		log.error(e, "Could not call unAuthenticate method on: %s", identity.getClass());
        	}
        }

        identity.setUsername(decoded[0]);
        identity.setPassword(decoded[1]);
        try {
            identity.authenticate();

            endLogin(credentials, charset);
        }
        catch (LoginException e) {
        	// Force add of login error messages
        	try {
        		Method method = identity.getClass().getMethod("getCredentials");
        		Object cred = method.invoke(identity);
        		method = cred.getClass().getMethod("invalidate");
        		method.invoke(cred);
        	} catch (Exception f) {
        		log.error(f, "Could not call getCredentials().invalidate() method on: %s", identity.getClass());
        	}
            
            if (Events.exists()) 
            	Events.instance().raiseEvent("org.jboss.seam.security.loginFailed", e);
            
            throw SecurityServiceException.newInvalidCredentialsException("User authentication failed");
        }

        return identity.getPrincipal();
    }

    public Object authorize(AbstractSecurityContext context) throws Exception {
        startAuthorization(context);
        
        if (context.getDestination().isSecured()) {

            Identity identity = Identity.instance();
            if (!identity.isLoggedIn()) {
                // TODO: Session expiration detection...
                // throw SecurityServiceException.newSessionExpiredException("Session expired");
                throw SecurityServiceException.newNotLoggedInException("User not logged in");
            }

            boolean accessDenied = true;
            for (String role : context.getDestination().getRoles()) {
                if (identity.hasRole(role)) {
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
                // If destination is not secured...
                if (t instanceof NotLoggedInException)
                    throw SecurityServiceException.newNotLoggedInException("User not logged in");
                // Don't create a dependency to javax.ejb in SecurityService...
                if (t instanceof SecurityException ||
                    t instanceof AuthorizationException ||
                    "javax.ejb.EJBAccessException".equals(t.getClass().getName()))
                    throw SecurityServiceException.newAccessDeniedException(t.getMessage());
            }
            throw e;
        }
    }

    public void logout() throws SecurityServiceException {
    	// if (Identity.instance().isLoggedIn(false)) ?
        Identity.instance().logout();
    }
    
    
    @Override
    @SuppressWarnings("unchecked")
    public void handleSecurityException(SecurityServiceException e) {
        List<StatusMessage> messages = Collections.emptyList();
        
        try {
            StatusMessages statusMessages = StatusMessages.instance();
            if (statusMessages != null) {
                // Execute and get the messages (once again reflection hack to use protected methods) 
                Method m = StatusMessages.class.getDeclaredMethod("doRunTasks");
                m.setAccessible(true);
                m.invoke(statusMessages);
                
                Method m2 = StatusMessages.class.getDeclaredMethod("getMessages");
                m2.setAccessible(true);
                messages = (List<StatusMessage>)m2.invoke(statusMessages);
            }
        }
        catch (Exception se) {
            log.error("Could not get status messages", se);
        }
        
        List<Object> tideMessages = new ArrayList<Object>(messages.size());
        
        try {
            Class<?> c = Thread.currentThread().getContextClassLoader().loadClass("org.granite.tide.TideMessage");
            Constructor<?> co = c.getConstructor(String.class, String.class, String.class);
            
            for (StatusMessage fm : messages) {
                String severity = null;
                if (fm.getSeverity() == StatusMessage.Severity.INFO)
                    severity = "INFO";
                else if (fm.getSeverity() == StatusMessage.Severity.WARN)
                    severity = "WARNING";
                else if (fm.getSeverity() == StatusMessage.Severity.ERROR)
                    severity = "ERROR";
                else if (fm.getSeverity() == StatusMessage.Severity.FATAL)
                    severity = "FATAL";
                
                tideMessages.add(co.newInstance(severity, fm.getSummary(), fm.getDetail()));
            }
            
            e.getExtendedData().put("messages", tideMessages);
        }
        catch (Throwable t) {
            e.getExtendedData().put("messages", tideMessages);
        }
    }
}
