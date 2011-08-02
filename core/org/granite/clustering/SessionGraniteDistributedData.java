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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpSession;

import org.granite.context.GraniteContext;
import org.granite.messaging.webapp.HttpGraniteContext;

import flex.messaging.messages.AsyncMessage;
import flex.messaging.messages.CommandMessage;

/**
 * @author Franck WOLFF
 */
public class SessionGraniteDistributedData implements GraniteDistributedData {

	private static final String KEY_PREFIX = "__GDD__";
	private static final String CREDENTIALS_KEY = KEY_PREFIX + "CREDENTIALS";
	private static final String CHANNELID_KEY_PREFIX = KEY_PREFIX + "CHANNELID.";
	private static final String SUBSCRIPTION_KEY_PREFIX = KEY_PREFIX + "SUBSCRIPTION.";
	private static final String DESTINATION_CLIENTID_KEY_PREFIX = KEY_PREFIX + "DESTINATION_CLIENTID.";
	private static final String DESTINATION_SUBSCRIPTIONID_KEY_PREFIX = KEY_PREFIX + "DESTINATION_SUBSCRIPTIONID.";
	private static final String DESTINATION_SELECTOR_KEY_PREFIX = KEY_PREFIX + "DESTINATION_SELECTOR.";
	
	private final HttpSession session;

	private static HttpSession getSession() {
		GraniteContext context = GraniteContext.getCurrentInstance();
		if (context instanceof HttpGraniteContext)
			return ((HttpGraniteContext)context).getSession(false);
		return null;
	}
	
	public SessionGraniteDistributedData() {
		this(getSession());
	}
	
	public SessionGraniteDistributedData(HttpSession session) {
		if (session == null)
			throw new NullPointerException("HTTP session cannot be null");
		this.session = session;
	}
	
	public Object getCredentials() {
		return session.getAttribute(CREDENTIALS_KEY);
	}

	public boolean hasCredentials() {
		return (getCredentials() != null);
	}

	public void setCredentials(Object credentials) {
		if (credentials != null)
			session.setAttribute(CREDENTIALS_KEY, credentials);
		else
			removeCredentials();
	}

	public void removeCredentials() {
		session.removeAttribute(CREDENTIALS_KEY);
	}

	public void addChannelId(String channelId) {
		if (channelId == null)
			throw new NullPointerException("channelId cannot be null");
		session.setAttribute(CHANNELID_KEY_PREFIX + channelId, Boolean.TRUE);
	}

	public boolean hasChannelId(String channelId) {
		if (channelId == null)
			return false;
		return (session.getAttribute(CHANNELID_KEY_PREFIX + channelId) != null);
	}

	public void removeChannelId(String channelId) {
		if (channelId == null)
			return;
		session.removeAttribute(CHANNELID_KEY_PREFIX + channelId);
		clearSubscriptions(channelId);
	}

	public Set<String> getChannelIds() {
		Set<String> channelIds = new HashSet<String>();
		for (Enumeration<String> e = session.getAttributeNames(); e.hasMoreElements(); ) {
			String key = e.nextElement();
			if (key.startsWith(CHANNELID_KEY_PREFIX))
				channelIds.add(key.substring(CHANNELID_KEY_PREFIX.length()));
		}
		return channelIds;
	}

	public void clearChannelIds() {
		Set<String> channelIds = getChannelIds();
		for (String channelId : channelIds)
			removeChannelId(channelId);
	}

	public void addSubcription(String channelId, CommandMessage message) {
		if (channelId == null || message == null)
			throw new IllegalArgumentException("channelId and message cannot be null");
		if (!hasChannelId(channelId))
			throw new IllegalArgumentException("Unknown channelId: " + channelId);
		if (channelId.indexOf('.') != -1)
			throw new IllegalArgumentException("Invalid channelId (should not contain '.' characters): " + channelId);
		String subscriptionId = (String)message.getHeader(AsyncMessage.DESTINATION_CLIENT_ID_HEADER);
		if (subscriptionId == null)
			throw new IllegalArgumentException("Subscription id cannot be null: " + message);
		session.setAttribute(SUBSCRIPTION_KEY_PREFIX + channelId + '.' + subscriptionId, message);
	}

	public boolean hasSubcription(String channelId, String subscriptionId) {
		if (channelId == null || subscriptionId == null)
			return false;
		return (session.getAttribute(SUBSCRIPTION_KEY_PREFIX + channelId + '.' + subscriptionId) != null);
	}

	public void removeSubcription(String channelId, String subscriptionId) {
		if (channelId == null || subscriptionId == null)
			return;
		session.removeAttribute(SUBSCRIPTION_KEY_PREFIX + channelId + '.' + subscriptionId);
	}

	public List<CommandMessage> getSubscriptions(String channelId) {
		if (channelId == null)
			return Collections.emptyList();
		String channelSubscriptionKeyPrefix = SUBSCRIPTION_KEY_PREFIX + channelId + '.';
		List<CommandMessage> subscriptions = new ArrayList<CommandMessage>();
		for (Enumeration<String> e = session.getAttributeNames(); e.hasMoreElements(); ) {
			String key = e.nextElement();
			if (key.startsWith(channelSubscriptionKeyPrefix)) {
				CommandMessage subscription = (CommandMessage)session.getAttribute(key);
				subscriptions.add(subscription);
			}
		}
		return subscriptions;
	}

	public void clearSubscriptions(String channelId) {
		if (channelId == null)
			return;
		String channelSubscriptionKeyPrefix = SUBSCRIPTION_KEY_PREFIX + channelId + '.';
		for (Enumeration<String> e = session.getAttributeNames(); e.hasMoreElements(); ) {
			String key = e.nextElement();
			if (key.startsWith(channelSubscriptionKeyPrefix))
				session.removeAttribute(key);
		}
	}
	
	
	public String getDestinationClientId(String destination) {
		return (String)session.getAttribute(DESTINATION_CLIENTID_KEY_PREFIX + destination);
	}
	
	public void setDestinationClientId(String destination, String clientId) {
		session.setAttribute(DESTINATION_CLIENTID_KEY_PREFIX + destination, clientId);
	}
	
	public String getDestinationSubscriptionId(String destination) {
		return (String)session.getAttribute(DESTINATION_SUBSCRIPTIONID_KEY_PREFIX + destination);
	}
	
	public void setDestinationSubscriptionId(String destination, String subscriptionId) {
		session.setAttribute(DESTINATION_SUBSCRIPTIONID_KEY_PREFIX + destination, subscriptionId);
	}
	
	public String getDestinationSelector(String destination) {
		return (String)session.getAttribute(DESTINATION_SELECTOR_KEY_PREFIX + destination);
	}
	
	public void setDestinationSelector(String destination, String selector) {
		session.setAttribute(DESTINATION_SELECTOR_KEY_PREFIX + destination, selector);
	}
}
