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

import flex.messaging.messages.Message;
import org.granite.client.messaging.ServerApp;
import org.granite.client.messaging.channel.ChannelBuilder;
import org.granite.client.messaging.channel.ChannelType;
import org.granite.client.messaging.channel.MessagingChannel;
import org.granite.client.messaging.channel.RemotingChannel;
import org.granite.client.messaging.codec.MessagingCodec;
import org.granite.client.messaging.transport.Transport;
import org.granite.messaging.amf.AMF0Message;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author William DRAI
 */
public class UdpChannelBuilder implements ChannelBuilder {

    private String gravityUrlMapping = "/gravityamf/amf.txt";

    public void setGravityUrlMapping(String gravityUrlMapping) {
        this.gravityUrlMapping = gravityUrlMapping;
    }

    @Override
    public RemotingChannel buildRemotingChannel(Class<? extends RemotingChannel> channelClass, String id, URI uri, int maxConcurrentRequests, Transport transport, MessagingCodec<AMF0Message> codec) {
        return null;
    }

    @Override
    public RemotingChannel buildRemotingChannel(Class<? extends RemotingChannel> channelClass, String id, ServerApp serverApp, int maxConcurrentRequests, Transport transport, MessagingCodec<AMF0Message> codec) {
        return null;
    }

    @Override
    public MessagingChannel buildMessagingChannel(String channelType, String id, URI uri, Transport transport, MessagingCodec<Message[]> codec) {
        if (!channelType.equals(ChannelType.UDP))
            return null;

        return new UdpMessagingChannelImpl(codec, transport, id, uri);
    }

    @Override
    public MessagingChannel buildMessagingChannel(String channelType, String id, ServerApp serverApp, Transport transport, MessagingCodec<Message[]> codec) {
        if (!channelType.equals(ChannelType.UDP))
            return null;

        String uri = (serverApp.getSecure() ? "https" : "http") + "://" + serverApp.getServerName() + ":" + serverApp.getServerPort() + serverApp.getContextRoot() + gravityUrlMapping;

        try {
            return new UdpMessagingChannelImpl(codec, transport, id, new URI(uri));
        }
        catch (URISyntaxException e) {
            throw new RuntimeException("Bad uri: " + uri, e);
        }
    }
}
