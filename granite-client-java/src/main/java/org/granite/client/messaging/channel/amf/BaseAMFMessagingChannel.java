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
package org.granite.client.messaging.channel.amf;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.granite.client.messaging.Consumer;
import org.granite.client.messaging.ResponseListener;
import org.granite.client.messaging.channel.AsyncToken;
import org.granite.client.messaging.channel.Channel;
import org.granite.client.messaging.channel.MessagingChannel;
import org.granite.client.messaging.channel.ResponseMessageFuture;
import org.granite.client.messaging.codec.MessagingCodec;
import org.granite.client.messaging.messages.RequestMessage;
import org.granite.client.messaging.messages.ResponseMessage;
import org.granite.client.messaging.messages.requests.DisconnectMessage;
import org.granite.client.messaging.messages.responses.AbstractResponseMessage;
import org.granite.client.messaging.messages.responses.ResultMessage;
import org.granite.client.messaging.transport.DefaultTransportMessage;
import org.granite.client.messaging.transport.Transport;
import org.granite.client.messaging.transport.TransportMessage;
import org.granite.logging.Logger;
import org.granite.util.UUIDUtil;

import flex.messaging.messages.AcknowledgeMessage;
import flex.messaging.messages.AsyncMessage;
import flex.messaging.messages.CommandMessage;
import flex.messaging.messages.Message;

/**
 * @author Franck WOLFF
 */
public class BaseAMFMessagingChannel extends AbstractAMFChannel implements MessagingChannel {
	
	private static final Logger log = Logger.getLogger(BaseAMFMessagingChannel.class);
	
	protected final MessagingCodec<Message[]> codec;
	
	protected String sessionId = null;
	protected final ConcurrentMap<String, Consumer> consumersMap = new ConcurrentHashMap<String, Consumer>();	
	protected final AtomicReference<String> connectMessageId = new AtomicReference<String>(null);
	protected final AtomicReference<ReconnectTimerTask> reconnectTimerTask = new AtomicReference<ReconnectTimerTask>();
	
	protected volatile long reconnectIntervalMillis = TimeUnit.SECONDS.toMillis(30L);
	protected volatile long reconnectMaxAttempts = 60L;
	protected volatile long reconnectAttempts = 0L;

	protected BaseAMFMessagingChannel(MessagingCodec<Message[]> codec, Transport transport, String id, URI uri) {
		super(transport, id, uri, 1);
		
		this.codec = codec;
	}
	
	public void setSessionId(String sessionId) {
		if ((sessionId == null && this.sessionId != null) || (sessionId != null && !sessionId.equals(this.sessionId))) {
			this.sessionId = sessionId;
			log.info("Messaging channel sessionId %s", sessionId);
		}				
	}

	protected boolean connect() {
		
		// Connecting: make sure we don't have an active reconnect timer task.
		cancelReconnectTimerTask();
		
		// No subscriptions...
		if (consumersMap.isEmpty())
			return false;
		
		// We are already waiting for a connection/answer.
		final String id = UUIDUtil.randomUUID();
		if (!connectMessageId.compareAndSet(null, id))
			return false;
		
		log.debug("Connecting channel with clientId %s", clientId);
		
		// Create and try to send the connect message.
		CommandMessage connectMessage = new CommandMessage();
		connectMessage.setOperation(CommandMessage.CONNECT_OPERATION);
		connectMessage.setMessageId(id);
		connectMessage.setTimestamp(System.currentTimeMillis());
		connectMessage.setClientId(clientId);

		try {
			transport.send(this, new DefaultTransportMessage<Message[]>(id, true, clientId, sessionId, new Message[]{connectMessage}, codec));
			
			return true;
		}
		catch (Exception e) {
			// Connect immediately failed, release the message id and schedule a reconnect.
			connectMessageId.set(null);
			scheduleReconnectTimerTask();
			
			return false;
		}
	}
	
	@Override
	public void addConsumer(Consumer consumer) {
		consumersMap.putIfAbsent(consumer.getSubscriptionId(), consumer);
		
		connect();
	}

	@Override
	public boolean removeConsumer(Consumer consumer) {
		return (consumersMap.remove(consumer.getSubscriptionId()) != null);
	}
	
	public synchronized ResponseMessageFuture disconnect(ResponseListener...listeners) {
		cancelReconnectTimerTask();
		
		connectMessageId.set(null);
		reconnectAttempts = 0L;
		
		for (Consumer consumer : consumersMap.values())
			consumer.onDisconnect();
		
		consumersMap.clear();	
		
		return send(new DisconnectMessage(clientId), listeners);
	}

	@Override
	protected TransportMessage createTransportMessage(AsyncToken token) throws UnsupportedEncodingException {
		Message[] messages = convertToAmf(token.getRequest());
		return new DefaultTransportMessage<Message[]>(token.getId(), false, clientId, sessionId, messages, codec);
	}

	@Override
	protected ResponseMessage decodeResponse(InputStream is) throws IOException {
		boolean reconnect = true;
		
		try {
			if (is.available() > 0) {
				final Message[] messages = codec.decode(is);
				
				if (messages.length > 0 && messages[0] instanceof AcknowledgeMessage) {
					
					reconnect = false;

					final AbstractResponseMessage response = convertFromAmf((AcknowledgeMessage)messages[0]);
		
					if (response instanceof ResultMessage) {
						RequestMessage request = getRequest(response.getCorrelationId());
						if (request != null) {
							ResultMessage result = (ResultMessage)response;
							switch (request.getType()) {

							case PING:
								if (messages[0].getBody() instanceof Map) {
									Map<?, ?> advices = (Map<?, ?>)messages[0].getBody();
									Object reconnectIntervalMillis = advices.get(Channel.RECONNECT_INTERVAL_MS_KEY);
									if (reconnectIntervalMillis instanceof Number)
										this.reconnectIntervalMillis = ((Number)reconnectIntervalMillis).longValue();
									Object reconnectMaxAttempts = advices.get(Channel.RECONNECT_MAX_ATTEMPTS_KEY);
									if (reconnectMaxAttempts instanceof Number)
										this.reconnectMaxAttempts = ((Number)reconnectMaxAttempts).longValue();
								}
								break;
							
							case SUBSCRIBE:
								result.setResult(messages[0].getHeader(AsyncMessage.DESTINATION_CLIENT_ID_HEADER));
								break;

							default:
								break;
							}
						}
					}
					
					AbstractResponseMessage current = response;
					for (int i = 1; i < messages.length; i++) {
						if (!(messages[i] instanceof AcknowledgeMessage))
							throw new RuntimeException("Message should be an AcknowledgeMessage: " + messages[i]);
						
						AbstractResponseMessage next = convertFromAmf((AcknowledgeMessage)messages[i]);
						current.setNext(next);
						current = next;
					}
					
					return response;
				}
				
				for (Message message : messages) {
					if (!(message instanceof AsyncMessage))
						throw new RuntimeException("Message should be an AsyncMessage: " + message);
					
					String subscriptionId = (String)message.getHeader(AsyncMessage.DESTINATION_CLIENT_ID_HEADER);
					Consumer consumer = consumersMap.get(subscriptionId);
					if (consumer != null)
						consumer.onMessage(convertFromAmf((AsyncMessage)message));
					else
						log.warn("No consumer for subscriptionId: %s", subscriptionId);
				}
			}
		}
		finally {
			if (reconnect) {
				connectMessageId.set(null);
				connect();
			}
		}
		
		return null;
	}

	@Override
	public void onError(TransportMessage message, Exception e) {
		super.onError(message, e);
		
		if (message != null && connectMessageId.compareAndSet(message.getId(), null))
			scheduleReconnectTimerTask();
	}

	protected void cancelReconnectTimerTask() {
		ReconnectTimerTask task = reconnectTimerTask.getAndSet(null);
		if (task != null && task.cancel())
			reconnectAttempts = 0L;
	}
	
	protected void scheduleReconnectTimerTask() {
		ReconnectTimerTask task = new ReconnectTimerTask();
		
		ReconnectTimerTask previousTask = reconnectTimerTask.getAndSet(task);
		if (previousTask != null)
			previousTask.cancel();
		
		if (reconnectAttempts < reconnectMaxAttempts) {
			reconnectAttempts++;
			schedule(task, reconnectIntervalMillis);
		}
	}
	
	class ReconnectTimerTask extends TimerTask {

		@Override
		public void run() {
			connect();
		}
	}

}
