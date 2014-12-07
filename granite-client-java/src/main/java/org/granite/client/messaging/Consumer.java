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
package org.granite.client.messaging;

import java.util.concurrent.ConcurrentHashMap;

import org.granite.client.messaging.channel.MessagingChannel;
import org.granite.client.messaging.channel.ResponseMessageFuture;
import org.granite.client.messaging.events.IssueEvent;
import org.granite.client.messaging.events.ResultEvent;
import org.granite.client.messaging.events.TopicMessageEvent;
import org.granite.client.messaging.messages.push.TopicMessage;
import org.granite.client.messaging.messages.requests.ReplyMessage;
import org.granite.client.messaging.messages.requests.SubscribeMessage;
import org.granite.client.messaging.messages.requests.UnsubscribeMessage;
import org.granite.logging.Logger;

/**
 * Consumer class that allows to receive messages from a remote pub/sub destination
 *
 * <pre>
 * {@code
 * Consumer consumer = new Consumer(messagingChannel, "myDestination", "myTopic");
 * consumer.subscribe().get();
 * consumer.addMessageListener(new TopicMessageListener() {
 *     public void onMessage(TopicMessageEvent event) {
 *         System.out.println("Received: " + event.getData());
 *     }
 * });
 * }
 * </pre>
 *
 * @author Franck WOLFF
 */
public class Consumer extends AbstractTopicAgent {
	
	private static final Logger log = Logger.getLogger(Consumer.class);

	private final ConcurrentHashMap<TopicMessageListener, Boolean> listeners = new ConcurrentHashMap<TopicMessageListener, Boolean>();
	private final ConcurrentHashMap<TopicSubscriptionListener, Boolean> subscriptionListeners = new ConcurrentHashMap<TopicSubscriptionListener, Boolean>();
	
	private String subscriptionId = null;
	private String selector = null;

    /**
     * Create a consumer for the specified channel and destination
     * @param channel messaging channel
     * @param destination remote destination
     * @param topic subtopic to which the producer sends its messages
     */
	public Consumer(MessagingChannel channel, String destination, String topic) {
		super(channel, destination, topic);
	}

    /**
     * Message selector for this consumer
     * @return selector
     */
	public String getSelector() {
		return selector;
	}

    /**
     * Set the message selector for this consumer
     * This must be set before subscribing to the destination
     * It is necessary to resubscribe to the destination after changing its value
     * @param selector selector
     */
	public void setSelector(String selector) {
		this.selector = selector;
	}

    /**
     * Is this consumer subscribed ?
     * @return true if subscribed
     */
	public boolean isSubscribed() {
		return subscriptionId != null;
	}

    /**
     * Current subscription id received from the server
     * @return subscription id
     */
	public String getSubscriptionId() {
		return subscriptionId;
	}

    /**
     * Subscribe to the remote destination
     * @param listeners array of listeners notified when the subscription is processed
     * @return future triggered when subscription is processed
     */
	public ResponseMessageFuture subscribe(ResponseListener...listeners) {
		return subscribe(null, listeners);
	}
	
	public ResponseMessageFuture resubscribe(ResponseListener...listeners) {
		return subscribe(subscriptionId, listeners);
	}
	
	public ResponseMessageFuture subscribe(final String subscriptionId, ResponseListener...listeners) {
		SubscribeMessage subscribeMessage = new SubscribeMessage(destination, topic, selector);
		subscribeMessage.getHeaders().putAll(defaultHeaders);
		if (subscriptionId != null)
			subscribeMessage.setSubscriptionId(subscriptionId);
		
		final Consumer consumer = this;
		ResponseListener listener = new ResultIssuesResponseListener() {
			
			@Override
			public void onResult(ResultEvent event) {
				Consumer.this.subscriptionId = (String)event.getResult();
				channel.addConsumer(consumer);
				
				for (TopicSubscriptionListener subscriptionListener : subscriptionListeners.keySet())
					subscriptionListener.onSubscriptionSuccess(Consumer.this, event, subscriptionId);
			}
			
			@Override
			public void onIssue(IssueEvent event) {
				log.error("Subscription failed %s: %s", consumer, event);
				
				for (TopicSubscriptionListener subscriptionListener : subscriptionListeners.keySet())
					subscriptionListener.onSubscriptionFault(Consumer.this, event);
			}
		};
		
		if (listeners == null || listeners.length == 0)
			listeners = new ResponseListener[]{listener};
		else {
			ResponseListener[] tmp = new ResponseListener[listeners.length + 1];
			System.arraycopy(listeners, 0, tmp, 1, listeners.length);
			tmp[0] = listener;
			listeners = tmp;
		}
		
		for (TopicSubscriptionListener subscriptionListener : subscriptionListeners.keySet())
			subscriptionListener.onSubscribing(this);
		
		return channel.send(subscribeMessage, listeners);
	}

    /**
     * Unubscribe from the remote destination
     * @param listeners array of listeners notified when the unsubscription is processed
     * @return future triggered when unsubscription is processed
     */
	public ResponseMessageFuture unsubscribe(ResponseListener...listeners) {
		UnsubscribeMessage unsubscribeMessage = new UnsubscribeMessage(destination, topic, subscriptionId);
		unsubscribeMessage.getHeaders().putAll(defaultHeaders);
		
		final Consumer consumer = this;
		ResponseListener listener = new ResultIssuesResponseListener() {
			
			@Override
			public void onResult(ResultEvent event) {
				channel.removeConsumer(consumer);
				
				for (TopicSubscriptionListener subscriptionListener : subscriptionListeners.keySet())
					subscriptionListener.onUnsubscriptionSuccess(Consumer.this, event, subscriptionId);
				
				subscriptionId = null;
			}
			
			@Override
			public void onIssue(IssueEvent event) {
				log.error("Unsubscription failed %s: %s", consumer, event);
				channel.removeConsumer(consumer);
				
				for (TopicSubscriptionListener subscriptionListener : subscriptionListeners.keySet())
					subscriptionListener.onUnsubscriptionFault(Consumer.this, event, subscriptionId);
				
				subscriptionId = null;
			}
		};
		
		if (listeners == null || listeners.length == 0)
			listeners = new ResponseListener[]{listener};
		else {
			ResponseListener[] tmp = new ResponseListener[listeners.length + 1];
			System.arraycopy(listeners, 0, tmp, 0, listeners.length);
			tmp[listeners.length] = listener;
			listeners = tmp;
		}
		
		for (TopicSubscriptionListener subscriptionListener : subscriptionListeners.keySet())
			subscriptionListener.onUnsubscribing(this);
		
		return channel.send(unsubscribeMessage, listeners);
	}

    /**
     * Register a message listener for this consumer
     * @param listener message listener
     */
	public void addMessageListener(TopicMessageListener listener) {
		listeners.putIfAbsent(listener, Boolean.TRUE);
	}
	
    /**
     * Unregister a message listener for this consumer
     * @param listener message listener
     * @return true if the listener was actually removed (if it was registered before)
     */
	public boolean removeMessageListener(TopicMessageListener listener) {
		return listeners.remove(listener) != null;
	}

    /**
     * Register a subscription listener for this consumer
     * @param listener subscription listener
     */
	public void addSubscriptionListener(TopicSubscriptionListener listener) {
		subscriptionListeners.putIfAbsent(listener, Boolean.TRUE);
	}
	
    /**
     * Unregister a subscription listener for this consumer
     * @param listener subscription listener
     * @return true if the listener was actually removed (if it was registered before)
     */
	public boolean removeSubscriptionListener(TopicSubscriptionListener listener) {
		return subscriptionListeners.remove(listener) != null;
	}

    /**
     * Called when the channel is disconnected
     */
	public void onDisconnect() {
		subscriptionId = null;
	}

    /**
     * Called when a message is received on the channel
     * @param message message received
     */
	public void onMessage(TopicMessage message) {
		for (TopicMessageListener listener : listeners.keySet()) {
			try {
				listener.onMessage(new TopicMessageEvent(this, message));
			}
			catch (Exception e) {
				log.error(e, "Consumer listener threw an exception: ", listener);
			}
		}
	}

	/**
	 * Reply to a server-to-client request
	 * @param message incoming request message
	 * @param reply response to send
	 */
    public void reply(TopicMessage message, Object reply) {
        ReplyMessage replyMessage = new ReplyMessage(destination, topic, message.getId(), reply);
        replyMessage.getHeaders().putAll(message.getHeaders());
        channel.send(replyMessage);
    }
    
	@Override
	public String toString() {
		return getClass().getName() + " {subscriptionId=" + subscriptionId +
			", destination=" + destination +
			", topic=" + topic +
			", selector=" + selector +
		"}";
	}
}
