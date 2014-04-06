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
package org.granite.client.configuration;

import java.io.IOException;
import java.io.InputStream;

import org.granite.client.messaging.codec.MessagingCodec.ClientType;
import org.granite.config.Config;

/**
 * @author Franck WOLFF
 */
public class SimpleConfiguration implements Configuration {
	
	private String graniteStdConfigPath = SimpleConfiguration.class.getPackage().getName().replace('.', '/') + "/granite-config.xml";
	private String graniteConfigPath = null;
	
	private ClientGraniteConfig graniteConfig = null;
	private Object servicesConfig = null;
	
	private ClientType clientType = ClientType.AS3;

    /**
     * Create a configuration using the default granite-config.xml
     */
	public SimpleConfiguration() {
	}

    /**
     * Create a configuration using specified config file paths
     * @param graniteStdConfigPath path of standard config file
     * @param graniteConfigPath path of custom config file (null if no custom file)
     */
	public SimpleConfiguration(String graniteStdConfigPath, String graniteConfigPath) {
		this.graniteStdConfigPath = graniteStdConfigPath;
		this.graniteConfigPath = graniteConfigPath;
	}
	
	public ClientType getClientType() {
		return clientType;
	}

	public void setClientType(ClientType clientType) {
		this.clientType = clientType;
	}

    /**
     * Set the path of the standard config file
     * @param graniteConfigPath path
     */
	public void setGraniteStdConfigPath(String graniteConfigPath) {
		this.graniteStdConfigPath = graniteConfigPath;
	}

    /**
     * Set the path of the custom config file
     * @param graniteConfigPath path
     */
	public void setGraniteConfigPath(String graniteConfigPath) {
		this.graniteConfigPath = graniteConfigPath;
	}

    public boolean isLoaded() {
        return graniteConfig != null;
    }
	
	public void load() {
		InputStream is = null;
		try {
			is = Thread.currentThread().getContextClassLoader().getResourceAsStream(graniteStdConfigPath);
			if (graniteConfigPath != null)
				is = Thread.currentThread().getContextClassLoader().getResourceAsStream(graniteConfigPath);
			graniteConfig = new ClientGraniteConfig(graniteStdConfigPath, is, null, null);
			postLoad(graniteConfig);
			servicesConfig = new ClientServicesConfig();
		}
		catch (Exception e) {
			graniteConfig = null;
			servicesConfig = null;
			throw new RuntimeException("Cannot load configuration", e);
		}
		finally {
			if (is != null) try {
				is.close();
			}
			catch (IOException e) {
			}
		}
	}

    /**
     * Can be overriden by subclasses to do some post loading customization of the configuration
     * @param graniteConfig config object loaded
     */
	protected void postLoad(ClientGraniteConfig graniteConfig) {
	}

    /**
     * GraniteConfig object
     * @return GraniteConfig object
     */
	@SuppressWarnings("unchecked")
	public <C extends Config> C getGraniteConfig() {
		return (C)graniteConfig;
	}

    /**
     * ServicesConfig object
     * @return ServicesConfig object
     */
	@SuppressWarnings("unchecked")
	public <C extends Config> C getServicesConfig() {
		return (C)servicesConfig;
	}

}
