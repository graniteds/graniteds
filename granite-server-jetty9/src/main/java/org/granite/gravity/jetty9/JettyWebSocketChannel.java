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
package org.granite.gravity.jetty9;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.granite.gravity.GravityInternal;
import org.granite.gravity.websocket.AbstractWebSocketChannel;
import org.granite.logging.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;


public class JettyWebSocketChannel extends AbstractWebSocketChannel implements WebSocketListener {
	
	private static final Logger log = Logger.getLogger(JettyWebSocketChannel.class);

	private Session socketSession;

	
	public JettyWebSocketChannel(GravityInternal gravity, String id, JettyWebSocketChannelFactory factory, String clientType) {
    	super(gravity, id, factory, clientType);

        log.debug("Create channel %s", getId());
    }

    @Override
    public void onWebSocketConnect(Session session) {
		this.socketSession = session;
		this.socketSession.setIdleTimeout(getGravity().getGravityConfig().getChannelIdleTimeoutMillis());
		
		setMaxBinaryMessageBufferSize(socketSession.getPolicy().getMaxBinaryMessageBufferSize());

        connect();

        log.debug("Channel %s websocket connected %s", getId(), socketSession.isOpen() ? "(open)" : "(closed)");
	}

    @Override
    public void onWebSocketBinary(byte[] data, int offset, int length) {
        super.receiveBytes(data, offset, length);
    }

    @Override
    public void onWebSocketClose(int closeCode, String message) {
        log.debug("Channel %s websocket connection onClose %d, %s", getId(), closeCode, message);

        this.socketSession = null;
    }

    @Override
    public void onWebSocketError(Throwable throwable) {
        log.error(throwable, "Channel %s websocket error");
    }

    @Override
    public void onWebSocketText(String s) {
        log.warn("Channel %s unsupported text message", getId());
    }

    @Override
    protected boolean isConnected() {
        log.debug("Channel %s websocket connection %s", getId(), socketSession == null ? "(null)" : (socketSession.isOpen() ? "(open)" : "(not open)"));
        return socketSession != null && socketSession.isOpen();
    }

    @Override
    protected void sendBytes(byte[] msg) throws IOException {
        socketSession.getRemote().sendBytes(ByteBuffer.wrap(msg));
    }

	public void close() {
        log.debug("Channel %s close", getId());
		if (socketSession != null) {
            socketSession.close(1000, "Channel closed");
            socketSession = null;
		}
	}
}