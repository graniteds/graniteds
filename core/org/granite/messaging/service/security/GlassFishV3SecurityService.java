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
import java.lang.reflect.Method;
import java.security.Principal;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpSession;

import org.apache.catalina.Engine;
import org.apache.catalina.Realm;
import org.apache.catalina.Server;
import org.apache.catalina.ServerFactory;
import org.apache.catalina.Service;
import org.apache.catalina.Session;
import org.apache.catalina.authenticator.Constants;
import org.apache.catalina.connector.RequestFacade;
import org.apache.catalina.connector.Request;
import org.granite.context.GraniteContext;
import org.granite.logging.Logger;
import org.granite.messaging.webapp.HttpGraniteContext;


/**
 * @author Franck WOLFF
 */
public class GlassFishV3SecurityService extends AbstractSecurityService {
	
	private static final Logger log = Logger.getLogger(GlassFishV3SecurityService.class);
    
    private static Method authenticate = null;
    static {
    	// GlassFish V3.0
    	try {
    		authenticate = Realm.class.getMethod("authenticate", String.class, String.class);
    		log.info("Detected GlassFish v3.0 authentication");
    	}
    	catch (NoSuchMethodException e) {
    	}
    	catch (NoSuchMethodError e) {
    	}
    	// GlassFish V3.1+
    	if (authenticate == null) try {
    		authenticate = Realm.class.getMethod("authenticate", String.class, char[].class);
    		log.info("Detected GlassFish v3.1+ authentication");
    	}
    	catch (NoSuchMethodException e) {
    	}
    	catch (NoSuchMethodError e) {
    	}
    	if (authenticate == null)
    		throw new ExceptionInInitializerError("Could not find any supported Realm.authenticate method");

    }
    
    private static Principal authenticate(Realm realm, String username, String password) {
    	try {
	    	if (authenticate.getParameterTypes()[1].equals(String.class))
	    		return (Principal)authenticate.invoke(realm, username, password);
	    	return (Principal)authenticate.invoke(realm, username, password.toCharArray());
    	}
    	catch (Exception e) {
    		throw new RuntimeException(e);
    	}
    }

    private final Field requestField;
    private Engine engine = null;

    public GlassFishV3SecurityService() {
        super();
        try {
            // We need to access the org.apache.catalina.connector.Request field from
            // a org.apache.catalina.connector.RequestFacade. Unfortunately there is
            // no public getter for this field (and I don't want to create a Valve)...
            requestField = RequestFacade.class.getDeclaredField("request");
            requestField.setAccessible(true);
        } catch (Exception e) {
            throw new RuntimeException("Could not get 'request' field in GlassFish V3 RequestFacade", e);
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
            throw new NullPointerException("Could not get GlassFish V3 server");

        Service service = null;
        if (serviceId != null)
            service = server.findService(serviceId);
        else {
            Service[] services = server.findServices();
            if (services != null && services.length > 0)
                service = services[0];
        }
        if (service == null)
            throw new NullPointerException("Could not find GlassFish V3 service for: " + (serviceId != null ? serviceId : "(default)"));

        engine = (Engine)service.getContainer();
        if (engine == null)
            throw new NullPointerException("Could not find GlassFish V3 container for: " + (serviceId != null ? serviceId : "(default)"));
    }

    public void login(Object credentials) throws SecurityServiceException {
        String[] decoded = decodeBase64Credentials(credentials);

        HttpGraniteContext context = (HttpGraniteContext)GraniteContext.getCurrentInstance();
        HttpServletRequest httpRequest = context.getRequest();

        Request request = getRequest(httpRequest);
        Realm realm = request.getContext().getRealm();

        Principal principal = authenticate(realm, decoded[0], decoded[1]);
        if (principal == null)
            throw SecurityServiceException.newInvalidCredentialsException("Wrong username or password");
        
        request.setAuthType(AUTH_TYPE);
        request.setUserPrincipal(principal);

        Session session = request.getSessionInternal();
        session.setAuthType(AUTH_TYPE);
        session.setPrincipal(principal);
        session.setNote(Constants.SESS_USERNAME_NOTE, decoded[0]);
        session.setNote(Constants.SESS_PASSWORD_NOTE, decoded[1]);
        
        endLogin(context, credentials);
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
        	if (principal == null && tryRelogin(graniteContext))
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
            
            endLogout(context);
            
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
            throw new RuntimeException("Could not get GlassFish V3 request", e);
        }
    }

    protected Realm getRealm(HttpServletRequest request) {
    	Request creq = getRequest(request);
        return creq.getContext().getRealm();
    }
}
