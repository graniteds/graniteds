/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
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
package org.granite.client.messaging.udp;

import java.net.URI;
import java.net.URISyntaxException;

import org.granite.client.messaging.channel.AMFChannelFactory;
import org.granite.client.messaging.channel.ChannelFactory;
import org.granite.client.messaging.channel.JMFChannelFactory;
import org.granite.client.messaging.codec.AMF3MessagingCodec;
import org.granite.client.messaging.codec.JMFAMF3MessagingCodec;
import org.granite.client.messaging.codec.MessagingCodec;

import flex.messaging.messages.Message;

/**
 * @author Franck WOLFF
 */
public class UdpChannelFactoryBridge {

	private final ChannelFactory channelFactory;
	
	public UdpChannelFactoryBridge(ChannelFactory channelFactory) {
		this.channelFactory = channelFactory;
	}

	public ChannelFactory getChannelFactory() {
		return channelFactory;
	}
	
	public UdpMessagingChannel newMessagingChannel(String id, String uri) {
		try {
			return newMessagingChannel(id, new URI(uri));
		}
		catch (URISyntaxException e) {
			throw new IllegalArgumentException("Bad uri: " + uri, e);
		}
	}
	
	public UdpMessagingChannel newMessagingChannel(String id, URI uri) {
		MessagingCodec<Message[]> codec = null;
		
		if (channelFactory instanceof JMFChannelFactory)
			codec = new JMFAMF3MessagingCodec(((JMFChannelFactory)channelFactory).getSharedContext());
		else if (channelFactory instanceof AMFChannelFactory)
			codec = new AMF3MessagingCodec(((AMFChannelFactory)channelFactory).getDefaultConfiguration());
		
		UdpMessagingChannel channel = new UdpMessagingChannelImpl(codec, channelFactory.getMessagingTransport(), id, uri);
		
		long defaultTimeToLive = channelFactory.getDefaultTimeToLive();
		if (defaultTimeToLive != -1L)
			channel.setDefaultTimeToLive(defaultTimeToLive);
		
		return new UdpMessagingChannelImpl(codec, channelFactory.getMessagingTransport(), id, uri);
	}
}
