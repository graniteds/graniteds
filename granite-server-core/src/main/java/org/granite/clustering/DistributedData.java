/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2015 GRANITE DATA SERVICES S.A.S.
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
package org.granite.clustering;

import java.util.List;
import java.util.Set;

import flex.messaging.messages.CommandMessage;

/**
 * @author Franck WOLFF
 */
public interface DistributedData {

	// SecurityService credentials.
	
	public Object getCredentials();
	public boolean hasCredentials();
	public void setCredentials(Object credentials);
	public void removeCredentials();
	
	public String getCredentialsCharset();
	public boolean hasCredentialsCharset();
	public void setCredentialsCharset(String credentialsCharset);
	public void removeCredentialsCharset();
	
	// Gravity channels/subscriptions.
	
	public void addChannelId(String channelId, String channelFactoryClassName, String clientType);
	public boolean hasChannelId(String channelId);
	public String getChannelFactoryClassName(String channelId);
    public String getChannelClientType(String channelId);
	public void removeChannelId(String channelId);
	public Set<String> getChannelIds();
	public void clearChannelIds();
	
	public void addSubcription(String channelId, CommandMessage message);
	public boolean hasSubcription(String channelId, String subscriptionId);
	public void removeSubcription(String channelId, String subscriptionId);
	public List<CommandMessage> getSubscriptions(String channelId);
	public void clearSubscriptions(String channelId);
	
	// Gravity/Tide data management
	
	public String getDestinationClientId(String destination);
	public void setDestinationClientId(String destination, String clientId);
	
	public String getDestinationSubscriptionId(String destination);
	public void setDestinationSubscriptionId(String destination, String subscriptionId);
	
	public Object[] getDestinationDataSelectors(String destination);
	public void setDestinationDataSelectors(String destination, Object[] selectors);
	
	public String getDestinationSelector(String destination);
	public void setDestinationSelector(String destination, String selector);
}
