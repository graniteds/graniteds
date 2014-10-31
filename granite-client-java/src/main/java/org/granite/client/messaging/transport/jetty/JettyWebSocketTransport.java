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
package org.granite.client.messaging.transport.jetty;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.websocket.WebSocket.Connection;
import org.eclipse.jetty.websocket.WebSocket.OnBinaryMessage;
import org.eclipse.jetty.websocket.WebSocketClient;
import org.eclipse.jetty.websocket.WebSocketClientFactory;
import org.granite.client.messaging.channel.Channel;
import org.granite.client.messaging.transport.*;
import org.granite.client.messaging.transport.websocket.AbstractWebSocketTransport;
import org.granite.logging.Logger;


/**
 * @author William DRAI
 */
public class JettyWebSocketTransport extends AbstractWebSocketTransport<Connection> {
	
	private static final Logger log = Logger.getLogger(JettyWebSocketTransport.class);

	private WebSocketClientFactory webSocketClientFactory = new WebSocketClientFactory();
	
	
	public SslContextFactory getSetSslContextFactory() {
		return webSocketClientFactory.getSslContextFactory();
	}

	@Override
	public synchronized boolean start() {
		if (isStarted())
			return true;

		log.info("Starting Jetty WebSocketClient transport...");
		
		try {
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

	public void connect(final Channel channel, final TransportMessage transportMessage) {
		URI uri = channel.getUri();
		
		try {		
			WebSocketClient webSocketClient = webSocketClientFactory.newWebSocketClient();
			webSocketClient.setMaxIdleTime(getMaxIdleTime());
			webSocketClient.setMaxTextMessageSize(1024);
            webSocketClient.setMaxBinaryMessageSize(getMaxMessageSize());
			webSocketClient.setProtocol("org.granite.gravity." + transportMessage.getContentType().substring("application/x-".length()));

			if (transportMessage.getSessionId() != null)
				webSocketClient.getCookies().put("JSESSIONID", transportMessage.getSessionId());

			String u = uri.toString();
			u += "?connectId=" + transportMessage.getId() + "&GDSClientType=" + transportMessage.getClientType();
			if (transportMessage.getClientId() != null)
				u += "&GDSClientId=" + transportMessage.getClientId();
			else if (channel.getClientId() != null)
				u += "&GDSClientId=" + channel.getClientId();

            log.info("Connecting to websocket %s protocol %s sessionId %s", u, webSocketClient.getProtocol(), transportMessage.getSessionId());

			webSocketClient.open(new URI(u), new WebSocketHandler(channel));
		}
		catch (Exception e) {
			getStatusHandler().handleException(new TransportException("Could not connect to uri " + channel.getUri(), e));
		}
	}

    @Override
    public synchronized void stop() {
        if (webSocketClientFactory == null)
            return;

        log.info("Stopping Jetty WebSocketClient transport...");

        super.stop();

        try {
            setStopping(true);

            webSocketClientFactory.stop();
        }
        catch (Exception e) {
            getStatusHandler().handleException(new TransportException("Could not stop Jetty WebSocketFactory", e));

            log.error(e, "Jetty WebSocketClient failed to stop properly.");
        }
        finally {
            setStopping(false);
        }

        log.info("Jetty WebSocketClient transport stopped.");
    }

	private static class JettyTransportData extends TransportData<Connection> {
		
		private Connection connection = null;

        @Override
        public void connect(Connection connection) {
            this.connection = connection;
        }

        @Override
        public boolean isConnected() {
            return connection != null;
        }

        @Override
        public void disconnect() {
            this.connection = null;
        }

        @Override
        public void sendBytes(byte[] data) throws IOException {
            this.connection.sendMessage(data, 0, data.length);
        }
    }

    public TransportData<Connection> newTransportData() {
        return new JettyTransportData();
    }

    private class WebSocketHandler implements OnBinaryMessage {

        private final Channel channel;

        public WebSocketHandler(Channel channel) {
            this.channel = channel;
        }

        @Override
        public void onOpen(Connection connection) {
            onConnect(channel, connection);
        }

        @Override
        public void onMessage(byte[] data, int offset, int length) {
            onBinaryMessage(channel, data, offset, length);
        }

        @Override
        public void onClose(int closeCode, String message) {
            JettyWebSocketTransport.this.onClose(channel, closeCode, message);
        }
    }
}
