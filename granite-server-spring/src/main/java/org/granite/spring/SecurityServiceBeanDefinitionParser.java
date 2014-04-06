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
package org.granite.spring;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * @author William Drai
 */
public class SecurityServiceBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

    @Override
    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
    	if (!SpringGraniteConfig.isSpringSecurity3Present())
    		throw new RuntimeException("Cannot use graniteds:security-service with Spring 2.x");
    	
        // Set the default ID if necessary
        if (!StringUtils.hasText(element.getAttribute(ID_ATTRIBUTE)))
            element.setAttribute(ID_ATTRIBUTE, "org.granite.spring.security.SpringSecurity3Service");
    	
    	builder.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);

        String authenticationExtension = element.getAttribute("authentication-extension");
        if (authenticationExtension != null && authenticationExtension.trim().length() > 0)
            builder.addPropertyReference("authenticationExtension", authenticationExtension);

        String authenticationManager = element.getAttribute("authentication-manager");
        if (authenticationManager != null && authenticationManager.trim().length() > 0)
        	builder.addPropertyReference("authenticationManager", authenticationManager);
    	
        boolean allowAnonymousAccess = Boolean.valueOf(element.hasAttribute("allow-anonymous-access"));
        	builder.addPropertyValue("allowAnonymousAccess", allowAnonymousAccess);
        
        String securityContextRepository = element.getAttribute("security-context-repository");
        if (securityContextRepository != null && securityContextRepository.trim().length() > 0)
        	builder.addPropertyReference("securityContextRepository", securityContextRepository);
    	
        String securityMetadataSource = element.getAttribute("security-metadata-source");
        if (securityMetadataSource != null && securityMetadataSource.trim().length() > 0)
        	builder.addPropertyReference("securityMetadataSource", securityMetadataSource);
    	
        String securityInterceptor = element.getAttribute("security-interceptor");
        if (securityInterceptor != null && securityInterceptor.trim().length() > 0)
        	builder.addPropertyReference("securityInterceptor", securityInterceptor);
        
        String authenticationTrustResolver = element.getAttribute("authentication-trust-resolver");
        if (authenticationTrustResolver != null && authenticationTrustResolver.trim().length() > 0)
            builder.addPropertyReference("authenticationTrustResolver", authenticationTrustResolver);
        
        String sessionAuthenticationStrategy = element.getAttribute("session-authentication-strategy");
        if (sessionAuthenticationStrategy != null && sessionAuthenticationStrategy.trim().length() > 0)
            builder.addPropertyReference("sessionAuthenticationStrategy", sessionAuthenticationStrategy);
        
        String passwordEncoder = element.getAttribute("password-encoder");
        if (passwordEncoder != null && passwordEncoder.trim().length() > 0)
            builder.addPropertyReference("passwordEncoder", passwordEncoder);
    }

    @Override
    protected String getBeanClassName(Element element) {
        return "org.granite.spring.security.SpringSecurity3Service";
    }
}
