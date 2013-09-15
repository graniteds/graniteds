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
import java.util.HashSet;
import java.util.Set;

import org.granite.client.messaging.ClientAliasRegistry;
import org.granite.client.messaging.transport.Transport;
import org.granite.client.messaging.transport.TransportException;
import org.granite.client.platform.Platform;
import org.granite.messaging.AliasRegistry;
import org.granite.util.ContentType;

/**
 * @author Franck WOLFF
 */
public abstract class AbstractChannelFactory implements ChannelFactory {
	
	protected final ContentType contentType;
	
	protected Transport remotingTransport = null;
	protected Transport messagingTransport = null;
	protected Object context = null;

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

	public Transport getRemotingTransport() {
		return remotingTransport;
	}

	public void setRemotingTransport(Transport remotingTransport) {
		this.remotingTransport = remotingTransport;
	}

	public Transport getMessagingTransport() {
		return messagingTransport;
	}

	public void setMessagingTransport(Transport messagingTransport) {
		this.messagingTransport = messagingTransport;
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
		else if (!messagingTransport.isStarted() && !messagingTransport.start())
			throw new TransportException("Could not start messaging transport: " + messagingTransport);
		
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
		RemotingChannel channel = createRemotingChannel(id, uri, maxConcurrentRequests);
		if (defaultTimeToLive != null)
			channel.setDefaultTimeToLive(defaultTimeToLive);
		return channel;
	}
	
	protected abstract RemotingChannel createRemotingChannel(String id, URI uri, int maxConcurrentRequests);
	
	@Override
	public MessagingChannel newMessagingChannel(String id, String uri) {
		try {
			return newMessagingChannel(id, new URI(uri));
		}
		catch (URISyntaxException e) {
			throw new IllegalArgumentException("Bad uri: " + uri, e);
		}
	}
	
	@Override
	public MessagingChannel newMessagingChannel(String id, URI uri) {
		MessagingChannel channel = createMessagingChannel(id, uri);
		if (defaultTimeToLive != null)
			channel.setDefaultTimeToLive(defaultTimeToLive);
		return channel;
	}
	
	protected abstract MessagingChannel createMessagingChannel(String id, URI uri);
}
