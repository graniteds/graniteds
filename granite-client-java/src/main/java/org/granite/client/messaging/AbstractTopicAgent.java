/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2014 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of the Granite Data Services Platform.
 *
 *                               ***
 *
 *   Community License: GPL 3.0
 *
 *   This file is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published
 *   by the Free Software Foundation, either version 3 of the License,
 *   or (at your option) any later version.
 *
 *   This file is distributed in the hope that it will be useful, but
 *   WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *                               ***
 *
 *   Available Commercial License: GraniteDS SLA 1.0
 *
 *   This is the appropriate option if you are creating proprietary
 *   applications and you are not prepared to distribute and share the
 *   source code of your application under the GPL v3 license.
 *
 *   Please visit http://www.granitedataservices.com/license for more
 *   details.
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
