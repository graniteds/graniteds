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

import flex.messaging.messages.Message;

import org.granite.client.messaging.ServerApp;
import org.granite.client.messaging.channel.amf.BaseAMFMessagingChannel;
import org.granite.client.messaging.codec.MessagingCodec;
import org.granite.client.messaging.transport.Transport;
import org.granite.messaging.amf.AMF0Message;
import org.granite.util.TypeUtil;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author William DRAI
 */
public class DefaultChannelBuilder implements ChannelBuilder {

    public static final String LONG_POLLING_CHANNEL_TYPE = "long-polling";
    public static final String WEBSOCKET_CHANNEL_TYPE = "websocket";

    private String graniteUrlMapping = "/graniteamf/amf.txt";
    private String gravityUrlMapping = "/gravityamf/amf.txt";
    private String websocketUrlMapping = "/websocketamf/amf";

    public void setGraniteUrlMapping(String graniteUrlMapping) {
        this.graniteUrlMapping = graniteUrlMapping;
    }

    public void setGravityUrlMapping(String gravityUrlMapping) {
        this.gravityUrlMapping = gravityUrlMapping;
    }

    public void setWebsocketUrlMapping(String websocketUrlMapping) {
        this.websocketUrlMapping = websocketUrlMapping;
    }

    public RemotingChannel buildRemotingChannel(Class<? extends RemotingChannel> channelClass, String id, URI uri, int maxConcurrentRequests, Transport transport, MessagingCodec<AMF0Message> codec) {
        try {
            return TypeUtil.newInstance(channelClass, new Class<?>[] { Transport.class, MessagingCodec.class, String.class, URI.class, int.class },
                    new Object[] { transport, codec, id, uri, maxConcurrentRequests });
        }
        catch (Exception e) {
            throw new RuntimeException("Could not build remoting channel for class " + channelClass, e);
        }
    }

    @SuppressWarnings("unchecked")
    public RemotingChannel buildRemotingChannel(Class<? extends RemotingChannel> channelClass, String id, ServerApp serverApp, int maxConcurrentRequest, Transport transport, MessagingCodec<AMF0Message> codec) {
        String uri = (serverApp.getSecure() ? "https" : "http") + "://" + serverApp.getServerName() + ":" + serverApp.getServerPort() + serverApp.getContextRoot() + graniteUrlMapping;

        try {
            return buildRemotingChannel(channelClass, id, new URI(uri), maxConcurrentRequest, transport, codec);
        }
        catch (URISyntaxException e) {
            throw new RuntimeException("Bad uri: " + uri, e);
        }
    }

    @SuppressWarnings("unchecked")
	public MessagingChannel buildMessagingChannel(String channelType, String id, URI uri, Transport transport, MessagingCodec<Message[]> codec) {
        if (!(channelType.equals(LONG_POLLING_CHANNEL_TYPE) || channelType.equals(WEBSOCKET_CHANNEL_TYPE)))
            return null;
        
        return new BaseAMFMessagingChannel(codec, transport, id, uri);
    }

    @SuppressWarnings("unchecked")
    public MessagingChannel buildMessagingChannel(String channelType, String id, ServerApp serverApp, Transport transport, MessagingCodec<Message[]> codec) {
        String uri;
        if (channelType.equals(LONG_POLLING_CHANNEL_TYPE))
            uri = (serverApp.getSecure() ? "https" : "http") + "://" + serverApp.getServerName() + ":" + serverApp.getServerPort() + serverApp.getContextRoot() + gravityUrlMapping;
        else if (channelType.equals(WEBSOCKET_CHANNEL_TYPE))
            uri = (serverApp.getSecure() ? "wss" : "ws") + "://" + serverApp.getServerName() + ":" + serverApp.getServerPort() + serverApp.getContextRoot() + websocketUrlMapping;
        else
            return null;

        try {
            return buildMessagingChannel(channelType, id, new URI(uri), transport, codec);
        }
        catch (URISyntaxException e) {
            throw new RuntimeException("Bad uri: " + uri, e);
        }
    }
}