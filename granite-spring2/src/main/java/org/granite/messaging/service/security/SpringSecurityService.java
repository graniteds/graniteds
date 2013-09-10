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
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.granite.context.GraniteContext;
import org.granite.logging.Logger;
import org.granite.messaging.webapp.HttpGraniteContext;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.security.AbstractAuthenticationManager;
import org.springframework.security.AccessDeniedException;
import org.springframework.security.Authentication;
import org.springframework.security.AuthenticationException;
import org.springframework.security.BadCredentialsException;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.context.HttpSessionContextIntegrationFilter;
import org.springframework.security.context.SecurityContext;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;
import org.springframework.security.providers.anonymous.AnonymousAuthenticationToken;
import org.springframework.security.userdetails.UsernameNotFoundException;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * @author Bouiaw
 * @author wdrai
 */
public class SpringSecurityService extends AbstractSecurityService {
        
	private static final Logger log = Logger.getLogger(SpringSecurityService.class);
	
    private static final String FILTER_APPLIED = "__spring_security_filterSecurityInterceptor_filterApplied";
    
	private AbstractSpringSecurityInterceptor securityInterceptor = null;
	
        
	public SpringSecurityService() {
		log.debug("Starting Spring Security Service!");
    }

    public void configure(Map<String, String> params) {
        log.debug("Configuring with parameters (NOOP) %s: ", params);
    }
	
	public void setSecurityInterceptor(AbstractSpringSecurityInterceptor securityInterceptor) {
		this.securityInterceptor = securityInterceptor;
	}
    
    public void login(Object credentials, String charset) {
        List<String> decodedCredentials = Arrays.asList(decodeBase64Credentials(credentials, charset));

        HttpGraniteContext context = (HttpGraniteContext)GraniteContext.getCurrentInstance();
        HttpServletRequest httpRequest = context.getRequest();

        String user = decodedCredentials.get(0);
        String password = decodedCredentials.get(1);
        Authentication auth = new UsernamePasswordAuthenticationToken(user, password);

        ApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(
            httpRequest.getSession().getServletContext()
        );
        if (ctx != null) {
            AbstractAuthenticationManager authenticationManager =
                BeanFactoryUtils.beanOfTypeIncludingAncestors(ctx, AbstractAuthenticationManager.class);
            try {
                Authentication authentication = authenticationManager.authenticate(auth);
                SecurityContext securityContext = SecurityContextHolder.getContext();
                securityContext.setAuthentication(authentication);
                SecurityContextHolder.setContext(securityContext);
                saveSecurityContextInSession(securityContext, 0);

                endLogin(credentials, charset);
            } 
            catch (AuthenticationException e) {
            	handleAuthenticationExceptions(e);
            }
        }

        log.debug("User %s logged in", user);
    }
    
    protected void handleAuthenticationExceptions(AuthenticationException e) {
    	if (e instanceof BadCredentialsException || e instanceof UsernameNotFoundException)
            throw SecurityServiceException.newInvalidCredentialsException(e.getMessage());
        
    	throw SecurityServiceException.newAuthenticationFailedException(e.getMessage());
    }

    public Object authorize(AbstractSecurityContext context) throws Exception {
        log.debug("Authorize: %s", context);
        log.debug("Is %s secured? %b", context.getDestination().getId(), context.getDestination().isSecured());

        startAuthorization(context);

        HttpGraniteContext graniteContext = (HttpGraniteContext)GraniteContext.getCurrentInstance();
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        SecurityContext securityContextBefore = null;
        int securityContextHashBefore = 0;
        if (graniteContext.getRequest().getAttribute(FILTER_APPLIED) == null) {
	        securityContextBefore = loadSecurityContextFromSession();
	        if (securityContextBefore == null)
	        	securityContextBefore = SecurityContextHolder.getContext();
	        else
		        securityContextHashBefore = securityContextBefore.hashCode();
        	SecurityContextHolder.setContext(securityContextBefore);
	        authentication = securityContextBefore.getAuthentication();
        }
        
        if (context.getDestination().isSecured()) {
            if (!isAuthenticated(authentication) || authentication instanceof AnonymousAuthenticationToken) {
                log.debug("Is not authenticated!");
                throw SecurityServiceException.newNotLoggedInException("User not logged in");
            }
            if (!userCanAccessService(context, authentication)) { 
                log.debug("Access denied for: %s", authentication.getName());
                throw SecurityServiceException.newAccessDeniedException("User not in required role");
            }
        }

        try {
        	Object returnedObject = securityInterceptor != null 
        		? securityInterceptor.invoke(context)
        		: endAuthorization(context);
        	
        	return returnedObject;
        }
        catch (AccessDeniedException e) {
        	throw SecurityServiceException.newAccessDeniedException(e.getMessage());
        }
        catch (InvocationTargetException e) {
            handleAuthorizationExceptions(e);
            throw e;
        }
        finally {
            if (graniteContext.getRequest().getAttribute(FILTER_APPLIED) == null) {
            	// Do this only when not already filtered by Spring Security
	        	SecurityContext securityContextAfter = SecurityContextHolder.getContext();
	        	SecurityContextHolder.clearContext();
	        	saveSecurityContextInSession(securityContextAfter, securityContextHashBefore);
            }
        }
    }

    public void logout() {
        HttpGraniteContext context = (HttpGraniteContext)GraniteContext.getCurrentInstance();
        HttpSession session = context.getSession(false);
        if (session != null && session.getAttribute(HttpSessionContextIntegrationFilter.SPRING_SECURITY_CONTEXT_KEY) != null)
        	session.invalidate();
        
    	SecurityContextHolder.clearContext();
    }

    protected boolean isUserInRole(Authentication authentication, String role) {
        for (GrantedAuthority ga : authentication.getAuthorities()) {
            if (ga.getAuthority().matches(role))
                return true;
        }
        return false;
    }

    protected boolean isAuthenticated(Authentication authentication) {
        return authentication != null && authentication.isAuthenticated();
    }

    protected boolean userCanAccessService(AbstractSecurityContext context, Authentication authentication) {
        log.debug("Is authenticated as: %s", authentication.getName());

        for (String role : context.getDestination().getRoles()) {
            if (isUserInRole(authentication, role)) {
                log.debug("Allowed access to %s in role %s", authentication.getName(), role);
                return true;
            }
            log.debug("Access denied for %s not in role %s", authentication.getName(), role);
        }
        return false;
    }

    protected SecurityContext loadSecurityContextFromSession() {
        HttpGraniteContext context = (HttpGraniteContext)GraniteContext.getCurrentInstance();
        HttpServletRequest request = context.getRequest();
    	return (SecurityContext)request.getSession().getAttribute(HttpSessionContextIntegrationFilter.SPRING_SECURITY_CONTEXT_KEY);
    }
    
    protected void saveSecurityContextInSession(SecurityContext securityContext, int securityContextHashBefore) {
    	if (securityContext.hashCode() != securityContextHashBefore &&
    			!(securityContext.getAuthentication() instanceof AnonymousAuthenticationToken)) {
	        HttpGraniteContext context = (HttpGraniteContext)GraniteContext.getCurrentInstance();
	        HttpServletRequest request = context.getRequest();
	        request.getSession().setAttribute(HttpSessionContextIntegrationFilter.SPRING_SECURITY_CONTEXT_KEY, securityContext);
    	}
    }

    protected void handleAuthorizationExceptions(InvocationTargetException e) {
        for (Throwable t = e; t != null; t = t.getCause()) {
            // Don't create a dependency to javax.ejb in SecurityService...
            if (t instanceof SecurityException ||
                t instanceof AccessDeniedException ||
                "javax.ejb.EJBAccessException".equals(t.getClass().getName()))
                throw SecurityServiceException.newAccessDeniedException(t.getMessage());
        }
    }

}
