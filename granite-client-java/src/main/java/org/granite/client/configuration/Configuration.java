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
package org.granite.client.configuration;

import org.granite.client.messaging.codec.MessagingCodec.ClientType;
import org.granite.config.GraniteConfig;
import org.granite.config.flex.ServicesConfig;

/**
 * Configuration object for AMF channel factory
 * @author Franck WOLFF
 */
public interface Configuration {

    /**
     * Client type (JAVA or AS3) for specific AMF serialization parameters
     * @return client type
     */
	ClientType getClientType();

    /**
     * Set the client type for specific AMF serialization parameters
     * @param clientType client type
     */
	void setClientType(ClientType clientType);

    /**
     * GraniteConfig object loaded of built by this configuration
     * @return granite config
     */
	GraniteConfig getGraniteConfig();

    /**
     * ServicesConfig object loaded of built by this configuration
     * @return services config
     */
	ServicesConfig getServicesConfig();

    /**
     * Is the configuration loaded ?
     * @return true if loaded
     */
    boolean isLoaded();

    /**
     * Load the configuration
     * Called by the channel factory during {@link org.granite.client.messaging.channel.ChannelFactory#start}
     */
	void load();
}
