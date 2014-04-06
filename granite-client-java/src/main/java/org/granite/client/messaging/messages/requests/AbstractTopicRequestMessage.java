/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2014 GRANITE DATA SERVICES S.A.S.
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

import java.util.Map;

import org.granite.client.messaging.messages.AbstractMessage;

/**
 * @author Franck WOLFF
 */
public abstract class AbstractTopicRequestMessage extends AbstractRequestMessage {

	private String destination = null;
	private String topic = null;

	public AbstractTopicRequestMessage() {
	}

	public AbstractTopicRequestMessage(String destination, String topic) {
		this(null, destination, topic);
	}

	public AbstractTopicRequestMessage(String clientId, String destination, String topic) {
		super(clientId);
		
		this.destination = destination;
		this.topic = topic;
	}

	public AbstractTopicRequestMessage(
		String id,
		String clientId,
		long timestamp,
		long timeToLive,
		Map<String, Object> headers,
		String destination,
		String topic) {
		
		super(id, clientId, timestamp, timeToLive, headers);
		
		this.destination = destination;
		this.topic = topic;
	}

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	@Override
	protected void copy(AbstractMessage message) {
		super.copy(message);
		
		((AbstractTopicRequestMessage)message).destination = destination;
		((AbstractTopicRequestMessage)message).topic = topic;
	}
}
