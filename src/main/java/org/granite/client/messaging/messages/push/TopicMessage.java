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
package org.granite.client.messaging.messages.push;

import java.util.Map;

import org.granite.client.messaging.messages.AbstractMessage;
import org.granite.client.messaging.messages.Message;

/**
 * @author Franck WOLFF
 */
public class TopicMessage extends AbstractMessage {

	private Object data;
	
	public TopicMessage() {
	}

	public TopicMessage(String clientId, Object data) {
		super(clientId);
		
		this.data = data;
	}

	public TopicMessage(
		String id,
		String clientId,
		long timestamp,
		long timeToLive,
		Map<String, Object> headers,
		Object data) {
		
		super(id, clientId, timestamp, timeToLive, headers);
		
		this.data = data;
	}
	
	@Override
	public Type getType() {
		return Type.TOPIC;
	}

	@Override
	public Message copy() {
		TopicMessage message = new TopicMessage();

		super.copy(message);
		
		return message;
	}
	
	public Object getData() {
		return data;
	}
}
