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

import java.util.Map;

import org.granite.client.messaging.channel.MessagingChannel;
import org.granite.client.messaging.channel.ResponseMessageFuture;
import org.granite.client.messaging.messages.requests.PublishMessage;

/**
 * @author Franck WOLFF
 */
public class Producer extends AbstractTopicAgent {

	public Producer(MessagingChannel channel, String destination, String topic) {
		super(channel, destination, topic);
	}

	public ResponseMessageFuture publish(Object message, ResponseListener...listeners) {
		PublishMessage publishMessage = new PublishMessage(destination, topic, message);
		publishMessage.getHeaders().putAll(defaultHeaders);
		return channel.send(publishMessage, listeners);
	}

	public ResponseMessageFuture publish(Object message, Map<String, Object> headers, ResponseListener...listeners) {
		PublishMessage publishMessage = new PublishMessage(destination, topic, message);
		publishMessage.getHeaders().putAll(defaultHeaders);
		publishMessage.getHeaders().putAll(headers);
		return channel.send(publishMessage, listeners);
	}
}
