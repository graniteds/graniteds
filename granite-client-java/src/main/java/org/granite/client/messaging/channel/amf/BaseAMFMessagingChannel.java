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
package org.granite.client.messaging.channel.amf;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
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
import org.granite.client.messaging.messages.Message.Type;
import org.granite.client.messaging.messages.RequestMessage;
import org.granite.client.messaging.messages.ResponseMessage;
import org.granite.client.messaging.messages.requests.DisconnectMessage;
import org.granite.client.messaging.messages.responses.AbstractResponseMessage;
import org.granite.client.messaging.messages.responses.FaultMessage;
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
	protected final List<ChannelResponseListener> responseListeners = new ArrayList<ChannelResponseListener>();
	
	protected volatile long reconnectIntervalMillis = TimeUnit.SECONDS.toMillis(30L);
	protected volatile long reconnectMaxAttempts = 60L;
	protected volatile long reconnectAttempts = 0L;

	public BaseAMFMessagingChannel(MessagingCodec<Message[]> codec, Transport transport, String id, URI uri) {
		super(transport, id, uri, 1);
		
		this.codec = codec;
	}
	
	public void setSessionId(String sessionId) {
		if ((sessionId == null && this.sessionId != null) || (sessionId != null && !sessionId.equals(this.sessionId))) {
			this.sessionId = sessionId;
			log.info("Messaging channel %s set sessionId %s", clientId, sessionId);
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
			transport.send(this, new DefaultTransportMessage<Message[]>(id, true, false, clientId, sessionId, new Message[] { connectMessage }, codec));
			
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
		String subscriptionId = consumer.getSubscriptionId();
		if (subscriptionId == null) {
			for (String sid : consumersMap.keySet()) {
				if (consumersMap.get(sid) == consumer) {
					subscriptionId = sid;
					break;
				}
			}
		}
		if (subscriptionId == null) {
			log.warn("Trying to remove unexisting consumer for destination %s", consumer.getDestination());
			return false;
		}
		return consumersMap.remove(subscriptionId) != null;
	}
	
	public void addListener(ChannelResponseListener listener) {
		responseListeners.add(listener);
	}
	
	public void removeListener(ChannelResponseListener listener) {
		responseListeners.remove(listener);
	}
	
    @Override
    public ResponseMessageFuture logout(boolean sendLogout, ResponseListener... listeners) {
		log.info("Logging out channel %s", clientId);
		
    	ResponseMessageFuture future = super.logout(sendLogout, listeners);
    	
    	// Force disconnection for streaming transports to ensure next calls are in a new session/authentication context
		disconnect();
    	
    	return future;
    }
	
	public synchronized ResponseMessageFuture disconnect(ResponseListener...listeners) {
		cancelReconnectTimerTask();
		
		for (Consumer consumer : consumersMap.values())
			consumer.onDisconnect();
		
		consumersMap.clear();
		
		connectMessageId.set(null);
		reconnectAttempts = 0L;
		
		return send(new DisconnectMessage(clientId), listeners);
	}

	@Override
	protected TransportMessage createTransportMessage(AsyncToken token) throws UnsupportedEncodingException {
		Message[] messages = convertToAmf(token.getRequest());
		return new DefaultTransportMessage<Message[]>(token.getId(), false, token.isDisconnectRequest(), clientId, sessionId, messages, codec);
	}

	@Override
	protected ResponseMessage decodeResponse(InputStream is) throws IOException {
		
		boolean reconnect = false;
		AbstractResponseMessage responseChain = null;
		AbstractResponseMessage currentResponse = null;
		
		try {
			if (is.available() > 0) {
				final Message[] messages = codec.decode(is);
				
                log.debug("Channel %s: received %d messages", clientId, messages.length);
                    
				for (Message message : messages) {
					
					if (message instanceof AcknowledgeMessage) {
						AbstractResponseMessage response = convertFromAmf((AcknowledgeMessage)messages[0]);
						
						if (response instanceof ResultMessage) {
							Type requestType = null;
							RequestMessage request = getRequest(response.getCorrelationId());
							if (request != null)
								requestType = request.getType();
							else if (response.getCorrelationId().equals(connectMessageId.get())) // Reconnect
								requestType = Type.PING;
							
							if (requestType != null) {
								ResultMessage result = (ResultMessage)response;
								switch (requestType) {
								
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
	                                if (messages[0].getHeaders().containsKey("JSESSIONID"))
	                                    setSessionId((String)messages[0].getHeader("JSESSIONID"));
	                                
	                                if (clientId != null && clientId.equals(result.getClientId()))
	                                	log.warn("Received PING result for unknown clientId %s", request != null ? request.getClientId() : "(no request)");
	                                else {
	                                    log.debug("Channel %s pinged clientId %s", id, clientId);
	                                	clientId = result.getClientId();
	                                	setPinged(true);
	            						
	            						authenticate(null);
	                                }
	                                break;
	                                
								case LOGIN:
									setAuthenticated(true);
									break;
								
								case SUBSCRIBE:
									result.setResult(messages[0].getHeader(AsyncMessage.DESTINATION_CLIENT_ID_HEADER));
									break;

								default:
									break;
								}
							}
						}
						else if (response instanceof FaultMessage) {
							Type requestType = null;
							RequestMessage request = getRequest(response.getCorrelationId());
							if (request != null)
								requestType = request.getType();
							else if (response.getCorrelationId().equals(connectMessageId.get())) // Reconnect
								requestType = Type.PING;
							
							if (requestType != null) {
								switch (requestType) {
								
								case PING:
									clientId = null;
									setPinged(false);
									
									setAuthenticated(false);
	                                break;
	                                
								case LOGIN:
									setAuthenticated(false);
									break;
								
								default:
									break;
								}
							}
						}
						
						if (responseChain == null)
							responseChain = currentResponse = response;
						else {
							currentResponse.setNext(response);
							currentResponse = response;
						}
					}
				}
				
				if (responseChain != null) {
					for (ChannelResponseListener listener : responseListeners)
						listener.onResponse(responseChain);
				}
				
				for (Message message : messages) {
					if (!(message instanceof AcknowledgeMessage)) {
						reconnect = transport.isReconnectAfterReceive();
						
						if (!(message instanceof AsyncMessage))
							throw new RuntimeException("Message should be an AsyncMessage: " + message);
						
						String subscriptionId = (String)message.getHeader(AsyncMessage.DESTINATION_CLIENT_ID_HEADER);
						Consumer consumer = consumersMap.get(subscriptionId);
						if (consumer != null)
							consumer.onMessage(convertFromAmf((AsyncMessage)message));
						else
							log.warn("Channel %s: no consumer for subscriptionId: %s", clientId, subscriptionId);
					}
				}
			}
			else
				reconnect = transport.isReconnectAfterReceive();
		}
		finally {
			if (reconnect) {
				connectMessageId.set(null);
				connect();
			}
		}
		
		return responseChain;
	}

    @Override
    protected void internalStop() {
        super.internalStop();

        cancelReconnectTimerTask();
    }

    @Override
	public void onError(TransportMessage message, Exception e) {
        if (!isStarted())
            return;

		super.onError(message, e);
		
		// Mark consumers as unsubscribed
		// Should maybe not do it once consumers auto resubscribe after disconnect
		for (Consumer consumer : consumersMap.values())
			consumer.onDisconnect();
		
		if (message != null && connectMessageId.compareAndSet(message.getId(), null)) {
			scheduleReconnectTimerTask();
		}
	}

	protected void cancelReconnectTimerTask() {
		ReconnectTimerTask task = reconnectTimerTask.getAndSet(null);
		if (task != null && task.cancel())
			reconnectAttempts = 0L;
	}
	
	protected void scheduleReconnectTimerTask() {
        log.info("Channel %s schedule reconnect", getId());
        
        setPinged(false);
        setAuthenticated(false);
        
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
