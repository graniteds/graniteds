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
import java.util.Set;

import org.granite.client.messaging.ServerApp;
import org.granite.client.messaging.transport.Transport;
import org.granite.messaging.AliasRegistry;
import org.granite.util.ContentType;

/**
 * @author Franck WOLFF
 */
public interface ChannelFactory {

	ContentType getContentType();
	
	long getDefaultTimeToLive();
	void setDefaultTimeToLive(long defaultTimeToLive);
	
	Object getContext();
	void setContext(Object context);

    void setDefaultChannelBuilder(ChannelBuilder channelBuilder);

	Transport getRemotingTransport();
	void setRemotingTransport(Transport remotingTransport);

    void setMessagingTransport(Transport messagingTransport);
    void setMessagingTransport(String channelType, Transport messagingTransport);
    Transport getMessagingTransport();
	Transport getMessagingTransport(String channelType);

    void setAliasRegistry(AliasRegistry aliasRegistry);
	void setScanPackageNames(Set<String> packageNames);
	
	void start();
	
	void stop();
	void stop(boolean stopTransports);

	RemotingChannel newRemotingChannel(String id, String uri);
	RemotingChannel newRemotingChannel(String id, String uri, int maxConcurrentRequests);
	MessagingChannel newMessagingChannel(String channelType, String id, String uri);

	RemotingChannel newRemotingChannel(String id, URI uri);
	RemotingChannel newRemotingChannel(String id, URI uri, int maxConcurrentRequests);
	MessagingChannel newMessagingChannel(String channelType, String id, URI uri);

    RemotingChannel newRemotingChannel(String id, ServerApp serverApp);
    RemotingChannel newRemotingChannel(String id, ServerApp serverApp, int maxConcurrentRequests);
    MessagingChannel newMessagingChannel(String channelType, String id, ServerApp serverApp);
}