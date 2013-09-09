/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of Granite Data Services.
 *
 *   Granite Data Services is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU Library General Public License as published by
 *   the Free Software Foundation; either version 2 of the License, or (at your
 *   option) any later version.
 *
 *   Granite Data Services is distributed in the hope that it will be useful, but
 *   WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *   FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
 *   for more details.
 *
 *   You should have received a copy of the GNU Library General Public License
 *   along with this library; if not, see <http://www.gnu.org/licenses/>.
 */
package org.granite.client.messaging;

import java.util.HashMap;
import java.util.Map;

import org.granite.client.messaging.channel.MessagingChannel;

/**
 * @author Franck WOLFF
 */
public abstract class AbstractTopicAgent implements TopicAgent {
	
	protected final MessagingChannel channel;
	protected final String destination;
	protected final String topic;
	protected final Map<String, Object> defaultHeaders = new HashMap<String, Object>();

	public AbstractTopicAgent(MessagingChannel channel, String destination, String topic) {
		if (channel == null || destination == null)
			throw new NullPointerException("channel and destination cannot be null");
		this.channel = channel;
		this.destination = destination;
		this.topic = topic;
	}

	@Override
	public MessagingChannel getChannel() {
		return channel;
	}

	@Override
	public String getDestination() {
		return destination;
	}

	@Override
	public String getTopic() {
		return topic;
	}
	
	@Override
	public Map<String, Object> getDefaultHeaders() {
		return defaultHeaders;
	}
}
