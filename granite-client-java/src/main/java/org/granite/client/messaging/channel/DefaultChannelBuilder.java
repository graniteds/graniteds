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
 * Default implementation of ChannelBuilder handling the built-in channel types (long polling and websocket)
 * This can also be used to specify custom url mappings for the server channel uris:
 *
 * channelFactory.setChannelBuilder(new DefaultChannelBuilder("/customremoting/amf", "/customgravity/amf", "/customws/amf");
 * channelFactory.start();
 *
 * @author William DRAI
 */
public class DefaultChannelBuilder implements ChannelBuilder {

    private String graniteUrlMapping = "/graniteamf/amf.txt";
    private String gravityUrlMapping = "/gravityamf/amf.txt";
    private String websocketUrlMapping = "/websocketamf/amf";

    /**
     * Create a channel builder with conventional defaults for url mappings
     */
    public DefaultChannelBuilder() {
    }

    /**
     * Create a channel builder with specified url mappings
     * @param graniteUrlMapping url mapping for remoting
     * @param gravityUrlMapping url mapping for long polling
     * @param websocketUrlMapping url mapping for websocket
     */
    public DefaultChannelBuilder(String graniteUrlMapping, String gravityUrlMapping, String websocketUrlMapping) {
        this.graniteUrlMapping = graniteUrlMapping;
        this.gravityUrlMapping = gravityUrlMapping;
        this.websocketUrlMapping = websocketUrlMapping;
    }

    /**
     * Set url mapping for remoting
     * @param graniteUrlMapping url mapping
     */
    public void setGraniteUrlMapping(String graniteUrlMapping) {
        this.graniteUrlMapping = graniteUrlMapping;
    }

    /**
     * Set url mapping for long polling
     * @param gravityUrlMapping url mapping
     */
    public void setGravityUrlMapping(String gravityUrlMapping) {
        this.gravityUrlMapping = gravityUrlMapping;
    }

    /**
     * Set url mapping for websocket
     * @param websocketUrlMapping url mapping
     */
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

    public RemotingChannel buildRemotingChannel(Class<? extends RemotingChannel> channelClass, String id, ServerApp serverApp, int maxConcurrentRequest, Transport transport, MessagingCodec<AMF0Message> codec) {
        String uri = (serverApp.getSecure() ? "https" : "http") + "://" + serverApp.getServerName() + ":" + serverApp.getServerPort() + serverApp.getContextRoot() + graniteUrlMapping;

        try {
            return buildRemotingChannel(channelClass, id, new URI(uri), maxConcurrentRequest, transport, codec);
        }
        catch (URISyntaxException e) {
            throw new RuntimeException("Bad uri: " + uri, e);
        }
    }

	public MessagingChannel buildMessagingChannel(String channelType, String id, URI uri, Transport transport, MessagingCodec<Message[]> codec) {
        if (!(channelType.equals(ChannelType.LONG_POLLING) || channelType.equals(ChannelType.WEBSOCKET)))
            return null;
        
        return new BaseAMFMessagingChannel(codec, transport, id, uri);
    }

    public MessagingChannel buildMessagingChannel(String channelType, String id, ServerApp serverApp, Transport transport, MessagingCodec<Message[]> codec) {
        String uri;
        if (channelType.equals(ChannelType.LONG_POLLING))
            uri = (serverApp.getSecure() ? "https" : "http") + "://" + serverApp.getServerName() + ":" + serverApp.getServerPort() + serverApp.getContextRoot() + gravityUrlMapping;
        else if (channelType.equals(ChannelType.WEBSOCKET))
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