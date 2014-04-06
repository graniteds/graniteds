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
public final class PublishMessage extends AbstractTopicRequestMessage {

    private static final long serialVersionUID = 1L;
	
	private Object body = null;

	public PublishMessage() {
	}

	public PublishMessage(String destination, String topic, Object body) {
		this(null, destination, topic, body);
	}

	public PublishMessage(String clientId, String destination, String topic, Object body) {
		super(clientId, destination, topic);
		
		this.body = body;
	}

	public PublishMessage(
		String id,
		String clientId,
		long timestamp,
		long timeToLive,
		Map<String, Object> headers,
		String destination,
		String topic,
		Object body) {
		
		super(id, clientId, timestamp, timeToLive, headers, destination, topic);
		
		this.body = body;
	}

	@Override
	public Type getType() {
		return Type.PUBLISH;
	}

	public Object getBody() {
		return body;
	}

	public void setBody(Object body) {
		this.body = body;
	}

	@Override
	public Message copy() {
		PublishMessage message = new PublishMessage();
		
		copy(message);
		
		return message;
	}
}
