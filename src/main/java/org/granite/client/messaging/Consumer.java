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

import java.util.concurrent.ConcurrentHashMap;

import org.granite.client.messaging.channel.MessagingChannel;
import org.granite.client.messaging.channel.ResponseMessageFuture;
import org.granite.client.messaging.events.IssueEvent;
import org.granite.client.messaging.events.ResultEvent;
import org.granite.client.messaging.events.TopicMessageEvent;
import org.granite.client.messaging.messages.push.TopicMessage;
import org.granite.client.messaging.messages.requests.SubscribeMessage;
import org.granite.client.messaging.messages.requests.UnsubscribeMessage;
import org.granite.logging.Logger;

/**
 * @author Franck WOLFF
 */
public class Consumer extends AbstractTopicAgent {
	
	private static final Logger log = Logger.getLogger(Consumer.class);

	private final ConcurrentHashMap<TopicMessageListener, Boolean> listeners = new ConcurrentHashMap<TopicMessageListener, Boolean>();
	
	private String subscriptionId = null;
	private String selector = null;
	
	public Consumer(MessagingChannel channel, String destination, String topic) {
		super(channel, destination, topic);
	}

	public String getSelector() {
		return selector;
	}

	public void setSelector(String selector) {
		this.selector = selector;
	}
	
	public boolean isSubscribed() {
		return subscriptionId != null;
	}

	public String getSubscriptionId() {
		return subscriptionId;
	}

	public ResponseMessageFuture subscribe(ResponseListener...listeners) {
		SubscribeMessage subscribeMessage = new SubscribeMessage(destination, topic, selector);
		subscribeMessage.getHeaders().putAll(defaultHeaders);
		
		final Consumer consumer = this;
		ResponseListener listener = new ResultIssuesResponseListener() {
			
			@Override
			public void onResult(ResultEvent event) {
				subscriptionId = (String)event.getResult();
				channel.addConsumer(consumer);
			}
			
			@Override
			public void onIssue(IssueEvent event) {
				log.error("Subscription failed %s: %s", consumer, event);
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
		
		return channel.send(subscribeMessage, listeners);
	}

	public ResponseMessageFuture unsubscribe(ResponseListener...listeners) {
		UnsubscribeMessage unsubscribeMessage = new UnsubscribeMessage(destination, topic, subscriptionId);
		unsubscribeMessage.getHeaders().putAll(defaultHeaders);
		
		final Consumer consumer = this;
		ResponseListener listener = new ResultIssuesResponseListener() {
			
			@Override
			public void onResult(ResultEvent event) {
				channel.removeConsumer(consumer);
				subscriptionId = null;
			}
			
			@Override
			public void onIssue(IssueEvent event) {
				log.error("Unsubscription failed %s: %s", consumer, event);
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

		return channel.send(unsubscribeMessage, listeners);
	}

	public void addMessageListener(TopicMessageListener listener) {
		listeners.putIfAbsent(listener, Boolean.TRUE);
	}
	
	public boolean removeMessageListener(TopicMessageListener listener) {
		return listeners.remove(listener) != null;
	}
	
	public void onDisconnect() {
		subscriptionId = null;
	}

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

	@Override
	public String toString() {
		return getClass().getName() + " {subscriptionId=" + subscriptionId +
			", destination=" + destination +
			", topic=" + topic +
			", selector=" + selector +
		"}";
	}
}
