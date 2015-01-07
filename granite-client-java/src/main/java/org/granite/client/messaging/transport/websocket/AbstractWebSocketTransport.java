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
package org.granite.client.messaging.transport.websocket;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.util.LinkedList;

import org.granite.client.messaging.channel.Channel;
import org.granite.client.messaging.transport.AbstractTransport;
import org.granite.client.messaging.transport.TransportException;
import org.granite.client.messaging.transport.TransportFuture;
import org.granite.client.messaging.transport.TransportMessage;
import org.granite.logging.Logger;
import org.granite.util.PublicByteArrayOutputStream;


/**
 * @author William DRAI
 */
public abstract class AbstractWebSocketTransport<S> extends AbstractTransport<Object> {
	
	private static final Logger log = Logger.getLogger(AbstractWebSocketTransport.class);

	private final static int CLOSE_NORMAL = 1000;
	private final static int CLOSE_SHUTDOWN = 1001;
//	private final static int CLOSE_PROTOCOL = 1002;
	
	private boolean connected = false;
	private boolean disconnecting = false;
	
	private int maxIdleTime = 3000000;
    @SuppressWarnings("unused")
	private int pingDelay = 30000;
	private int reconnectMaxAttempts = 5;
    @SuppressWarnings("unused")
	private int reconnectIntervalMillis = 60000;
    
    private int maxMessageSize = 16364;
	
	public void setMaxIdleTime(int maxIdleTime) {
		this.maxIdleTime = maxIdleTime;
	}

    public int getMaxIdleTime() {
        return maxIdleTime;
    }

    public void setPingDelay(int pingDelay) {
        this.pingDelay = pingDelay;
    }

    public void setReconnectIntervalMillis(int reconnectIntervalMillis) {
        this.reconnectIntervalMillis = reconnectIntervalMillis;
    }

    public boolean isReconnectAfterReceive() {
        return false;
    }

    public boolean isDisconnectAfterAuthenticationFailure() {
        return true;
    }
    
	public boolean isAuthenticationAfterReconnectWithRemoting() {
		return true;
	}
    
    public void setMaxMessageSize(int maxMessageSize) {
    	this.maxMessageSize = maxMessageSize;
    }
    public int getMaxMessageSize() {
    	return maxMessageSize;
    }

	@Override
	public TransportFuture send(final Channel channel, final TransportMessage message) {
        TransportData<S> transportData = null;
        boolean pending = false;

        synchronized (channel) {
            transportData = channel.getTransportData();
            if (transportData == null) {
                transportData = newTransportData();
                channel.setTransportData(transportData);
            }

            if (message != null) {
                if (message.isConnect())
                    connectMessage = message;
                else {
                	if (message.isDisconnect())
                		disconnecting = true;
                    pending = true;
                }
            }
        }
        
        if (!transportData.isConnected() && !disconnecting) {
            connected = true;
            connect(channel, message);
            return null;
        }
        else if (pending) // Ignore ping message
            transportData.pendingMessages.addLast(message);
        
        synchronized (channel) {
            while (!transportData.pendingMessages.isEmpty()) {
                TransportMessage pendingMessage = transportData.pendingMessages.removeFirst();
                try {
                    PublicByteArrayOutputStream os = new PublicByteArrayOutputStream(256);
                    pendingMessage.encode(os);
                    transportData.sendBytes(os.getBytes());
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

    protected abstract TransportData<S> newTransportData();

	private int reconnectAttempts = 0;
	private TransportMessage connectMessage = null;

	public abstract void connect(final Channel channel, final TransportMessage transportMessage);

	public static abstract class TransportData<S> {
		
		private final LinkedList<TransportMessage> pendingMessages = new LinkedList<TransportMessage>();

        public abstract void connect(S connection);

        public abstract boolean isConnected();

        public abstract void disconnect();

        public abstract void sendBytes(byte[] data) throws IOException;
	}

    private boolean stopping = false;

    protected void setStopping(boolean stopping) {
        this.stopping = stopping;
    }
    
	protected void onConnect(Channel channel, S connection) {
        synchronized (channel) {
            reconnectAttempts = 0;
            TransportData<S> transportData = channel.getTransportData();
            if (transportData == null) {
                transportData = newTransportData();
                channel.setTransportData(transportData);
            }
            transportData.connect(connection);
        }
        send(channel, null);
    }

    protected void onBinaryMessage(Channel channel, byte[] data, int offset, int length) {
        channel.onMessage(connectMessage, new ByteArrayInputStream(data, offset, length));
    }

    protected void onClose(Channel channel, int closeCode, String message) {
        log.info("Websocket connection closed %d %s channel %s", closeCode, message, channel.getClientId());
        boolean waitBeforeReconnect = !((closeCode == CLOSE_NORMAL || closeCode == CLOSE_SHUTDOWN) && message != null && message.startsWith("Idle"));

        // Mark the connection as closed, the channel should reopen a connection if needed for the next message
        if (channel.getTransportData() != null) {
        	((TransportData<?>)channel.getTransportData()).disconnect();
        	channel.setTransportData(null);
        }
        
        if (stopping || !isStarted()) {
            log.debug("Websocket connection marked as disconnected");
            connected = false;
        }
        else if (closeCode != CLOSE_SHUTDOWN && channel.getClientId() == null) {
            log.debug("Websocket connection could not connect");
            getStatusHandler().handleException(new TransportException("Transport could not connect code: " + closeCode + " " + message));
            return;
        }
        
        if (disconnecting) {
        	channel.onDisconnect();
        	disconnecting = false;
        	connected = false;
        }
        
        if (connected) {
            if (waitBeforeReconnect || reconnectAttempts >= reconnectMaxAttempts) {
                connected = false;
                
                // Notify the channel of disconnect so it can schedule a reconnect if needed
                log.debug("Websocket disconnected");
                channel.onError(connectMessage, new RuntimeException(message + " (code=" + closeCode + ")"));
                getStatusHandler().handleException(new TransportException("Transport disconnected"));
                return;
            }

            reconnectAttempts++;

            // If the channel should be connected, try to reconnect
            log.info("Connection lost (code %d, msg %s), reconnect channel (retry #%d)", closeCode, message, reconnectAttempts);
            connected = true;
            connectMessage = channel.createConnectMessage(connectMessage.getId(), true);
            connect(channel, connectMessage);
        }
    }

    protected void onError(Channel channel, Throwable throwable) {
        log.error(throwable, "Websocket connection error");
        if (throwable instanceof ConnectException)
        	channel.onError(connectMessage, new RuntimeException("Websocket connection error", throwable));
        getStatusHandler().handleException(new TransportException("Websocket connection error: " + throwable.getMessage()));
    }
}
