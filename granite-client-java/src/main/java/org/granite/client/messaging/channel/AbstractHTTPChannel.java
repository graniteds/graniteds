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
package org.granite.client.messaging.channel;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.granite.client.messaging.AllInOneResponseListener;
import org.granite.client.messaging.ResponseListener;
import org.granite.client.messaging.ResponseListenerDispatcher;
import org.granite.client.messaging.events.Event;
import org.granite.client.messaging.events.Event.Type;
import org.granite.client.messaging.messages.MessageChain;
import org.granite.client.messaging.messages.RequestMessage;
import org.granite.client.messaging.messages.ResponseMessage;
import org.granite.client.messaging.messages.requests.DisconnectMessage;
import org.granite.client.messaging.messages.requests.LoginMessage;
import org.granite.client.messaging.messages.requests.LogoutMessage;
import org.granite.client.messaging.messages.requests.PingMessage;
import org.granite.client.messaging.messages.responses.FaultMessage;
import org.granite.client.messaging.messages.responses.ResultMessage;
import org.granite.client.messaging.transport.Transport;
import org.granite.client.messaging.transport.TransportFuture;
import org.granite.client.messaging.transport.TransportMessage;
import org.granite.client.messaging.transport.TransportStopListener;
import org.granite.logging.Logger;

/**
 * @author Franck WOLFF
 */
public abstract class AbstractHTTPChannel extends AbstractChannel<Transport> implements TransportStopListener, Runnable {
	
	private static final Logger log = Logger.getLogger(AbstractHTTPChannel.class);
	
	private final BlockingQueue<AsyncToken> tokensQueue = new LinkedBlockingQueue<AsyncToken>();;
	private final ConcurrentMap<String, AsyncToken> tokensMap = new ConcurrentHashMap<String, AsyncToken>();
	private final AsyncToken stopToken = new AsyncToken(new DisconnectMessage());
	private final AtomicBoolean stopped = new AtomicBoolean(true);
	private AsyncToken disconnectToken = null;
	
	private Thread senderThread = null;
	private Semaphore connections;
	private Timer timer = null;
	
	private List<ChannelStatusListener> statusListeners = new ArrayList<ChannelStatusListener>();
	private volatile boolean pinged = false;
	private volatile boolean authenticated = false;
	private volatile boolean authenticating = false;
	protected volatile int maxConcurrentRequests;
	protected volatile long defaultTimeToLive = DEFAULT_TIME_TO_LIVE; // 1 mn.
	
	public AbstractHTTPChannel(Transport transport, String id, URI uri, int maxConcurrentRequests) {
		super(transport, id, uri);
		
		if (maxConcurrentRequests < 1)
			throw new IllegalArgumentException("maxConcurrentRequests must be greater or equal to 1");
		
		this.maxConcurrentRequests = maxConcurrentRequests;
	}
	
	protected abstract TransportMessage createTransportMessage(AsyncToken token) throws UnsupportedEncodingException;
	
	protected abstract ResponseMessage decodeResponse(InputStream is) throws IOException;

	protected boolean schedule(TimerTask timerTask, long delay) {
		if (timer != null) {
			timer.schedule(timerTask, delay);
			return true;
		}
		return false;
	}
	
	public long getDefaultTimeToLive() {
		return defaultTimeToLive;
	}

	public void setDefaultTimeToLive(long defaultTimeToLive) {
		this.defaultTimeToLive = defaultTimeToLive;
	}

	public boolean isAuthenticated() {
		return authenticated;
	}

	public int getMaxConcurrentRequests() {
		return maxConcurrentRequests;
	}

	@Override
	public void onStop(Transport transport) {
		stop();
	}

	@Override
	public synchronized boolean start() {
		if (senderThread != null)
			return true;
		
		tokensQueue.clear();
		tokensMap.clear();
		disconnectToken = null;
		stopped.set(false);
		
		log.info("Starting channel %s...", id);
		senderThread = new Thread(this, id + "_sender");
		try {
			timer = new Timer(id + "_timer", true);
			connections = new Semaphore(maxConcurrentRequests);
			senderThread.start();
			
			transport.addStopListener(this);
			
			log.info("Channel %s started.", id);
		}
		catch (Exception e) {
			if (timer != null) {
				timer.cancel();
				timer = null;
			}
			connections = null;
			senderThread = null;
			log.error(e, "Channel %s failed to start.", id);
			return false;
		}
		return true;
	}

	@Override
	public synchronized boolean isStarted() {
		return senderThread != null;
	}

	@Override
	public synchronized boolean stop() {
		if (senderThread == null)
			return false;
	
		log.info("Stopping channel %s...", id);
		
		if (timer != null) {
			try {
				timer.cancel();
			}
			catch (Exception e) {
				log.error(e, "Channel %s timer failed to stop.", id);
			}
			finally {
				timer = null;
			}
		}
		
        internalStop();
		
        log.debug("Interrupting thread %s", id);
		
		connections = null;
		tokensMap.clear();
		tokensQueue.clear();
		disconnectToken = null;
		
		stopped.set(true);
		tokensQueue.add(stopToken);
        
		Thread thread = this.senderThread;
		senderThread = null;
		thread.interrupt();
		
		pinged = false;
		clientId = null;
		setAuthenticated(false, null);
		
		return true;
	}

    protected void internalStop() {
    }

    
    public void reauthenticate() {
		if (authenticating || authenticated)
			return;
		
		Credentials credentials = this.credentials;
		if (credentials == null)
			return;
		
		log.debug("Channel %id client %id reauthenticating", getId(), clientId);
		LoginMessage loginMessage = new LoginMessage(clientId, credentials);
		try {
			ResponseMessage response = sendSimpleBlockingToken(loginMessage);
			if (response instanceof ResultMessage)
				setAuthenticated(true, response);
		}
		catch (Exception e) {
			log.warn(e, "Could not reauthenticate channel %s", clientId);
		}
    }
    
    protected LoginMessage authenticate(AsyncToken dependentToken) {
		if (authenticating || authenticated)
			return null;
		
		Credentials credentials = this.credentials;
		if (credentials == null)
			return null;
		
		LoginMessage loginMessage = new LoginMessage(clientId, credentials);
		if (dependentToken != null) {
			log.debug("Channel %s blocking authentication %s clientId %s", id, loginMessage.getId(), clientId);
			ResultMessage result = sendBlockingToken(loginMessage, dependentToken);
			if (result == null)
				return loginMessage;
			setAuthenticated(true, result);
		}
		else {
			log.debug("Channel %s non blocking authentication %s clientId %s", id, loginMessage.getId(), clientId);
			send(loginMessage);
			authenticating = true;
		}
		return loginMessage;
    }
    
	@Override
	public void run() {

		while (!Thread.interrupted()) {
			try {
				if (stopped.get())
					break;
				
				AsyncToken token = tokensQueue.take();
				if (token == stopToken)
					break;
				
				if (token.isDone())
					continue;

				if (token.isDisconnectRequest()) {
					sendToken(token);
					continue;
				}
				
				if (!pinged) {
                    PingMessage pingMessage = new PingMessage(clientId);
                    log.debug("Channel %s send ping %s with clientId %s", id, pingMessage.getId(), clientId);
					ResultMessage result = sendBlockingToken(pingMessage, token);
					if (result == null)
						continue;
					clientId = result.getClientId();
                    log.debug("Channel %s pinged clientId %s", id, clientId);
					pinged = true;
				}
				
				authenticate(token);
				
				if (!(token.getRequest() instanceof PingMessage))
					sendToken(token);
			}
			catch (InterruptedException e) {
				log.info("Channel %s stopped.", id);
				break;
			}
			catch (Exception e) {
				log.error(e, "Channel %s got an unexpected exception.", id);
			}
		}
	}
	
	private ResultMessage sendBlockingToken(RequestMessage request, AsyncToken dependentToken) {
		
		// Make this blocking request share the timeout/timeToLive values of the dependent token.
		request.setTimestamp(dependentToken.getRequest().getTimestamp());
		request.setTimeToLive(dependentToken.getRequest().getTimeToLive());
		
		// Create the blocking token and schedule it with the dependent token timeout.
		AsyncToken blockingToken = new AsyncToken(request);
		try {
			timer.schedule(blockingToken, blockingToken.getRequest().getRemainingTimeToLive());
		}
		catch (IllegalArgumentException e) {
			dependentToken.dispatchTimeout(System.currentTimeMillis());
			return null;
		}
		catch (Exception e) {
			dependentToken.dispatchFailure(e);
			return null;
		}
		
		// Try to send the blocking token (can block if the connections semaphore can't be acquired
		// immediately).
		try {
			if (!sendToken(blockingToken))
				return null;
		}
		catch (Exception e) {
			dependentToken.dispatchFailure(e);
			return null;
		}
		
		// Block until we get a server response (result or fault), a cancellation (unlikely), a timeout
		// or any other execution exception.
		try {
			ResponseMessage response = blockingToken.get();
			
			// Request was successful, return a non-null result. 
			if (response instanceof ResultMessage)
				return (ResultMessage)response;
			
			if (response instanceof FaultMessage) {
				FaultMessage faultMessage = (FaultMessage)response.copy(dependentToken.getRequest().getId());
				if (dependentToken.getRequest() instanceof MessageChain) {
					ResponseMessage nextResponse = faultMessage;
					for (MessageChain<?> nextRequest = ((MessageChain<?>)dependentToken.getRequest()).getNext(); nextRequest != null; nextRequest = nextRequest.getNext()) {
						nextResponse.setNext(response.copy(nextRequest.getId()));
						nextResponse = nextResponse.getNext();
					}
				}
				dependentToken.dispatchFault(faultMessage);
			}
			else
				throw new RuntimeException("Unknown response message type: " + response);
			
		}
		catch (InterruptedException e) {
			dependentToken.dispatchFailure(e);
		}
		catch (TimeoutException e) {
			dependentToken.dispatchTimeout(System.currentTimeMillis());
		}
		catch (ExecutionException e) {
			if (e.getCause() instanceof Exception)
				dependentToken.dispatchFailure((Exception)e.getCause());
			else
				dependentToken.dispatchFailure(e);
		}
		catch (Exception e) {
			dependentToken.dispatchFailure(e);
		}
		
		return null;
	}
	
	private ResponseMessage sendSimpleBlockingToken(RequestMessage request) throws Exception {
		
		request.setTimestamp(System.currentTimeMillis());
		if (request.getTimeToLive() <= 0L)
			request.setTimeToLive(defaultTimeToLive);
		
		// Create the blocking token and schedule it with the dependent token timeout.
		AsyncToken blockingToken = new AsyncToken(request);
		try {
			timer.schedule(blockingToken, blockingToken.getRequest().getRemainingTimeToLive());
		}
		catch (IllegalArgumentException e) {
			return null;
		}
		catch (Exception e) {
			return null;
		}
		
		// Try to send the blocking token (can block if the connections semaphore can't be acquired
		// immediately).
		try {
			if (!sendToken(blockingToken))
				return null;
		}
		catch (Exception e) {
			throw e;
		}
		
		// Block until we get a server response (result or fault), a cancellation (unlikely), a timeout
		// or any other execution exception.
		try {
			return blockingToken.get();			
		}
		catch (Exception e) {
			throw e;
		}
	}
	
	private boolean sendToken(final AsyncToken token) {

		boolean releaseConnections = false;
		try {
		    // Block until a connection is available.
			if (!connections.tryAcquire(token.getRequest().getRemainingTimeToLive(), TimeUnit.MILLISECONDS)) {
				token.dispatchTimeout(System.currentTimeMillis());
				return false;
			}

			// Semaphore was successfully acquired, we must release it in the finally block unless we succeed in
			// sending the data (see below).
			releaseConnections = true;

		    // Check if the token has already received an event (likely a timeout or a cancellation).
			if (token.isDone())
				return false;

			// Make sure we have set a clientId (can be null for ping message).
			token.getRequest().setClientId(clientId);
			
			if (token.getRequest() instanceof DisconnectMessage) {
				// Store disconnect token
				disconnectToken = token;
			}
			else {
			    // Add the token to active tokens map.
			    if (tokensMap.putIfAbsent(token.getId(), token) != null)
					throw new RuntimeException("MessageId isn't unique: " + token.getId());
			}
			
	    	// Actually send the message content.
		    TransportFuture transportFuture = transport.send(this, createTransportMessage(token));
		    
		    // Create and try to set a channel listener: if no event has been dispatched for this token (tokenEvent == null),
		    // the listener will be called on the next event. Otherwise, we just call the listener immediately.
		    ResponseListener channelListener = new ChannelResponseListener(token.getId(), tokensMap, transportFuture, connections);
		    Event tokenEvent = token.setChannelListener(channelListener);
		    if (tokenEvent != null)
				ResponseListenerDispatcher.dispatch(channelListener, tokenEvent);
		    
		    // Message was sent and we were able to handle everything ourself.
		    releaseConnections = false;
			
		    return true;
		}
		catch (Exception e) {
			tokensMap.remove(token.getId());
			token.dispatchFailure(e);			
			if (timer != null)
				timer.purge();	// Must purge to cleanup timer references to AsyncToken
			return false;
		}
		finally {
			if (releaseConnections && connections != null)
				connections.release();
		}
	}
	
	protected RequestMessage getRequest(String id) {
		AsyncToken token = tokensMap.get(id);
		return (token != null ? token.getRequest() : null);
	}
	
	
	@Override
	public ResponseMessageFuture send(RequestMessage request, ResponseListener... listeners) {
		if (request == null)
			throw new NullPointerException("request cannot be null");
		
		if (!start())
			throw new RuntimeException("Channel not started");
		
		AsyncToken token = new AsyncToken(request, listeners);

		request.setTimestamp(System.currentTimeMillis());
		if (request.getTimeToLive() <= 0L)
			request.setTimeToLive(defaultTimeToLive);
		
		try {
			timer.schedule(token, request.getRemainingTimeToLive());
			tokensQueue.add(token);
		}
		catch (Exception e) {
			log.error(e, "Could not add token to queue: %s", token);
			token.dispatchFailure(e);
			return new ImmediateFailureResponseMessageFuture(e);
		}
		
		return token;
	}
	
    @Override
    public ResponseMessageFuture logout(ResponseListener... listeners) {
        return logout(true, listeners);
	}

    @Override
    public ResponseMessageFuture logout(boolean sendLogout, ResponseListener... listeners) {
		log.info("Logging out channel %s", clientId);
		
        credentials = null;
        setAuthenticated(false, null);
        if (sendLogout)
            return send(new LogoutMessage(), listeners);
        return null;
    }

	@Override
	public void onMessage(TransportMessage message, InputStream is) {
		try {
			ResponseMessage response = decodeResponse(is);			
			if (response == null)
				return;
			
			AsyncToken token = tokensMap.remove(response.getCorrelationId());
			if (token == null) {
				log.warn("Unknown correlation id: %s", response.getCorrelationId());
				return;
			}
			
			switch (response.getType()) {
				case RESULT:
					token.dispatchResult((ResultMessage)response);
					break;
				case FAULT:
				    FaultMessage faultMessage = (FaultMessage)response;
				    if (isAuthenticated() && (faultMessage.getCode() == FaultMessage.Code.NOT_LOGGED_IN || faultMessage.getCode() == FaultMessage.Code.SESSION_EXPIRED)) {
				    	log.info("Channel %s possible session expiration detected, mark as not logged in", getId());
				        setAuthenticated(false, faultMessage);
				        // TODO: Why clear credentials here ???
				        // credentials = null;
				    }
				    
					token.dispatchFault((FaultMessage)response);
					break;
				default:
					token.dispatchFailure(new RuntimeException("Unknown message type: " + response));
					break;
			}
		}
		catch (Exception e) {
			log.error(e, "Could not deserialize or dispatch incoming messages");
			
			AsyncToken token = message != null ? tokensMap.remove(message.getId()) : null;
			if (token != null)
				token.dispatchFailure(e);
		}
		finally {
			if (timer != null)
				timer.purge();	// Must purge to cleanup timer references to AsyncToken
		}
	}
	
	@Override
	public void onDisconnect() {
		log.info("Disconnecting channel %s", clientId);
		
		tokensMap.clear();
		tokensQueue.clear();
		
		if (timer != null)
			timer.purge();	// Must purge to cleanup timer references to AsyncToken
		
		if (disconnectToken != null) {
			// Handle "hard" disconnect
			ResultMessage resultMessage = new ResultMessage(clientId, disconnectToken.getRequest().getId(), true);
			disconnectToken.dispatchResult(resultMessage);
			disconnectToken = null;
		}
		
		clientId = null;
		pinged = false;
		authenticating = false;
		authenticated = false;
	}
	
	@Override
	public void onError(TransportMessage message, Exception e) {
		if (message == null)
			return;
		
		AsyncToken token = tokensMap.remove(message.getId());
		if (token == null)
			return;
		
		token.dispatchFailure(e);
		
		if (timer != null)
			timer.purge();	// Must purge to cleanup timer references to AsyncToken
	}
	
	@Override
	public void onCancelled(TransportMessage message) {
		AsyncToken token = tokensMap.remove(message.getId());
		if (token == null)
			return;
		
		token.dispatchCancelled();
		
		if (timer != null)
			timer.purge();	// Must purge to cleanup timer references to AsyncToken
	}
	
	private static class ChannelResponseListener extends AllInOneResponseListener {
		
		private final String tokenId;
		private final ConcurrentMap<String, AsyncToken> tokensMap;
		private final TransportFuture transportFuture;
		private final Semaphore connections;
		
		public ChannelResponseListener(
			String tokenId,
			ConcurrentMap<String, AsyncToken> tokensMap,
			TransportFuture transportFuture,
			Semaphore connections) {

			this.tokenId = tokenId;
			this.tokensMap = tokensMap;
			this.transportFuture = transportFuture;
			this.connections = connections;
		}

		@Override
		public void onEvent(Event event) {
			try {
				tokensMap.remove(tokenId);
				if (event.getType() == Type.TIMEOUT || event.getType() == Type.CANCELLED) {
					if (transportFuture != null) {
						try {
							transportFuture.cancel();
						}
						catch (UnsupportedOperationException e) {
							// In case transport does not support cancel
						}
					}
				}
			}
			finally {
				if (connections != null)
					connections.release();
			}
		}
	}
	
	
	public void addListener(ChannelStatusListener listener) {
		statusListeners.add(listener);
	}
	
	public void removeListener(ChannelStatusListener listener) {
		statusListeners.remove(listener);
	}
	
	protected void setPinged(boolean pinged) {
		if (this.pinged == pinged)
			return;
		this.pinged = pinged;
		for (ChannelStatusListener listener : statusListeners)
			listener.pingedChanged(this, pinged);
	}
	
	protected void setAuthenticated(boolean authenticated, ResponseMessage response) {
		if (!this.authenticating && this.authenticated == authenticated)
			return;
		log.debug("Channel %s authentication changed clientId %s (%s)", id,  clientId, String.valueOf(authenticated));
		this.authenticating = false;
		this.authenticated = authenticated;
		for (ChannelStatusListener listener : statusListeners)
			listener.authenticatedChanged(this, authenticated, response);
	}
	
	protected void dispatchFault(FaultMessage faultMessage) {
		for (ChannelStatusListener listener : statusListeners)
			listener.fault(this, faultMessage);
	}
	
	public void bindStatus(ChannelStatusNotifier notifier) {
		notifier.addListener(statusListener);
	}
	
	public void unbindStatus(ChannelStatusNotifier notifier) {
		notifier.removeListener(statusListener);
	}	
	
	private ChannelStatusListener statusListener = new ChannelStatusListener() {
		
		@Override
		public void fault(Channel channel, FaultMessage faultMessage) {
		}

		@Override
		public void pingedChanged(Channel channel, boolean pinged) {
			if (channel == AbstractHTTPChannel.this)
				return;
			AbstractHTTPChannel.this.pinged = pinged;
		}
		
		@Override
		public void authenticatedChanged(Channel channel, boolean authenticated, ResponseMessage response) {
			if (channel == AbstractHTTPChannel.this)
				return;
			AbstractHTTPChannel.this.authenticating = false;
			AbstractHTTPChannel.this.authenticated = authenticated;
		}		
	};
}
