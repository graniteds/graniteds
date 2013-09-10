package org.granite.gravity.glassfish;

import com.sun.grizzly.websockets.DefaultWebSocket;
import com.sun.grizzly.websockets.ProtocolHandler;
import com.sun.grizzly.websockets.WebSocketListener;


public class GlassFishWebSocket extends DefaultWebSocket {

	public GlassFishWebSocket(ProtocolHandler protocolHandler, WebSocketListener... listeners) {
		super(protocolHandler, listeners);
	}

}
