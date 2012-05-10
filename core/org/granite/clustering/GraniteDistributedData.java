/*
  GRANITE DATA SERVICES
  Copyright (C) 2011 GRANITE DATA SERVICES S.A.S.

  This file is part of Granite Data Services.

  Granite Data Services is free software; you can redistribute it and/or modify
  it under the terms of the GNU Library General Public License as published by
  the Free Software Foundation; either version 2 of the License, or (at your
  option) any later version.

  Granite Data Services is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
  for more details.

  You should have received a copy of the GNU Library General Public License
  along with this library; if not, see <http://www.gnu.org/licenses/>.
*/

package org.granite.clustering;

import java.util.List;
import java.util.Set;

import flex.messaging.messages.CommandMessage;

/**
 * @author Franck WOLFF
 */
public interface GraniteDistributedData {

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
	
	public void addChannelId(String channelId);
	public boolean hasChannelId(String channelId);
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
	
	public String getDestinationSelector(String destination);
	public void setDestinationSelector(String destination, String selector);
}
