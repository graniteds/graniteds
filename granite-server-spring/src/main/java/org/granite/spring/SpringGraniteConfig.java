/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2015 GRANITE DATA SERVICES S.A.S.
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

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletContext;

import org.granite.config.AbstractFrameworkGraniteConfig;
import org.granite.logging.Logger;
import org.granite.messaging.service.ExceptionConverter;
import org.granite.messaging.service.security.SecurityService;
import org.granite.util.TypeUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.context.ServletContextAware;
import org.xml.sax.SAXException;


public class SpringGraniteConfig extends AbstractFrameworkGraniteConfig implements InitializingBean, ServletContextAware, ApplicationContextAware {
	
	private static final Logger log = Logger.getLogger(SpringGraniteConfig.class);

	@Autowired(required=false)
    private ServletContext servletContext = null;
    private ApplicationContext applicationContext = null;

    
	// Determine Spring Security version
    public static boolean isSpringSecurity3Present() {
    	try {
    		SpringGraniteConfig.class.getClassLoader().loadClass("org.springframework.security.access.AccessDeniedException");
    		return true;
    	}
    	catch (ClassNotFoundException e) {
    	}
    	return false;
    }
    public static boolean isSpringSecurity2Present() {
    	try {
    		SpringGraniteConfig.class.getClassLoader().loadClass("org.springframework.security.context.SecurityContext");
    		return true;
    	}
    	catch (ClassNotFoundException e) {
    	}
    	return false;
    }
    
    public void afterPropertiesSet() throws IOException, SAXException {
    	init(servletContext, "org/granite/spring/granite-config-spring.xml");
    	
    	Map<String, ExceptionConverter> exceptionConvertersMap = applicationContext.getBeansOfType(ExceptionConverter.class);
    	for (ExceptionConverter exceptionConverter : exceptionConvertersMap.values())
    	    getGraniteConfig().registerExceptionConverter(exceptionConverter, true);
    	
    	if (getGraniteConfig().getSecurityService() == null) {
    		Map<String, ?> servicesMap = applicationContext.getBeansOfType(SecurityService.class);
    		if (servicesMap.size() == 1) {
    			Entry<String, ?> se = servicesMap.entrySet().iterator().next();
    			log.info("Found security service %s in Spring context", se.getKey());
    			getGraniteConfig().setSecurityService((SecurityService)se.getValue());
    		}
    		else {
    			if (servicesMap.size() > 1)
    				log.warn("Multiple security services found in Spring context, will use default config");
	    		
	    		// Autodetect and configure Spring Security service
	    		// Load dynamically to avoid runtime dependency on SS
	    		try {
			    	if (isSpringSecurity3Present())
			    		getGraniteConfig().setSecurityService(TypeUtil.newInstance("org.granite.spring.security.SpringSecurity3Service", SecurityService.class));
			    	else if (isSpringSecurity2Present())
			    		getGraniteConfig().setSecurityService(TypeUtil.newInstance("org.granite.messaging.service.security.SpringSecurityService", SecurityService.class));
	    		}
	    		catch (Exception e) {
	    			log.error(e, "Could not configure Spring Security service");
	    		}
    		}
    	}
    }
    
    public void setServletContext(ServletContext servletContext) {
    	this.servletContext = servletContext;
    }
    
    public void setApplicationContext(ApplicationContext applicationContext) {
    	this.applicationContext = applicationContext;
    }
}
