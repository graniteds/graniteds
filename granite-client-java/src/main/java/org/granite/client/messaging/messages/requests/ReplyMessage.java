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
package org.granite.client.messaging.messages.requests;

import org.granite.client.messaging.messages.AbstractMessage;
import org.granite.client.messaging.messages.Message;

import java.util.Map;

/**
 * @author Franck WOLFF
 */
public final class ReplyMessage extends AbstractTopicRequestMessage {

    private static final long serialVersionUID = 1L;

    private String correlationId = null;
	private Object body = null;

	public ReplyMessage() {
	}

	public ReplyMessage(String destination, String topic, String correlationId, Object body) {
		this(null, destination, topic, correlationId, body);
	}

	public ReplyMessage(String clientId, String destination, String topic, String correlationId, Object body) {
		super(clientId, destination, topic);

        this.correlationId = correlationId;
		this.body = body;
	}

	public ReplyMessage(
            String id,
            String clientId,
            long timestamp,
            long timeToLive,
            Map<String, Object> headers,
            String destination,
            String topic,
            String correlationId,
            Object body) {
		
		super(id, clientId, timestamp, timeToLive, headers, destination, topic);

        this.correlationId = correlationId;
		this.body = body;
	}

	@Override
	public Type getType() {
		return Type.REPLY;
	}

	public Object getBody() {
		return body;
	}

	public void setBody(Object body) {
		this.body = body;
	}

    public String getCorrelationId() {
        return correlationId;
    }

	@Override
	public Message copy() {
		ReplyMessage message = new ReplyMessage();
		
		copy(message);
		
		return message;
	}

	@Override
	protected void copy(AbstractMessage message) {
		super.copy(message);
		
		((ReplyMessage)message).correlationId = correlationId;
		((ReplyMessage)message).body = body;
	}
}
