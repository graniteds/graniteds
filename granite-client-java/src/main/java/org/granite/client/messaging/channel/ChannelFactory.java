/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2014 GRANITE DATA SERVICES S.A.S.
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
import java.util.Map;
import java.util.Set;

import org.granite.client.messaging.ServerApp;
import org.granite.client.messaging.transport.Transport;
import org.granite.messaging.AliasRegistry;
import org.granite.util.ContentType;

/**
 * ChannelFactory is the main entry point for using the low-level GraniteDS remoting and messaging API.
 * It can be used to create new channels and attach them to the underlying transports {@link Transport}.
 *
 * The usual way of using ChannelFactory is as follows:
 *
 * <pre>
 * {@code
 * ChannelFactory channelFactory = new JMFChannelFactory();
 * channelFactory.start();
 * RemotingChannel remotingCannel = channelFactory.newRemotingChannel("remoting", "http://localhost:8080/myapp/graniteamf/amf.txt");
 * MessagingChannel messagingChannel = channelFactory.newMessagingChannel("messaging", "http://localhost:8080/myapp/gravityamf/amf.txt");
 * MessagingChannel websocketChannel = channelFactory.newMessagingChannel(ChannelType.WEBSOCKET, "websocket", "ws://localhost:8080/myapp/websocketamf/amf");
 * }
 * </pre>
 *
 * @author Franck WOLFF
 */
public interface ChannelFactory {

    /**
     * The encoding (AMF or JMF) for this ChannelFactory
     * @return the content type
     * @see org.granite.util.ContentType
     */
	ContentType getContentType();

    /**
     * The default time to live defined for channels created by this factory
     * @return time to live in milliseconds
     */
	long getDefaultTimeToLive();

    /**
     * Set the default time to live for the channels created by this factory
     * @param defaultTimeToLive time to live in milliseconds
     */
	void setDefaultTimeToLive(long defaultTimeToLive);

    /**
     * A generic context object used to create transports (actual type of the context depends on the {@link org.granite.client.platform.Platform})
     * @return current context
     * @see org.granite.client.messaging.transport.Transport
     */
	Object getContext();

    /**
     * Set the current context used to create transports (actual type of this context depends on the {@link org.granite.client.platform.Platform})
     * @param context current context
     * @see org.granite.client.messaging.transport.Transport
     */
	void setContext(Object context);

    /**
     * Set the default type of messaging channels (initially ChannelType.LONG_POLLING)
     * @param channelType default type of messaging channels
     * @see org.granite.client.messaging.channel.ChannelType
     */
    void setDefaultChannelType(String channelType);

    /**
     * Return the default type of messaging channels (initially ChannelType.LONG_POLLING)
     * @return default type of messaging channels
     */
    String getDefaultChannelType();

    /**
     * Set the default builder for channels
     * A custom channel builder can also be defined to change the default url mappings of the remoting and messaging channels
     * @param channelBuilder the channel builder
     * @see org.granite.client.messaging.channel.DefaultChannelBuilder
     */
    void setDefaultChannelBuilder(ChannelBuilder channelBuilder);

    /**
     * The transport for remoting channels
     * @return transport
     * @see org.granite.client.messaging.transport.Transport
     */
	Transport getRemotingTransport();

    /**
     * Set the transport for remoting channels
     * @param remotingTransport transport
     * @see org.granite.client.messaging.transport.Transport
     */
	void setRemotingTransport(Transport remotingTransport);

    /**
     * Set the default transport for messaging channels
     * @param messagingTransport transport
     */
    void setMessagingTransport(Transport messagingTransport);

    /**
     * Set the transport for the specified messaging channel type
     * @param channelType channel type
     * @param messagingTransport transport
     * @see org.granite.client.messaging.channel.ChannelType
     * @see org.granite.client.messaging.transport.Transport
     */
    void setMessagingTransport(String channelType, Transport messagingTransport);

    /**
     * The default transport for messaging channels
     * @return the transport
     * @see org.granite.client.messaging.transport.Transport
     */
    Transport getMessagingTransport();
    
    /**
     * The transports for messaging channels
     * @return a map of transports keyed by channel type
     */
    Map<String, Transport> getMessagingTransports();

    /**
     * The transport for the specified messaging channel type
     * @param channelType channel type
     * @return the transport
     * @see org.granite.client.messaging.channel.ChannelType
     * @see org.granite.client.messaging.transport.Transport
     */
	Transport getMessagingTransport(String channelType);

    /**
     * The alias registry for this factory
     * @param aliasRegistry alias registry
     * @see org.granite.messaging.AliasRegistry
     */
    void setAliasRegistry(AliasRegistry aliasRegistry);

    /**
     * Set of package names to scan
     * The classes of these packages will be scanned during the start of this factory to find aliased classes
     * @param packageNames
     * @see org.granite.messaging.AliasRegistry
     */
	void setScanPackageNames(Set<String> packageNames);

    /**
     * Start this ChannelFactory
     * Must be called before trying to create channels and after all properties have been set.
     */
	void start();

    /**
     * Stop this ChannelFactory
     * All defined transports are also stopped and unregistered. After stop, it is recommended to create a new ChannelFactory
     * instead of restarting an existing one.
     */
	void stop();

    /**
     * Stop this ChannelFactory
     * Optionally when stopTransports is true all defined transports are also stopped and unregistered.
     * If the transportshe ChannelFactory should not be restarted after stop,
     * a new ChannelFactory should be created.
     * @param stopTransports true to stop associated transports
     */
	void stop(boolean stopTransports);

    /**
     * Create a remoting channel using the specified uri
     * @param id identifier for this channel
     * @param uri uri of the server to connect to
     * @return a new initialized messaging channel
     * @see org.granite.client.messaging.channel.RemotingChannel
     */
	RemotingChannel newRemotingChannel(String id, String uri);

    /**
     * Create a remoting channel using the specified uri
     * @param id identifier for this channel
     * @param uri uri of the server to connect to
     * @param maxConcurrentRequests maximum number of concurrent requests
     * @return an initializer channel
     * @see org.granite.client.messaging.channel.RemotingChannel
     */
	RemotingChannel newRemotingChannel(String id, String uri, int maxConcurrentRequests);

    /**
     * Create a messaging channel of the default type using the specified uri
     * @param id identifier for this channel
     * @param uri uri of the server to connect to
     * @return an initializer channel
     * @see org.granite.client.messaging.channel.MessagingChannel
     */
	MessagingChannel newMessagingChannel(String id, String uri);

    /**
     * Create a messaging channel of the specified type using the specified uri
     * @param channelType a channel type
     * @param id identifier for this channel
     * @param uri uri of the server to connect to
     * @return a new initialized messaging channel
     * @see org.granite.client.messaging.channel.ChannelType
     * @see org.granite.client.messaging.channel.MessagingChannel
     */
	MessagingChannel newMessagingChannel(String channelType, String id, String uri);

    /**
     * Create a remoting channel using the specified {@link URI}
     * @param id identifier for this channel
     * @param uri uri of the server to connect to
     * @return a new initialized messaging channel
     * @see org.granite.client.messaging.channel.RemotingChannel
     */
	RemotingChannel newRemotingChannel(String id, URI uri);

    /**
     * Create a remoting channel using the specified {@link URI}
     * @param id identifier for this channel
     * @param uri uri of the server to connect to
     * @param maxConcurrentRequests maximum number of concurrent requests
     * @return an initializer channel
     * @see org.granite.client.messaging.channel.RemotingChannel
     */
	RemotingChannel newRemotingChannel(String id, URI uri, int maxConcurrentRequests);

    /**
     * Create a messaging channel of the default type using the specified {@link URI}
     * @param id identifier for this channel
     * @param uri uri of the server to connect to
     * @return a new initialized messaging channel
     * @see org.granite.client.messaging.channel.MessagingChannel
     */
	MessagingChannel newMessagingChannel(String id, URI uri);

    /**
     * Create a messaging channel of the specified type using the specified {@link URI}
     * @param channelType a channel type
     * @param id identifier for this channel
     * @param uri uri of the server to connect to
     * @return a new initialized messaging channel
     * @see org.granite.client.messaging.channel.ChannelType
     * @see org.granite.client.messaging.channel.MessagingChannel
     */
	MessagingChannel newMessagingChannel(String channelType, String id, URI uri);

    /**
     * Create a remoting channel using the specified server application definition
     * @param id identifier for this channel
     * @param serverApp server application to connect to
     * @return an initializer channel
     * @see org.granite.client.messaging.channel.RemotingChannel
     * @see org.granite.client.messaging.ServerApp
     */
    RemotingChannel newRemotingChannel(String id, ServerApp serverApp);

    /**
     * Create a remoting channel using the specified server application definition
     * @param id identifier for this channel
     * @param serverApp server application to connect to
     * @param maxConcurrentRequests maximum number of concurrent requests
     * @return a new initialized remoting channel
     * @see org.granite.client.messaging.channel.RemotingChannel
     * @see org.granite.client.messaging.ServerApp
     */
    RemotingChannel newRemotingChannel(String id, ServerApp serverApp, int maxConcurrentRequests);

    /**
     * Create a messaging channel of the default type using the specified server application definition
     * @param id identifier for this channel
     * @param serverApp server application to connect to
     * @return a new initialized messaging channel
     * @see org.granite.client.messaging.channel.MessagingChannel
     * @see org.granite.client.messaging.ServerApp
     */
    MessagingChannel newMessagingChannel(String id, ServerApp serverApp);

    /**
     * Create a messaging channel of the specified type using the specified server application definition
     * @param channelType a channel type
     * @param id identifier for this channel
     * @param serverApp server application to connect to
     * @return a new initialized messaging channel
     * @see org.granite.client.messaging.channel.ChannelType
     * @see org.granite.client.messaging.channel.MessagingChannel
     * @see org.granite.client.messaging.ServerApp
     */
    MessagingChannel newMessagingChannel(String channelType, String id, ServerApp serverApp);
}