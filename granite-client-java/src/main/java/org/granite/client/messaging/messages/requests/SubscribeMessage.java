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
package org.granite.client.messaging.messages.requests;

import java.util.Map;

import org.granite.client.messaging.messages.Message;

/**
 * @author Franck WOLFF
 */
public final class SubscribeMessage extends AbstractTopicRequestMessage {

    private static final long serialVersionUID = 1L;
	
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
