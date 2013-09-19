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
package org.granite.config.flex;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.granite.config.GraniteConfig;
import org.granite.config.ServletGraniteConfig;
import org.granite.config.api.Configuration;
import org.granite.util.ServletParams;
 
/**
 * @author Franck WOLFF
 */
public class ServletServicesConfig {

    ///////////////////////////////////////////////////////////////////////////
    // Fields.

    private static final String SERVICES_CONFIG_KEY = ServletServicesConfig.class.getName() + "_CACHE";


    ///////////////////////////////////////////////////////////////////////////
    // Instance fields.

    private ServicesConfig config = null;

    
    private ServletServicesConfig(ServletContext context, ServicesConfig config) {
    	this.config = config;
    }

    public static synchronized ServletServicesConfig getServletConfig(ServletContext context) {
    	return (ServletServicesConfig)context.getAttribute(SERVICES_CONFIG_KEY);
    }
    
    ///////////////////////////////////////////////////////////////////////////
    // Static ServicesConfig loaders.
    
    public static synchronized ServicesConfig loadConfig(ServletContext context) throws ServletException {
        ServletServicesConfig servletServicesConfig = (ServletServicesConfig)context.getAttribute(SERVICES_CONFIG_KEY);

        if (servletServicesConfig == null) {
        	String path = null;
        	
        	Configuration configuration = (Configuration)context.getAttribute(ServletGraniteConfig.GRANITE_CONFIG_CONFIGURATION_KEY);
        	if (configuration != null)
        		path = configuration.getFlexServicesConfig();
        	
        	if (path == null)
        		path = ServletParams.get(context, "servicesConfigPath", String.class, null);
        	
            if (path == null)
            	path = "/WEB-INF/flex/services-config.xml";

            InputStream is = context.getResourceAsStream(path);

            try {
                GraniteConfig graniteConfig = ServletGraniteConfig.loadConfig(context);
                ServicesConfig servicesConfig = new ServicesConfig(is, configuration, graniteConfig.getScan());
                
                servletServicesConfig = loadConfig(context, servicesConfig);
            }
            catch (Exception e) {
                throw new ServletException("Could not load custom services-config.xml", e);
            }
            finally {
            	try {
            		if (is != null)
            			is.close();
            	} catch (IOException e) {
            		// Ignore...
            	}
            }
            
            context.setAttribute(SERVICES_CONFIG_KEY, servletServicesConfig);
        }

        return servletServicesConfig.config;
    }

    public static synchronized ServletServicesConfig loadConfig(ServletContext context, ServicesConfig servicesConfig) {
        ServletServicesConfig servletServicesConfig = new ServletServicesConfig(context, servicesConfig);
        
        context.setAttribute(SERVICES_CONFIG_KEY, servletServicesConfig);
        
        return servletServicesConfig;
    }
}
