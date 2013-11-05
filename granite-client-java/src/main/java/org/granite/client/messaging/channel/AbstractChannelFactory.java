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
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import flex.messaging.messages.Message;
import org.granite.client.messaging.ClientAliasRegistry;
import org.granite.client.messaging.ServerApp;
import org.granite.client.messaging.codec.MessagingCodec;
import org.granite.client.messaging.transport.Transport;
import org.granite.client.messaging.transport.TransportException;
import org.granite.client.platform.Platform;
import org.granite.messaging.AliasRegistry;
import org.granite.messaging.amf.AMF0Message;
import org.granite.scan.ServiceLoader;
import org.granite.util.ContentType;

/**
 * @author Franck WOLFF
 */
public abstract class AbstractChannelFactory implements ChannelFactory {
	
	protected final ContentType contentType;

    private String defaultChannelType = ChannelType.LONG_POLLING;
	protected Transport remotingTransport = null;
	protected Transport messagingTransport = null;
    protected Map<String, Transport> messagingTransports = new HashMap<String, Transport>();
	protected Object context = null;
    protected ChannelBuilder defaultChannelBuilder = new DefaultChannelBuilder();

	protected Set<String> scanPackageNames = null;
	protected AliasRegistry aliasRegistry = null;

	protected Long defaultTimeToLive = null;

	
	protected AbstractChannelFactory(ContentType contentType) {
		this(contentType, null, null, null);
	}

	protected AbstractChannelFactory(ContentType contentType, Object context) {
		this(contentType, context, null, null);
	}

	protected AbstractChannelFactory(ContentType contentType, Object context, Transport remotingTransport, Transport messagingTransport) {
		this.contentType = contentType;
		this.context = context;
		this.remotingTransport = remotingTransport;
		this.messagingTransport = messagingTransport;
	}

	public Object getContext() {
		return context;
	}

	public void setContext(Object context) {
		this.context = context;
	}

    public void setDefaultChannelBuilder(ChannelBuilder channelBuilder) {
        this.defaultChannelBuilder = channelBuilder;
    }

	public ContentType getContentType() {
		return contentType;
	}
	
	public long getDefaultTimeToLive() {
		return (defaultTimeToLive != null ? defaultTimeToLive.longValue() : -1L);
	}

	public void setDefaultTimeToLive(long defaultTimeToLive) {
		this.defaultTimeToLive = Long.valueOf(defaultTimeToLive);
	}

	public void setAliasRegistry(AliasRegistry aliasRegistry) {
	    this.aliasRegistry = aliasRegistry;
	}

    public void setDefaultChannelType(String channelType) {
        this.defaultChannelType = channelType;
    }

	public Transport getRemotingTransport() {
		return remotingTransport;
	}

	public void setRemotingTransport(Transport remotingTransport) {
		this.remotingTransport = remotingTransport;
	}

	public void setMessagingTransport(Transport messagingTransport) {
		this.messagingTransport = messagingTransport;
	}

    public void setMessagingTransport(String channelType, Transport messagingTransport) {
        this.messagingTransports.put(channelType, messagingTransport);
    }

    public Transport getMessagingTransport() {
        return messagingTransport;
    }

    public Transport getMessagingTransport(String channelType) {
        if (channelType != null && messagingTransports.containsKey(channelType))
            return messagingTransports.get(channelType);
        return messagingTransport;
    }

	public void setScanPackageNames(String... packageNames) {
		if (packageNames != null)
			this.scanPackageNames = new HashSet<String>(Arrays.asList(packageNames));
		else
			this.scanPackageNames = null;
	}

	public void setScanPackageNames(Set<String> packageNames) {
		this.scanPackageNames = packageNames;
	}


	public void start() {
		Platform platform = Platform.getInstance();
		platform.setContext(context);
		
		if (remotingTransport == null)
			remotingTransport = Platform.getInstance().newRemotingTransport();
		
		if (!remotingTransport.isStarted() && !remotingTransport.start())
			throw new TransportException("Could not start remoting transport: " + remotingTransport);
		
		if (messagingTransport == null) {
			messagingTransport = Platform.getInstance().newMessagingTransport();
			if (messagingTransport == null)
				messagingTransport = remotingTransport;
		}

        for (Map.Entry<String, Transport> me : Platform.getInstance().getMessagingTransports().entrySet())
            messagingTransports.put(me.getKey(), me.getValue());

		if (aliasRegistry == null)
			aliasRegistry = new ClientAliasRegistry();
		
		if (scanPackageNames != null)
			aliasRegistry.scan(scanPackageNames);
	}
	
	public void stop() {
		aliasRegistry = null;
		
		stop(true);
	}

	public void stop(boolean stopTransports) {
		if (stopTransports) {
			if (remotingTransport != null && remotingTransport.isStarted()) {
				remotingTransport.stop();
				remotingTransport = null;
			}
			
			if (messagingTransport != null && messagingTransport.isStarted()) {
				messagingTransport.stop();
				messagingTransport = null;
			}

            for (Transport transport : messagingTransports.values()) {
                if (transport.isStarted())
                    transport.stop();
            }
            messagingTransports.clear();
		}
	}

	@Override
	public RemotingChannel newRemotingChannel(String id, String uri) {
		return newRemotingChannel(id, uri, RemotingChannel.DEFAULT_MAX_CONCURRENT_REQUESTS);
	}
	
	@Override
	public RemotingChannel newRemotingChannel(String id, URI uri) {
		return newRemotingChannel(id, uri, RemotingChannel.DEFAULT_MAX_CONCURRENT_REQUESTS);
	}

    @Override
    public RemotingChannel newRemotingChannel(String id, ServerApp serverApp) {
        return newRemotingChannel(id, serverApp, RemotingChannel.DEFAULT_MAX_CONCURRENT_REQUESTS);
    }

	@Override
	public RemotingChannel newRemotingChannel(String id, String uri, int maxConcurrentRequests) {
		try {
			return newRemotingChannel(id, new URI(uri), maxConcurrentRequests);
		}
		catch (URISyntaxException e) {
			throw new IllegalArgumentException("Bad uri: " + uri, e);
		}
	}
	
	@Override
	public RemotingChannel newRemotingChannel(String id, URI uri, int maxConcurrentRequests) {
        if (getRemotingTransport() == null)
            throw new RuntimeException("Remoting transport not defined, start the ChannelFactory first");
        if (!getRemotingTransport().isStarted() && !getRemotingTransport().start())
            throw new TransportException("Could not start remoting transport: " + getRemotingTransport());

        RemotingChannel channel = defaultChannelBuilder.buildRemotingChannel(getRemotingChannelClass(), id, uri, maxConcurrentRequests, getRemotingTransport(), newMessagingCodec(AMF0Message.class));
		if (defaultTimeToLive != null)
			channel.setDefaultTimeToLive(defaultTimeToLive);
		return channel;
	}

    @Override
    public RemotingChannel newRemotingChannel(String id, ServerApp serverApp, int maxConcurrentRequests) {
        RemotingChannel channel = defaultChannelBuilder.buildRemotingChannel(getRemotingChannelClass(), id, serverApp, maxConcurrentRequests, getRemotingTransport(), newMessagingCodec(AMF0Message.class));
        if (defaultTimeToLive != null)
            channel.setDefaultTimeToLive(defaultTimeToLive);
        return channel;
    }

	protected abstract Class<? extends RemotingChannel> getRemotingChannelClass();

	@Override
	public MessagingChannel newMessagingChannel(String id, String uri) {
		return newMessagingChannel(defaultChannelType, id, uri);
	}

	@Override
	public MessagingChannel newMessagingChannel(String id, URI uri) {
		return newMessagingChannel(defaultChannelType, id, uri);
	}

	@Override
	public MessagingChannel newMessagingChannel(String id, ServerApp serverApp) {
		return newMessagingChannel(defaultChannelType, id, serverApp);
	}

	@Override
	public MessagingChannel newMessagingChannel(String channelType, String id, String uri) {
		try {
			return newMessagingChannel(channelType, id, new URI(uri));
		}
		catch (URISyntaxException e) {
			throw new IllegalArgumentException("Bad uri: " + uri, e);
		}
	}
	
	@Override
	public MessagingChannel newMessagingChannel(String channelType, String id, URI uri) {
        Transport transport = getMessagingTransport(channelType);
        if (transport == null)
            throw new RuntimeException("No transport defined for channel type " + channelType + ", start the ChannelFactory first");
        if (!transport.isStarted() && !transport.start())
            throw new TransportException("Could not start messaging transport: " + transport);
        MessagingCodec<Message[]> codec = newMessagingCodec(Message[].class);
        for (ChannelBuilder builder : ServiceLoader.load(ChannelBuilder.class)) {
            MessagingChannel channel = builder.buildMessagingChannel(channelType, id, uri, transport, codec);
            if (channel != null) {
                if (defaultTimeToLive != null)
                    channel.setDefaultTimeToLive(defaultTimeToLive);
                return channel;
            }
        }
		MessagingChannel channel = defaultChannelBuilder.buildMessagingChannel(channelType, id, uri, transport, codec);
        if (channel == null)
            throw new RuntimeException("Could not build channel for type " + channelType + " and uri " + uri);
		if (defaultTimeToLive != null)
			channel.setDefaultTimeToLive(defaultTimeToLive);
		return channel;
	}

    @Override
    public MessagingChannel newMessagingChannel(String channelType, String id, ServerApp serverApp) {
        Transport transport = getMessagingTransport(channelType);
        if (transport == null)
            throw new RuntimeException("No transport defined for channel type " + channelType + ", start the ChannelFactory first");
        if (!transport.isStarted() && !transport.start())
            throw new TransportException("Could not start messaging transport: " + transport);
        MessagingCodec<Message[]> codec = newMessagingCodec(Message[].class);
        for (ChannelBuilder builder : ServiceLoader.load(ChannelBuilder.class)) {
            MessagingChannel channel = builder.buildMessagingChannel(channelType, id, serverApp, transport, codec);
            if (channel != null) {
                if (defaultTimeToLive != null)
                    channel.setDefaultTimeToLive(defaultTimeToLive);
                return channel;
            }
        }
        MessagingChannel channel = defaultChannelBuilder.buildMessagingChannel(channelType, id, serverApp, transport, codec);
        if (channel == null)
            throw new RuntimeException("Could not build channel for type " + channelType + " and server " + serverApp.getServerName());
        if (defaultTimeToLive != null)
            channel.setDefaultTimeToLive(defaultTimeToLive);
        return channel;
    }

	protected abstract <M> MessagingCodec<M> newMessagingCodec(Class<M> messageClass);
}
