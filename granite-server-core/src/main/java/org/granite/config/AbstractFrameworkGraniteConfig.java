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
package org.granite.config;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletContext;

import org.granite.config.api.Configuration;
import org.granite.config.api.internal.ConfigurationImpl;
import org.granite.config.flex.ServicesConfig;
import org.granite.config.flex.ServletServicesConfig;
import org.granite.logging.Logger;
import org.xml.sax.SAXException;


public abstract class AbstractFrameworkGraniteConfig {

    ///////////////////////////////////////////////////////////////////////////
    // Static fields.

    private static final Logger log = Logger.getLogger(AbstractFrameworkGraniteConfig.class);

    private static final String GRANITE_CONFIG_DEFAULT = "/WEB-INF/granite/granite-config.xml";
    private static final String SERVICES_CONFIG_DEFAULT = "/WEB-INF/flex/services-config.xml";

    ///////////////////////////////////////////////////////////////////////////
    // Instance fields.

    private GraniteConfig graniteConfig = null;

    private ServicesConfig servicesConfig = null;

    ///////////////////////////////////////////////////////////////////////////
    // Constructor.

    ///////////////////////////////////////////////////////////////////////////
    // Static GraniteConfig loaders.

    protected void init(ServletContext servletContext, String configPath) throws IOException, SAXException {
    	String path = configuration.getGraniteConfig();
    	if (path == null)
    		path = GRANITE_CONFIG_DEFAULT;

        InputStream is = servletContext.getResourceAsStream(path);
        if (is == null) {
            log.warn("Could not load custom granite-config.xml: %s (file does not exists)", path);
            path = null;
        }
        
        this.graniteConfig = new GraniteConfig(configPath, is, null, null);
        
        ServletGraniteConfig.loadConfig(servletContext, graniteConfig);
        
    	path = configuration.getFlexServicesConfig();
    	if (path == null)
    		path = SERVICES_CONFIG_DEFAULT;

        is = servletContext.getResourceAsStream(path);
        if (is == null) {
            log.warn("Could not load custom services-config.xml: %s (file does not exists)", path);
            path = null;
        }
        
        this.servicesConfig = new ServicesConfig(is, null, false);
        
        ServletServicesConfig.loadConfig(servletContext, servicesConfig);
    }
    
    public GraniteConfig getGraniteConfig() {
    	return graniteConfig;
    }
    
    public ServicesConfig getServicesConfig() {
    	return servicesConfig;
    }
    
    protected Configuration configuration = new ConfigurationImpl();

    public void setCustomGraniteConfigPath(String path) {
    	configuration.setGraniteConfig(path);
    }

    public void setCustomServicesConfigPath(String path) {
    	configuration.setFlexServicesConfig(path);
    }
}
