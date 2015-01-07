/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2015 GRANITE DATA SERVICES S.A.S.
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
