/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2014 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of the Granite Data Services Platform.
 *
 *                               ***
 *
 *   Community License: GPL 3.0
 *
 *   This file is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published
 *   by the Free Software Foundation, either version 3 of the License,
 *   or (at your option) any later version.
 *
 *   This file is distributed in the hope that it will be useful, but
 *   WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *                               ***
 *
 *   Available Commercial License: GraniteDS SLA 1.0
 *
 *   This is the appropriate option if you are creating proprietary
 *   applications and you are not prepared to distribute and share the
 *   source code of your application under the GPL v3 license.
 *
 *   Please visit http://www.granitedataservices.com/license for more
 *   details.
 */
package org.granite.client.configuration;

import org.granite.client.messaging.codec.MessagingCodec.ClientType;
import org.granite.config.Config;

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
     * ClientGraniteConfig object loaded of built by this configuration
     * @return granite config
     */
	<C extends Config> C getGraniteConfig();
	
    /**
     * ServicesConfig object loaded of built by this configuration
     * @return services config
     */
	<C extends Config> C getServicesConfig();

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
