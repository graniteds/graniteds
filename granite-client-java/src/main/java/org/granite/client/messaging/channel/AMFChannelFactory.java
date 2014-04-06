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
package org.granite.client.messaging.channel;

import org.granite.client.configuration.Configuration;
import org.granite.client.messaging.channel.amf.AMFRemotingChannel;
import org.granite.client.messaging.codec.AMF0MessagingCodec;
import org.granite.client.messaging.codec.AMF3MessagingCodec;
import org.granite.client.messaging.codec.MessagingCodec;
import org.granite.client.messaging.transport.Transport;
import org.granite.client.platform.Platform;
import org.granite.config.api.AliasRegistryConfig;
import org.granite.messaging.amf.AMF0Message;
import org.granite.util.ContentType;

/**
 * Implementation of a ChannelFactory using AMF serialization
 *
 * @author Franck WOLFF
 */
public class AMFChannelFactory extends AbstractChannelFactory {
    
	private final Configuration defaultConfiguration;

    /**
     * Create a default AMF channel factory with a basic configuration
     */
	public AMFChannelFactory() {
		this(null, null, null, null);
	}

    /**
     * Create a default AMF channel factory with the basic configuration and for the specified platform context
     * @param context platform context
     * @see org.granite.client.platform.Platform
     */
	public AMFChannelFactory(Object context) {
		this(context, null, null, null);
	}

    /**
     * Create an AMF channel factory with the specified configuration and for the specified platform context
     * Custom configuration can extend the default SimpleConfiguration
     * @param context platform context
     * @param defaultConfiguration configuration
     * @see org.granite.client.configuration.SimpleConfiguration
     */
	public AMFChannelFactory(Object context, Configuration defaultConfiguration) {
		this(context, null, null, defaultConfiguration);
	}

    /**
     * Create an AMF channel factory with the specified transports and for the specified platform context
     * @param context platform context
     * @param remotingTransport remoting transport
     * @param messagingTransport messaging transport
     */
	public AMFChannelFactory(Object context, Transport remotingTransport, Transport messagingTransport) {
		this(context, remotingTransport, messagingTransport, null);
	}

    /**
     * Create an AMF channel factory with the specified configuration and transports and for the specified platform context
     * Custom configuration can extend the default SimpleConfiguration
     * @param context platform context
     * @param remotingTransport remoting transport
     * @param messagingTransport messaging transport
     * @param defaultConfiguration configuration
     * @see org.granite.client.configuration.SimpleConfiguration
     */
	public AMFChannelFactory(Object context, Transport remotingTransport, Transport messagingTransport, Configuration defaultConfiguration) {
		super(ContentType.AMF, context, remotingTransport, messagingTransport);
		
		this.defaultConfiguration = (defaultConfiguration != null ? defaultConfiguration : Platform.getInstance().newConfiguration());
        if (!this.defaultConfiguration.isLoaded())
            this.defaultConfiguration.load();

		this.aliasRegistry = ((AliasRegistryConfig)this.defaultConfiguration.getGraniteConfig()).getAliasRegistry();
	}

	@Override
	protected Class<? extends RemotingChannel> getRemotingChannelClass() {
        return AMFRemotingChannel.class;
	}

    @SuppressWarnings("unchecked")
	@Override
    protected <M> MessagingCodec<M> newMessagingCodec(Class<M> messageClass) {
        if (messageClass == flex.messaging.messages.Message[].class)
            return (MessagingCodec<M>)new AMF3MessagingCodec(defaultConfiguration);
        else if (messageClass == AMF0Message.class)
            return (MessagingCodec<M>)new AMF0MessagingCodec(defaultConfiguration);
        throw new IllegalArgumentException("Unknown message class " + messageClass);
    }

    public Configuration getDefaultConfiguration() {
		return defaultConfiguration;
	}
}
