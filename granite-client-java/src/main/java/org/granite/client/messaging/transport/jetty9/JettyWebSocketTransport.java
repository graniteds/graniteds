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
package org.granite.client.messaging.transport.jetty9;

import org.eclipse.jetty.util.HttpCookieStore;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.granite.client.messaging.channel.Channel;
import org.granite.client.messaging.transport.TransportException;
import org.granite.client.messaging.transport.TransportMessage;
import org.granite.client.messaging.transport.websocket.AbstractWebSocketTransport;
import org.granite.logging.Logger;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.HttpCookie;
import java.nio.ByteBuffer;


/**
 * @author William DRAI
 */
public class JettyWebSocketTransport extends AbstractWebSocketTransport<Session> {
	
	private static final Logger log = Logger.getLogger(JettyWebSocketTransport.class);

	private WebSocketClient webSocketClient = null;
	private SslContextFactory sslContextFactory = null;
	
	public void setSslContextFactory(SslContextFactory sslContextFactory) {
		this.sslContextFactory = sslContextFactory;
	}

	@Override
	public synchronized boolean start() {
		if (isStarted())
			return true;

		log.info("Starting Jetty 9 WebSocketClient transport...");
		
		try {
			if (sslContextFactory == null)
				webSocketClient = new WebSocketClient();			
			else {
				Constructor<WebSocketClient> c = WebSocketClient.class.getConstructor(SslContextFactory.class);
				webSocketClient = c.newInstance(sslContextFactory);
			}
            webSocketClient.setMaxIdleTimeout(getMaxIdleTime());
            webSocketClient.setMaxTextMessageBufferSize(1024);
            webSocketClient.setMaxBinaryMessageBufferSize(getMaxMessageSize());
            webSocketClient.setCookieStore(new HttpCookieStore());
            webSocketClient.start();

			log.info("Jetty 9 WebSocketClient transport started.");
			return true;
		}
		catch (Exception e) {
			webSocketClient = null;
			getStatusHandler().handleException(new TransportException("Could not start Jetty 9 WebSocketFactory", e));
			
			log.error(e, "Jetty 9 WebSocketClient transport failed to start.");
			return false;
		}
	}
	
	public synchronized boolean isStarted() {
		return webSocketClient != null;
	}

    @Override
	public void connect(final Channel channel, final TransportMessage transportMessage) {
		try {
            ClientUpgradeRequest request = new ClientUpgradeRequest();
            request.setRequestURI(channel.getUri());

            String protocol = "org.granite.gravity." + transportMessage.getContentType().substring("application/x-".length());
            request.setSubProtocols(protocol);

			if (transportMessage.getSessionId() != null)
                webSocketClient.getCookieStore().add(channel.getUri(), new HttpCookie("JSESSIONID", transportMessage.getSessionId()));

            request.setCookiesFrom(webSocketClient.getCookieStore());

			request.setHeader("connectId", transportMessage.getId());
            request.setHeader("GDSClientType", transportMessage.getClientType().toString());
            String clientId = transportMessage.getClientId() != null ? transportMessage.getClientId() : channel.getClientId();
            if (clientId != null)
                request.setHeader("GDSClientId", clientId);

            log.info("Connecting to websocket %s protocol %s sessionId %s clientId %s", channel.getUri(), protocol, transportMessage.getSessionId(), clientId);

			webSocketClient.connect(new WebSocketHandler(channel), channel.getUri(), request);
		}
		catch (Exception e) {
            log.error(e, "Could not connect to uri %s", channel.getUri());
			getStatusHandler().handleException(new TransportException("Could not connect to uri " + channel.getUri(), e));
		}
	}

    @Override
    public synchronized void stop() {
        if (webSocketClient == null)
            return;

        log.info("Stopping Jetty 9 WebSocketClient transport...");

        setStopping(true);

        super.stop();

        try {
            webSocketClient.stop();
        }
        catch (Exception e) {
            getStatusHandler().handleException(new TransportException("Could not stop Jetty 9 WebSocketFactory", e));

            log.error(e, "Jetty 9 WebSocketClient failed to stop properly.");
        }
        finally {
            webSocketClient.destroy();
            webSocketClient = null;

            setStopping(false);
        }

        log.info("Jetty 9 WebSocketClient transport stopped.");
    }

    @Override
    protected TransportData<Session> newTransportData() {
        return new Jetty9TransportData();
    }

	public static class Jetty9TransportData extends TransportData<Session> {
		
		private Session session = null;

        @Override
        public void connect(Session session) {
            this.session = session;
        }

        @Override
        public boolean isConnected() {
            return session != null;
        }

        @Override
        public void disconnect() {
            this.session = null;
        }

        @Override
        public void sendBytes(byte[] data) throws IOException {
            session.getRemote().sendBytes(ByteBuffer.wrap(data));
        }
    }


    private class WebSocketHandler implements WebSocketListener {

        private final Channel channel;

        public WebSocketHandler(Channel channel) {
            this.channel = channel;
        }

        @Override
        public void onWebSocketConnect(Session session) {
            onConnect(channel, session);
        }

        @Override
        public void onWebSocketBinary(byte[] data, int offset, int length) {
            onBinaryMessage(channel, data, offset, length);
        }

        @Override
        public void onWebSocketClose(int closeCode, String message) {
            onClose(channel, closeCode, message);
        }

        @Override
        public void onWebSocketError(Throwable throwable) {
            onError(channel, throwable);
        }

        @Override
        public void onWebSocketText(String s) {
            log.warn("Websocket text message not supported");
        }
    }
}
