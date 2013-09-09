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
package org.granite.client.messaging.messages.requests;

import java.util.Map;

import org.granite.client.messaging.messages.Message;

/**
 * @author Franck WOLFF
 */
public final class SubscribeMessage extends AbstractTopicRequestMessage {

	private String selector = null;
	
	public SubscribeMessage() {
	}

	public SubscribeMessage(String destination, String topic, String selector) {
		this(null, destination, topic, selector);
	}

	public SubscribeMessage(String clientId, String destination, String topic, String selector) {
		super(clientId, destination, topic);
		
		this.selector = selector;
	}

	public SubscribeMessage(
		String id,
		String clientId,
		long timestamp,
		long timeToLive,
		Map<String, Object> headers,
		String destination, String topic,
		String selector) {
		
		super(id, clientId, timestamp, timeToLive, headers, destination, topic);
		
		this.selector = selector;
	}

	public String getSelector() {
		return selector;
	}

	public void setSelector(String selector) {
		this.selector = selector;
	}

	@Override
	public Type getType() {
		return Type.SUBSCRIBE;
	}

	@Override
	public Message copy() {
		SubscribeMessage message = new SubscribeMessage();
		
		copy(message);
		
		message.selector = selector;
		
		return message;
	}
}
