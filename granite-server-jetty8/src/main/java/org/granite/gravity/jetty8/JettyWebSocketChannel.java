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
package org.granite.gravity.jetty8;

import java.io.IOException;

import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocket.OnBinaryMessage;
import org.granite.gravity.Gravity;
import org.granite.gravity.websocket.AbstractWebSocketChannel;
import org.granite.logging.Logger;


public class JettyWebSocketChannel extends AbstractWebSocketChannel implements WebSocket, OnBinaryMessage {
	
	private static final Logger log = Logger.getLogger(JettyWebSocketChannel.class);
	
	private Connection connection;

	
	public JettyWebSocketChannel(Gravity gravity, String id, JettyWebSocketChannelFactory factory, String clientType) {
    	super(gravity, id, factory, clientType);
    }

	public void onOpen(Connection connection) {
		this.connection = connection;
		this.connection.setMaxIdleTime((int)getGravity().getGravityConfig().getChannelIdleTimeoutMillis());

        connect();
	}

	public void onClose(int closeCode, String message) {
		log.debug("Channel %s websocket connection onClose %d, %s", getId(), closeCode, message);

        this.connection = null;
	}

	public void onMessage(byte[] data, int offset, int length) {
        receiveBytes(data, offset, length);
	}

    @Override
    protected boolean isConnected() {
		return connection != null && connection.isOpen();
    }

    @Override
    protected void sendBytes(byte[] msg) throws IOException {
        connection.sendMessage(msg, 0, msg.length);
    }

	public void close() {
		if (connection != null) {
			connection.close(1000, "Channel closed");
			connection = null;
		}
	}
}