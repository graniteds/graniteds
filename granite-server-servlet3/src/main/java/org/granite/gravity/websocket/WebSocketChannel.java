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
package org.granite.gravity.websocket;

import java.io.IOException;
import java.nio.ByteBuffer;

import javax.websocket.CloseReason;
import javax.websocket.MessageHandler;
import javax.websocket.Session;

import org.granite.gravity.GravityInternal;
import org.granite.logging.Logger;


public class WebSocketChannel extends AbstractWebSocketChannel implements MessageHandler.Whole<byte[]> {
	
	private static final Logger log = Logger.getLogger(WebSocketChannel.class);

	private Session session;

	
	public WebSocketChannel(GravityInternal gravity, String id, WebSocketChannelFactory factory, String clientType) {
    	super(gravity, id, factory, clientType);

        log.debug("Create channel %s", getId());
    }

    public void onWebSocketConnect(Session session) {
		this.session = session;
		
		setMaxBinaryMessageBufferSize(session.getMaxBinaryMessageBufferSize());

        connect();
		
        session.addMessageHandler(this);
        
        gravity.notifyConnected(this);
	}

    public void onWebSocketClose(int closeCode, String message) {
        log.debug("Channel %s websocket connection onClose %d, %s", getId(), closeCode, message);

        this.session = null;
        
        gravity.notifyDisconnected(this);
    }

    public void onWebSocketError(Throwable throwable) {
        log.error(throwable, "Channel %s websocket error", getId());
    }
    
    public void onMessage(byte[] data) {
        receiveBytes(data, 0, data.length);
    }

    @Override
    public boolean isConnected() {
        log.debug("Channel %s websocket connection %s", getId(), session == null ? "(null)" : (session.isOpen() ? "(open)" : "(not open)"));
        return session != null && session.isOpen();
    }

    @Override
    protected void sendBytes(byte[] msg) throws IOException {
        if (session != null && session.isOpen()) {
        	if (msg.length <= getMaxBinaryMessageBufferSize())
        		session.getBasicRemote().sendBinary(ByteBuffer.wrap(msg));
        	else {
        		final int length = getMaxBinaryMessageBufferSize();
        		int offset = 0;
        		do {
	        		session.getBasicRemote().sendBinary(ByteBuffer.wrap(msg, offset, length), false);
	        		offset += length;
        		}
        		while (msg.length - offset > length);
        		session.getBasicRemote().sendBinary(ByteBuffer.wrap(msg, offset, msg.length - offset), true);
        	}
        }
    }

	public void close() {
        log.debug("Channel %s close", getId());
		if (session != null) {
            try {
                session.close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "Channel closed"));
            }
            catch (IOException e) {
                throw new RuntimeException("Channel close error " + getId(), e);
            }
            session = null;
            
            gravity.notifyDisconnected(this);
		}
	}
}