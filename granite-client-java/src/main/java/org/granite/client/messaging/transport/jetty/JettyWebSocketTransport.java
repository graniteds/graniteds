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
package org.granite.client.messaging.transport.jetty;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.LinkedList;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.websocket.WebSocket.Connection;
import org.eclipse.jetty.websocket.WebSocket.OnBinaryMessage;
import org.eclipse.jetty.websocket.WebSocketClient;
import org.eclipse.jetty.websocket.WebSocketClientFactory;
import org.granite.client.messaging.channel.Channel;
import org.granite.client.messaging.transport.AbstractTransport;
import org.granite.client.messaging.transport.TransportException;
import org.granite.client.messaging.transport.TransportFuture;
import org.granite.client.messaging.transport.TransportMessage;
import org.granite.client.messaging.transport.WebSocketTransport;
import org.granite.logging.Logger;
import org.granite.util.PublicByteArrayOutputStream;


/**
 * @author William DRAI
 */
public class JettyWebSocketTransport extends AbstractTransport<Object> implements WebSocketTransport {
	
	private static final Logger log = Logger.getLogger(JettyWebSocketTransport.class);

	private final static int CLOSE_NORMAL = 1000;
	private final static int CLOSE_SHUTDOWN = 1001;
//	private final static int CLOSE_PROTOCOL = 1002;
	
	private WebSocketClientFactory webSocketClientFactory = null;

	private Future<Connection> connectionFuture = null;
	private boolean connected = false;
	
	private int maxIdleTime = 3000000;
	private int reconnectMaxAttempts = 5;
	private int reconnectIntervalMillis = 60000;
	
	public void setMaxIdleTime(int maxIdleTime) {
		this.maxIdleTime = maxIdleTime;
	}
	
	@Override
	public synchronized boolean start() {
		if (isStarted())
			return true;

		log.info("Starting Jetty WebSocketClient transport...");
		
		try {
			webSocketClientFactory = new WebSocketClientFactory();
			webSocketClientFactory.setBufferSize(4096);
			webSocketClientFactory.start();
			
			final long timeout = System.currentTimeMillis() + 10000L; // 10sec.
			while (!webSocketClientFactory.isStarted()) {
				if (System.currentTimeMillis() > timeout)
					throw new TimeoutException("Jetty WebSocketFactory start process too long");
				Thread.sleep(100);
			}
			
			log.info("Jetty WebSocketClient transport started.");
			return true;
		}
		catch (Exception e) {
			webSocketClientFactory = null;
			getStatusHandler().handleException(new TransportException("Could not start Jetty WebSocketFactory", e));
			
			log.error(e, "Jetty WebSocketClient transport failed to start.");
			return false;
		}
	}
	
	public synchronized boolean isStarted() {
		return webSocketClientFactory != null && webSocketClientFactory.isStarted();
	}
	
	@Override
	public TransportFuture send(final Channel channel, final TransportMessage message) {

		synchronized (channel) {

			TransportData transportData = channel.getTransportData();
			if (transportData == null) {
				transportData = new TransportData();
				channel.setTransportData(transportData);
			}
			
			if (message != null) {
				if (message.isConnect())
					connectMessage = message;
				else
					transportData.pendingMessages.addLast(message);
			}
			
			if (transportData.connection == null) {
				connect(channel, message);
				return null;
			}

			while (!transportData.pendingMessages.isEmpty()) {
				TransportMessage pendingMessage = transportData.pendingMessages.removeFirst();
				try {
					PublicByteArrayOutputStream os = new PublicByteArrayOutputStream(256);
					pendingMessage.encode(os);
					byte[] data = os.getBytes();
					transportData.connection.sendMessage(data, 0, os.size());
				}
				catch (IOException e) {
					transportData.pendingMessages.addFirst(pendingMessage);
					// report error...
					break;
				}
			}
		}
		
		return null;
	}
	
	@Override
	public void poll(final Channel channel, final TransportMessage message) {
		send(channel, message);
	}
	
	private int reconnectAttempts = 0;
	private TransportMessage connectMessage = null;

	public Future<Connection> connect(final Channel channel, final TransportMessage transportMessage) {
		if (connectionFuture != null)
			return connectionFuture;
		
		connected = true;
		
		URI uri = channel.getUri();
		
		try {		
			WebSocketClient webSocketClient = webSocketClientFactory.newWebSocketClient();
			webSocketClient.setMaxIdleTime(maxIdleTime);
			webSocketClient.setMaxTextMessageSize(1024);
			webSocketClient.setProtocol("org.granite.gravity");
			
			if (transportMessage.getSessionId() != null)
				webSocketClient.getCookies().put("JSESSIONID", transportMessage.getSessionId());
			
			String u = uri.toString();
			u += "?connectId=" + transportMessage.getId() + "&GDSClientType=" + transportMessage.getClientType();
			if (transportMessage.getClientId() != null)
				u += "&GDSClientId=" + transportMessage.getClientId();
			else if (channel.getClientId() != null)
				u += "&GDSClientId=" + channel.getClientId();
			
			connectionFuture = webSocketClient.open(new URI(u), new OnBinaryMessage() {
				
				@Override
				public void onOpen(Connection connection) {
					synchronized (channel) {
						connectionFuture = null;
						reconnectAttempts = 0;
						((TransportData)channel.getTransportData()).connection = connection;
						send(channel, null);
					}
				}
				
				@Override
				public void onMessage(byte[] data, int offset, int length) {
					channel.onMessage(new ByteArrayInputStream(data, offset, length));
				}
	
				@Override
				public void onClose(int closeCode, String message) {
					boolean waitBeforeReconnect = !(closeCode == CLOSE_NORMAL && message.startsWith("Idle"));
					
					synchronized (channel) {
						// Mark the connection as close, the channel should reopen a connection for the next message
						((TransportData)channel.getTransportData()).connection = null;
						connectionFuture = null;
						
						if (!isStarted())
							connected = false;
						
						if (closeCode == CLOSE_SHUTDOWN) {
							connected = false;
							return;
						}
						
						if (channel.getClientId() == null) {
							getStatusHandler().handleException(new TransportException("Transport could not connect code: " + closeCode + " " + message));
							return;
						}
						
						if (connected) {
							if (reconnectAttempts >= reconnectMaxAttempts) {
								connected = false;
								if (isStarted())
									stop();
								
								channel.onError(transportMessage, new RuntimeException(message + " (code=" + closeCode + ")"));
								getStatusHandler().handleException(new TransportException("Transport disconnected"));
								return;
							}
							
							if (waitBeforeReconnect) {
								try {
									waitBeforeReconnect = false;
									Thread.sleep(reconnectIntervalMillis);
								}
								catch (InterruptedException e) {
								}
							}
							
							reconnectAttempts++;
							
							// If the channel should be connected, try to reconnect
							log.info("Connection lost (code %d, msg %s), reconnect channel (retry #%d)", closeCode, message, reconnectAttempts);
							connect(channel, connectMessage);
						}
					}
				}
			});
			
			return connectionFuture;
		}
		catch (Exception e) {
			getStatusHandler().handleException(new TransportException("Could not connect to uri " + channel.getUri(), e));
			
			return null;
		}
	}
	
	private static class TransportData {
		
		private final LinkedList<TransportMessage> pendingMessages = new LinkedList<TransportMessage>();
		private Connection connection = null;
	}

	@Override
	public synchronized void stop() {
		if (webSocketClientFactory == null)
			return;
		
		log.info("Stopping Jetty WebSocketClient transport...");
		
		super.stop();
		
		try {
			webSocketClientFactory.stop();
		}
		catch (Exception e) {
			getStatusHandler().handleException(new TransportException("Could not stop Jetty WebSocketFactory", e));

			log.error(e, "Jetty WebSocketClient failed to stop properly.");
		}
		finally {
			webSocketClientFactory = null;
		}
		
		log.info("Jetty WebSocketClient transport stopped.");
	}
}
