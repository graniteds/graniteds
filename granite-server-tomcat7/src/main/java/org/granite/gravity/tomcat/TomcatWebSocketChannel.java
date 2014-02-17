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
package org.granite.gravity.tomcat;

import org.apache.catalina.websocket.MessageInbound;
import org.apache.catalina.websocket.StreamInbound;
import org.apache.catalina.websocket.WsOutbound;
import org.granite.gravity.Gravity;
import org.granite.gravity.websocket.AbstractWebSocketChannel;
import org.granite.logging.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;


public class TomcatWebSocketChannel extends AbstractWebSocketChannel {
	
	private static final Logger log = Logger.getLogger(TomcatWebSocketChannel.class);
	
	private StreamInbound streamInbound = new MessageInboundImpl();
	private WsOutbound connection;

	
	public TomcatWebSocketChannel(Gravity gravity, String id, TomcatWebSocketChannelFactory factory, String clientType) {
    	super(gravity, id, factory, clientType);
    }

	public StreamInbound getStreamInbound() {
		return streamInbound;
	}
	
	public class MessageInboundImpl extends MessageInbound {
		
		@Override
		protected void onOpen(WsOutbound outbound) {			
			connection = outbound;

            connect();
		}

		@Override
		public void onClose(int closeCode) {
			log.debug("WebSocket connection onClose %d", closeCode);
			
			connection = null;
		}
		
		@Override
		public void onBinaryMessage(ByteBuffer buf) {
			byte[] data = buf.array();

            receiveBytes(data, 0, data.length);
		}

		@Override
		protected void onTextMessage(CharBuffer buf) throws IOException {
            log.warn("Channel %s unsupported text message", getId());
		}
	}

    @Override
    protected boolean isConnected() {
        return connection != null;
    }

    @Override
    protected void sendBytes(byte[] msg) throws IOException {
        connection.writeBinaryMessage(ByteBuffer.wrap(msg));
    }

	public void close() {
		if (connection != null) {
			try {
				connection.close(1000, ByteBuffer.wrap("Channel closed".getBytes()));
			}
			catch (IOException e) {
				log.error("Could not close WebSocket connection", e);
			}
			connection = null;
		}
	}
}