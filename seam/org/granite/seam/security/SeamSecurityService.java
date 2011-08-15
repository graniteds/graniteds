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

package org.granite.seam.security;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.security.auth.login.LoginException;

import org.granite.logging.Logger;
import org.granite.messaging.service.security.AbstractSecurityContext;
import org.granite.messaging.service.security.AbstractSecurityService;
import org.granite.messaging.service.security.SecurityServiceException;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.security.AuthorizationException;
import org.jboss.seam.security.Identity;
import org.jboss.seam.security.NotLoggedInException;

/**
 * @author Venkat DANDA
 */
public class SeamSecurityService extends AbstractSecurityService {

	private static final Logger log = Logger.getLogger(SeamSecurityService.class);
	
    public void configure(Map<String, String> params) {
    }

    public void login(Object credentials) throws SecurityServiceException {
        String[] decoded = decodeBase64Credentials(credentials);
        
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
        }
        catch (LoginException e) {
            identity.login();   // Force add of login error messages
            
            handleAuthenticationExceptions(e);
        }
    }
    
    protected void handleAuthenticationExceptions(LoginException e) {
        throw SecurityServiceException.newInvalidCredentialsException("User authentication failed", e.getMessage());
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
        } catch (InvocationTargetException e) {
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
    public void handleSecurityException(SecurityServiceException e) {
        // Add messages
        //Prepare for the messages. First step is convert the tasks to Seam FacesMessages
        FacesMessages.afterPhase();
        //Second step is add the Seam FacesMessages to JSF FacesContext Messages
        FacesMessages.instance().beforeRenderResponse();
        
        List<FacesMessage> facesMessages = FacesMessages.instance().getCurrentMessages();
        
        try {
            Class<?> c = Thread.currentThread().getContextClassLoader().loadClass("org.granite.tide.TideMessage");
            Constructor<?> co = c.getConstructor(String.class, String.class, String.class);
            
            List<Object> tideMessages = new ArrayList<Object>(facesMessages.size());
            for (FacesMessage fm : facesMessages) {
                String severity = null;
                if (fm.getSeverity() == FacesMessage.SEVERITY_INFO)
                    severity = "INFO";
                else if (fm.getSeverity() == FacesMessage.SEVERITY_WARN)
                    severity = "WARNING";
                else if (fm.getSeverity() == FacesMessage.SEVERITY_ERROR)
                    severity = "ERROR";
                else if (fm.getSeverity() == FacesMessage.SEVERITY_FATAL)
                    severity = "FATAL";
                
                tideMessages.add(co.newInstance(severity, fm.getSummary(), fm.getDetail()));
            }
            
            e.getExtendedData().put("messages", tideMessages);
        }
        catch (Throwable t) {
            e.getExtendedData().put("messages", facesMessages);
        }
    }
}
