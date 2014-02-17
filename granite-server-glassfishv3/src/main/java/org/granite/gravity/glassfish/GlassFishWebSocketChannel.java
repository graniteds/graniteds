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
package org.granite.gravity.glassfish;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;

import javax.servlet.http.HttpSession;

import org.granite.context.GraniteContext;
import org.granite.context.SimpleGraniteContext;
import org.granite.gravity.AbstractChannel;
import org.granite.gravity.AsyncHttpContext;
import org.granite.gravity.Gravity;
import org.granite.gravity.GravityConfig;
import org.granite.gravity.websocket.AbstractWebSocketChannel;
import org.granite.logging.Logger;
import org.granite.messaging.jmf.JMFDeserializer;
import org.granite.messaging.jmf.JMFSerializer;
import org.granite.messaging.webapp.ServletGraniteContext;
import org.granite.util.ContentType;

import com.sun.grizzly.websockets.DataFrame;
import com.sun.grizzly.websockets.WebSocket;
import com.sun.grizzly.websockets.WebSocketListener;

import flex.messaging.messages.AsyncMessage;
import flex.messaging.messages.Message;


public class GlassFishWebSocketChannel extends AbstractWebSocketChannel implements WebSocketListener {
	
	private static final Logger log = Logger.getLogger(GlassFishWebSocketChannel.class);
	
	private WebSocket websocket;

	public GlassFishWebSocketChannel(Gravity gravity, String id, GlassFishWebSocketChannelFactory factory, String clientType) {
    	super(gravity, id, factory, clientType);
    }

	public void setWebSocket(WebSocket websocket) {
		this.websocket = websocket;
		this.websocket.add(this);

        connect();
	}

	public void onConnect(WebSocket websocket) {
        log.debug("Channel %s onConnect", getId());
	}

	public void onClose(WebSocket websocket, DataFrame frame) {
        log.debug("Channel %s onClose", getId());

        this.websocket = null;
	}
	
	public void onMessage(WebSocket websocket, byte[] data) {
        super.receiveBytes(data, 0, data.length);
	}

    public void onMessage(WebSocket websocket, String message) {
        log.warn("Channel %s unsupported text message", getId());
    }

    public void onFragment(WebSocket websocket, String message, boolean isLast) {
        log.warn("Channel %s unsupported onFragment text message", getId());
    }

    public void onFragment(WebSocket websocket, byte[] data, boolean isLast) {
        log.warn("Channel %s unsupported onFragment binary message", getId());
    }

    public void onPing(WebSocket websocket, byte[] data) {
        log.warn("Channel %s unsupported onPing message", getId());
    }

    public void onPong(WebSocket websocket, byte[] data) {
        log.warn("Channel %s unsupported onPong message", getId());
    }

    @Override
    protected boolean isConnected() {
        return websocket != null && websocket.isConnected();
    }

    @Override
    protected void sendBytes(byte[] msg) {
        websocket.send(msg);
    }

	public void close() {
		if (websocket != null) {
			websocket.close(1000, "Channel closed");
			websocket = null;
		}
	}

}