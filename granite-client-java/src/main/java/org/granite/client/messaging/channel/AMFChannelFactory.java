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
package org.granite.client.messaging.channel;

import org.granite.client.configuration.Configuration;
import org.granite.client.messaging.channel.amf.AMFRemotingChannel;
import org.granite.client.messaging.codec.AMF0MessagingCodec;
import org.granite.client.messaging.codec.AMF3MessagingCodec;
import org.granite.client.messaging.codec.MessagingCodec;
import org.granite.client.messaging.transport.Transport;
import org.granite.client.platform.Platform;
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

		this.aliasRegistry = this.defaultConfiguration.getGraniteConfig().getAliasRegistry();
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
