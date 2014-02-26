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
import java.lang.reflect.Method;
import java.security.Principal;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpSession;

import com.sun.web.security.RealmAdapter;
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
import org.granite.messaging.webapp.ServletGraniteContext;


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


    @Override
    public void prelogin(HttpSession session, Object httpRequest) {
        if (session == null) // Cannot prelogin() without a session
            return;

        if (session.getAttribute(AuthenticationContext.class.getName()) instanceof GlassFishV3AuthenticationContext)
            return;

        HttpServletRequest request = null;
        if (httpRequest instanceof HttpServletRequest)
            request = (HttpServletRequest)httpRequest;
        else {
            if (httpRequest.getClass().getName().equals("org.glassfish.tyrus.core.RequestContext")) {   // Websocket
                Field f = null;
                try {
                    f = httpRequest.getClass().getDeclaredField("isUserInRoleDelegate");
                    f.setAccessible(true);
                    Object delegate = f.get(httpRequest);
                    f = delegate.getClass().getDeclaredField("val$httpServletRequest");
                    f.setAccessible(true);
                    request = (HttpServletRequest)f.get(delegate);
                }
                catch (Exception e) {
                    throw new RuntimeException("Could not get internal undertow exchange", e);
                }
            }
        }
        Request req = getRequest(request);
        RealmAdapter realm = getRealm(request);

        GlassFishV3AuthenticationContext authorizationContext = new GlassFishV3AuthenticationContext(req.getWrapper().getServletName(), realm);
        session.setAttribute(AuthenticationContext.class.getName(), authorizationContext);
    }


    public void login(Object credentials, String charset) throws SecurityServiceException {
        String[] decoded = decodeBase64Credentials(credentials, charset);

        ServletGraniteContext graniteContext = (ServletGraniteContext)GraniteContext.getCurrentInstance();
        Principal principal = null;
        Request request = null;

        if (graniteContext instanceof HttpGraniteContext) {
            HttpGraniteContext context = (HttpGraniteContext)GraniteContext.getCurrentInstance();
            HttpServletRequest httpRequest = context.getRequest();
            request = getRequest(httpRequest);
            RealmAdapter realm = (RealmAdapter)request.getContext().getRealm();

            GlassFishV3AuthenticationContext authenticationContext = new GlassFishV3AuthenticationContext(request.getWrapper().getServletName(), realm);
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

    protected RealmAdapter getRealm(HttpServletRequest request) {
    	Request creq = getRequest(request);
        return (RealmAdapter)creq.getContext().getRealm();
    }


    public static class GlassFishV3AuthenticationContext implements AuthenticationContext {

        private final String securityServletName;
        private final RealmAdapter realm;
        private Principal principal;

        public GlassFishV3AuthenticationContext(String securityServletName, RealmAdapter realm) {
            this.securityServletName = securityServletName;
            this.realm = realm;
        }

        public Principal authenticate(String username, String password) {
            principal = GlassFishV3SecurityService.authenticate(realm, username, password);
            return principal;
        }

        public Principal getPrincipal() {
            return principal;
        }

        public boolean isUserInRole(String role) {
            return realm.hasRole(securityServletName, principal, role);
        }
    }
}
