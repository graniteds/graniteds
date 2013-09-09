/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of Granite Data Services.
 *
 *   Granite Data Services is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU Library General Public License as published by
 *   the Free Software Foundation; either version 2 of the License, or (at your
 *   option) any later version.
 *
 *   Granite Data Services is distributed in the hope that it will be useful, but
 *   WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *   FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
 *   for more details.
 *
 *   You should have received a copy of the GNU Library General Public License
 *   along with this library; if not, see <http://www.gnu.org/licenses/>.
 */
package org.granite.client.messaging.channel;

import java.net.URI;

import org.granite.client.configuration.Configuration;
import org.granite.client.messaging.channel.amf.AMFMessagingChannel;
import org.granite.client.messaging.channel.amf.AMFRemotingChannel;
import org.granite.client.messaging.transport.Transport;
import org.granite.client.platform.Platform;
import org.granite.util.ContentType;

/**
 * @author Franck WOLFF
 */
public class AMFChannelFactory extends AbstractChannelFactory {
    
	private final Configuration defaultConfiguration;
	
	public AMFChannelFactory() {
		this(null, null, null, null);
	}
	
	public AMFChannelFactory(Object context) {
		this(context, null, null, null);
	}
	
	public AMFChannelFactory(Object context, Configuration defaultConfiguration) {
		this(context, null, null, defaultConfiguration);
	}

	public AMFChannelFactory(Object context, Transport remotingTransport, Transport messagingTransport) {
		this(context, remotingTransport, messagingTransport, null);
	}

	public AMFChannelFactory(Object context, Transport remotingTransport, Transport messagingTransport, Configuration defaultConfiguration) {
		super(ContentType.AMF, context, remotingTransport, messagingTransport);
		
		this.defaultConfiguration = (defaultConfiguration != null ? defaultConfiguration : Platform.getInstance().newConfiguration());
		this.aliasRegistry = this.defaultConfiguration.getGraniteConfig().getAliasRegistry();
	}

	@Override
	protected AMFRemotingChannel createRemotingChannel(String id, URI uri, int maxConcurrentRequests) {
		return new AMFRemotingChannel(getRemotingTransport(), defaultConfiguration, id, uri, maxConcurrentRequests);
	}
	
	@Override
	protected AMFMessagingChannel createMessagingChannel(String id, URI uri) {
		return new AMFMessagingChannel(getMessagingTransport(), defaultConfiguration, id, uri);
	}
	
	public Configuration getDefaultConfiguration() {
		return defaultConfiguration;
	}
}
