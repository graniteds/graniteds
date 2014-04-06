/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2014 GRANITE DATA SERVICES S.A.S.
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

import java.lang.reflect.InvocationTargetException;
import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.acegisecurity.AbstractAuthenticationManager;
import org.acegisecurity.AccessDeniedException;
import org.acegisecurity.Authentication;
import org.acegisecurity.AuthenticationException;
import org.acegisecurity.BadCredentialsException;
import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.context.SecurityContext;
import org.acegisecurity.context.SecurityContextHolder;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import org.acegisecurity.userdetails.UsernameNotFoundException;
import org.granite.context.GraniteContext;
import org.granite.logging.Logger;
import org.granite.messaging.webapp.HttpGraniteContext;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * @author Francisco PEREDO
 */
public class AcegiSecurityService extends AbstractSecurityService {

    private static final Logger log = Logger.getLogger(AcegiSecurityService.class);
	private static final String SPRING_AUTHENTICATION_TOKEN = AcegiSecurityService.class.getName() + ".SPRING_AUTHENTICATION_TOKEN";

    public AcegiSecurityService() {
        log.debug("Starting Service!");
    }

    public void configure(Map<String, String> params) {
        log.debug("Configuring with parameters (NOOP) %s: ", params);
    }

    public Principal login(Object credentials, String charset) {
        List<String> decodedCredentials = Arrays.asList(decodeBase64Credentials(credentials, charset));

        HttpGraniteContext context = (HttpGraniteContext)GraniteContext.getCurrentInstance();
        HttpServletRequest httpRequest = context.getRequest();

        String user = decodedCredentials.get(0);
        String password = decodedCredentials.get(1);
        Authentication auth = new UsernamePasswordAuthenticationToken(user, password);
        Principal principal = null;

        ApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(
            httpRequest.getSession().getServletContext()
        );
        if (ctx != null) {
            AbstractAuthenticationManager authenticationManager =
                BeanFactoryUtils.beanOfTypeIncludingAncestors(ctx, AbstractAuthenticationManager.class);
            try {
                Authentication authentication = authenticationManager.authenticate(auth);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                principal = authentication;
                httpRequest.getSession().setAttribute(SPRING_AUTHENTICATION_TOKEN, authentication);
                
                endLogin(credentials, charset);
            } 
            catch (AuthenticationException e) {
            	handleAuthenticationExceptions(e);
            }
        }

        log.debug("User %s logged in", user);

        return principal;
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

        Authentication authentication = getAuthentication();
        if (context.getDestination().isSecured()) {
            if (!isAuthenticated(authentication)) {
                log.debug("Is not authenticated!");
                throw SecurityServiceException.newNotLoggedInException("User not logged in");
            }
            if (!userCanAccessService(context, authentication)) { 
                log.debug("Access denied for: %s", authentication.getName());
                throw SecurityServiceException.newAccessDeniedException("User not in required role");
            }
        }
        if (isAuthenticated(authentication)) {
            SecurityContext securityContext = SecurityContextHolder.getContext();
            securityContext.setAuthentication(authentication);
        }

        try {
            return endAuthorization(context);
        } catch (InvocationTargetException e) {
            handleAuthorizationExceptions(e);
            throw e;
        }
        finally {
        	SecurityContextHolder.clearContext();
        }
    }

    public void logout() {
        HttpGraniteContext context = (HttpGraniteContext)GraniteContext.getCurrentInstance();
        context.getSession().invalidate();
        SecurityContextHolder.getContext().setAuthentication(null);
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

    protected Authentication getAuthentication() {
        HttpGraniteContext context = (HttpGraniteContext)GraniteContext.getCurrentInstance();
        HttpServletRequest httpRequest = context.getRequest();
        Authentication authentication = 
            (Authentication) httpRequest.getSession().getAttribute(SPRING_AUTHENTICATION_TOKEN);
        return authentication;
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
