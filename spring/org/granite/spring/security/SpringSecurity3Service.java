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

package org.granite.spring.security;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.granite.context.GraniteContext;
import org.granite.logging.Logger;
import org.granite.messaging.service.security.AbstractSecurityContext;
import org.granite.messaging.service.security.AbstractSecurityService;
import org.granite.messaging.service.security.SecurityServiceException;
import org.granite.messaging.webapp.HttpGraniteContext;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionFixationProtectionStrategy;
import org.springframework.security.web.context.HttpRequestResponseHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.context.support.WebApplicationContextUtils;


/**
 * @author Bouiaw
 * @author wdrai
 */
public class SpringSecurity3Service extends AbstractSecurityService {
        
	private static final Logger log = Logger.getLogger(SpringSecurity3Service.class);
	
    private static final String FILTER_APPLIED = "__spring_security_scpf_applied";
    private static final String SECURITY_SERVICE_APPLIED = "__spring_security_granite_service_applied";
	
	private AuthenticationManager authenticationManager = null;
	private AuthenticationTrustResolver authenticationTrustResolver = new AuthenticationTrustResolverImpl();
	private SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();
	private AbstractSpringSecurity3Interceptor securityInterceptor = null;
	private SessionAuthenticationStrategy sessionAuthenticationStrategy = new SessionFixationProtectionStrategy();
	private PasswordEncoder passwordEncoder = null;
	private String authenticationManagerBeanName = null;
	private boolean allowAnonymousAccess = false;
	private Method getRequest = null;
	private Method getResponse = null;
    
	
	public SpringSecurity3Service() {
		log.debug("Starting Spring 3 Security Service");
		try {
	    	getRequest = HttpRequestResponseHolder.class.getDeclaredMethod("getRequest");
	    	getRequest.setAccessible(true);
	    	getResponse = HttpRequestResponseHolder.class.getDeclaredMethod("getResponse");
	    	getResponse.setAccessible(true);
		}
		catch (Exception e) {
			throw new RuntimeException("Could not get methods from HttpRequestResponseHolder", e);
		}
    }
	
	
	public void setAuthenticationManager(AuthenticationManager authenticationManager) {
		this.authenticationManager = authenticationManager;
	}
	
	public void setAuthenticationTrustResolver(AuthenticationTrustResolver authenticationTrustResolver) {
		this.authenticationTrustResolver = authenticationTrustResolver;
	}
	
	public void setAllowAnonymousAccess(boolean allowAnonymousAccess) {
		this.allowAnonymousAccess = allowAnonymousAccess;
	}
	
	public void setSecurityContextRepository(SecurityContextRepository securityContextRepository) {
		this.securityContextRepository = securityContextRepository;
	}
	
	public void setSecurityInterceptor(AbstractSpringSecurity3Interceptor securityInterceptor) {
		this.securityInterceptor = securityInterceptor;
	}
	
	public void setSessionAuthenticationStrategy(SessionAuthenticationStrategy sessionAuthenticationStrategy) {
		if (sessionAuthenticationStrategy == null)
			throw new NullPointerException("SessionAuthenticationStrategy cannot be null");
		this.sessionAuthenticationStrategy = sessionAuthenticationStrategy;
	}
	
	public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
		this.passwordEncoder = passwordEncoder;
	}

    public void configure(Map<String, String> params) {
        log.debug("Configuring with parameters %s: ", params);
        if (params.containsKey("authentication-manager-bean-name"))
        	authenticationManagerBeanName = params.get("authentication-manager-bean-name");
        if (Boolean.TRUE.toString().equals(params.get("allow-anonymous-access")))
        	allowAnonymousAccess = true;
    }
    
    public void login(Object credentials, String charset) {
        List<String> decodedCredentials = Arrays.asList(decodeBase64Credentials(credentials, charset));

        HttpGraniteContext graniteContext = (HttpGraniteContext)GraniteContext.getCurrentInstance();
        HttpServletRequest httpRequest = graniteContext.getRequest();

        String user = decodedCredentials.get(0);
        String password = decodedCredentials.get(1);
        if (passwordEncoder != null)
        	password = passwordEncoder.encodePassword(password, null);
        Authentication auth = new UsernamePasswordAuthenticationToken(user, password);

        ApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(
            httpRequest.getSession().getServletContext()
        );
        if (ctx != null) {
        	lookupAuthenticationManager(ctx, authenticationManagerBeanName);
            
            try {
                Authentication authentication = authenticationManager.authenticate(auth);
                
                if (authentication != null && !authenticationTrustResolver.isAnonymous(authentication))
            		sessionAuthenticationStrategy.onAuthentication(authentication, httpRequest, graniteContext.getResponse());
                
                HttpRequestResponseHolder holder = new HttpRequestResponseHolder(graniteContext.getRequest(), graniteContext.getResponse());
    	        SecurityContext securityContext = securityContextRepository.loadContext(holder);
    	        securityContext.setAuthentication(authentication);
    	        SecurityContextHolder.setContext(securityContext);
	            try {
	            	securityContextRepository.saveContext(securityContext, (HttpServletRequest)getRequest.invoke(holder), (HttpServletResponse)getResponse.invoke(holder));
	            }
	            catch (Exception e) {
	            	log.error(e, "Could not save context after authentication");
	            }
	            
	            endLogin(credentials, charset);
            } 
        	catch (SessionAuthenticationException e) {
                log.debug(e, "SessionAuthenticationStrategy rejected the authentication object");
                SecurityContextHolder.clearContext();
                handleAuthenticationExceptions(e);
            }                
            catch (AuthenticationException e) {
                SecurityContextHolder.clearContext();
            	handleAuthenticationExceptions(e);
            }
            finally {
        		log.debug("Clear authentication");
	            SecurityContextHolder.clearContext();
            }
        }

        log.debug("User %s logged in", user);
    }
    
    protected void handleAuthenticationExceptions(AuthenticationException e) {
    	if (e instanceof BadCredentialsException || e instanceof UsernameNotFoundException)
            throw SecurityServiceException.newInvalidCredentialsException(e.getMessage());
        
    	throw SecurityServiceException.newAuthenticationFailedException(e.getMessage());
    }
    
    public void lookupAuthenticationManager(ApplicationContext ctx, String authenticationManagerBeanName) throws SecurityServiceException {
    	if (this.authenticationManager != null)
    		return;
    	
    	Map<String, AuthenticationManager> authManagers = BeanFactoryUtils.beansOfTypeIncludingAncestors(ctx, AuthenticationManager.class);
    	
        if (authenticationManagerBeanName != null) {
        	this.authenticationManager = authManagers.get(authenticationManagerBeanName);
        	if (authenticationManager == null) {
        		log.error("AuthenticationManager bean not found " + authenticationManagerBeanName);
        		throw SecurityServiceException.newAuthenticationFailedException("Authentication failed");
        	}
        	return;
        }
        else if (authManagers.size() > 1) {
        	log.error("More than one AuthenticationManager beans found, specify which one to use in Spring config <graniteds:security-service authentication-manager='myAuthManager'/> or in granite-config.xml <security type='org.granite.spring.security.SpringSecurity3Service'><param name='authentication-manager-bean-name' value='myAuthManager'/></security>");
    		throw SecurityServiceException.newAuthenticationFailedException("Authentication failed");
        }
        
    	this.authenticationManager = authManagers.values().iterator().next();
    }

    
    public Object authorize(AbstractSecurityContext context) throws Exception {
        log.debug("Authorize %s on destination %s (secured: %b)", context, context.getDestination().getId(), context.getDestination().isSecured());

        startAuthorization(context);
        
        HttpGraniteContext graniteContext = (HttpGraniteContext)GraniteContext.getCurrentInstance();
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        HttpRequestResponseHolder holder = null;
        
    	boolean filterNotApplied = graniteContext.getRequest().getAttribute(FILTER_APPLIED) == null;
        boolean reentrant = graniteContext.getRequest().getAttribute(SECURITY_SERVICE_APPLIED) != null;
        // Manage security context here only if the Spring security filter has not already been applied and if we are not reentrant
        try {
	        if (filterNotApplied && !reentrant) {
		        holder = new HttpRequestResponseHolder(graniteContext.getRequest(), graniteContext.getResponse());
		        SecurityContext contextBeforeChainExecution = securityContextRepository.loadContext(holder);
			    SecurityContextHolder.setContext(contextBeforeChainExecution);
			    graniteContext.getRequest().setAttribute(SECURITY_SERVICE_APPLIED, true);
			    
			    if (isAuthenticated(authentication))
			    	contextBeforeChainExecution.setAuthentication(authentication);
			    else
			    	authentication = contextBeforeChainExecution.getAuthentication();
	        }
	        
	        if (context.getDestination().isSecured()) {
	            if (!isAuthenticated(authentication) || (!allowAnonymousAccess && authentication instanceof AnonymousAuthenticationToken)) {
	                log.debug("User not authenticated!");
	                throw SecurityServiceException.newNotLoggedInException("User not logged in");
	            }
	            if (!userCanAccessService(context, authentication)) { 
	                log.debug("Access denied for user %s", authentication.getName());
	                throw SecurityServiceException.newAccessDeniedException("User not in required role");
	            }
	        }
	        
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
            if (filterNotApplied && !reentrant) {
	            SecurityContext contextAfterChainExecution = SecurityContextHolder.getContext();
        		log.debug("Clear authentication and save to repo: %s", contextAfterChainExecution.getAuthentication() != null ? contextAfterChainExecution.getAuthentication().getName() : "none");
	            SecurityContextHolder.clearContext();
	            try {
	            	securityContextRepository.saveContext(contextAfterChainExecution, (HttpServletRequest)getRequest.invoke(holder), (HttpServletResponse)getResponse.invoke(holder));
	            }
	            catch (Exception e) {
	            	log.error(e, "Could not extract wrapped context from holder");
	            }
	            graniteContext.getRequest().removeAttribute(SECURITY_SERVICE_APPLIED);
        	}
        }
    }

    public void logout() {
    	HttpGraniteContext context = (HttpGraniteContext)GraniteContext.getCurrentInstance();
    	HttpSession session = context.getSession(false);
    	if (session != null && securityContextRepository.containsContext(context.getRequest()))    		
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
