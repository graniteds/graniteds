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

import java.util.Map;

import org.granite.client.messaging.channel.MessagingChannel;
import org.granite.client.messaging.channel.ResponseMessageFuture;
import org.granite.client.messaging.messages.requests.PublishMessage;

/**
 * Producer class that allows to publish messages on a remote pub/sub destination
 * All {@link #publish} methods are asynchronous and won't block the thread initiating the call.
 *
 * <pre>
 * {@code
 * Producer producer = new Producer(messagingChannel, "myDestination", "myTopic");
 * producer.publish("some message");
 * }
 * </pre>
 *
 * @author Franck WOLFF
 */
public class Producer extends AbstractTopicAgent {

    /**
     * Create a producer for the specified channel and destination
     * @param channel messaging channel
     * @param destination remote destination
     * @param topic subtopic to which the producer sends its messages
     */
	public Producer(MessagingChannel channel, String destination, String topic) {
		super(channel, destination, topic);
	}

    /**
     * Publish a message on the channel
     * @param message message (any object or String)
     * @param listeners array of listeners that will be notified when the message is delivered
     * @return future triggered when the message is delivered
     */
	public ResponseMessageFuture publish(Object message, ResponseListener...listeners) {
		PublishMessage publishMessage = new PublishMessage(destination, topic, message);
		publishMessage.getHeaders().putAll(defaultHeaders);
		return channel.send(publishMessage, listeners);
	}

    /**
     * Publish a message on the channel with the specified headers
     * @param message message (any object or String)
     * @param headers headers for this message
     * @param listeners array of listeners that will be notified when the message is delivered
     * @return future triggered when the message is delivered
     */
	public ResponseMessageFuture publish(Object message, Map<String, Object> headers, ResponseListener...listeners) {
		PublishMessage publishMessage = new PublishMessage(destination, topic, message);
		publishMessage.getHeaders().putAll(defaultHeaders);
		publishMessage.getHeaders().putAll(headers);
		return channel.send(publishMessage, listeners);
	}
}
