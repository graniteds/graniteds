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

import java.net.URI;

import flex.messaging.messages.Message;
import org.granite.client.messaging.ServerApp;
import org.granite.client.messaging.codec.MessagingCodec;
import org.granite.client.messaging.transport.Transport;
import org.granite.messaging.amf.AMF0Message;

/**
 * SPI for builders of remoting and messaging channels
 * Modules that provide custom channels should also register a ChannelBuilder implementation in the {@link java.util.ServiceLoader} registry
 * (i.e. a file META-INF/services/org.granite.client.messaging.channel.ChannelBuilder)
 * The ChannelFactory will look for all registered channel builders and find the first one able to build the requested channel type.
 * A ChannelBuilder should return null for any of the interface methods it can't fulfill.
 *
 * @author William DRAI
 */
public interface ChannelBuilder {

    /**
     * Build a remoting channel with the specified {@link URI}
     * @param channelClass channel class
     * @param id channel id
     * @param uri channel uri
     * @param maxConcurrentRequests max concurrent requests
     * @param transport transport for the channel
     * @param codec codec to apply to the transmitted messages
     * @return a new remoting channel
     */
    RemotingChannel buildRemotingChannel(Class<? extends RemotingChannel> channelClass, String id, URI uri, int maxConcurrentRequests, Transport transport, MessagingCodec<AMF0Message> codec);

    /**
     * Build a remoting channel with the specified server application {@link org.granite.client.messaging.ServerApp}
     * @param channelClass channel class
     * @param id channel id
     * @param serverApp server application
     * @param maxConcurrentRequests max concurrent requests
     * @param transport transport for the channel
     * @param codec codec to apply to the transmitted messages
     * @return a new remoting channel
     */
    RemotingChannel buildRemotingChannel(Class<? extends RemotingChannel> channelClass, String id, ServerApp serverApp, int maxConcurrentRequests, Transport transport, MessagingCodec<AMF0Message> codec);

    /**
     * Build a messaging channel with the specified {@link URI}
     * @param channelType channel type {@see ChannelType}
     * @param id channel id
     * @param uri channel uri
     * @param transport transport for the channel
     * @param codec codec to apply to the transmitted messages
     * @return a new messaging channel
     */
	MessagingChannel buildMessagingChannel(String channelType, String id, URI uri, Transport transport, MessagingCodec<Message[]> codec);

    /**
     * Build a messaging channel with the specified server application {@link org.granite.client.messaging.ServerApp}
     * @param channelType channel type {@see ChannelType}
     * @param id channel id
     * @param serverApp server application definition
     * @param transport transport for the channel
     * @param codec codec to apply to the transmitted messages
     * @return a new messaging channel
     */
    MessagingChannel buildMessagingChannel(String channelType, String id, ServerApp serverApp, Transport transport, MessagingCodec<Message[]> codec);
}