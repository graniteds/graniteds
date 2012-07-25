package org.granite.gravity.glassfish;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import org.granite.context.GraniteContext;
import org.granite.context.SimpleGraniteContext;
import org.granite.gravity.Gravity;
import org.granite.logging.Logger;

import com.sun.grizzly.tcp.Request;
import com.sun.grizzly.websockets.WebSocket;
import com.sun.grizzly.websockets.WebSocketApplication;

import flex.messaging.messages.CommandMessage;
import flex.messaging.messages.Message;


public class GlassFishWebSocketApplication extends WebSocketApplication {
	
	private static final Logger log = Logger.getLogger(GlassFishWebSocketApplication.class);
	
	private final Gravity gravity;
	private final Pattern mapping;
	
	public GlassFishWebSocketApplication(Gravity gravity, String mapping) {
		this.gravity = gravity;
		this.mapping = Pattern.compile(".*" + mapping.replace("*", ".*") + "$");
	}

	@Override
	public List<String> getSupportedProtocols(List<String> subProtocol) {
		if (subProtocol.contains("gravity"))
			return Collections.singletonList("gravity");
		return Collections.emptyList();
	}

	@Override
    public void onConnect(WebSocket websocket) {
		GlassFishWebSocketChannelFactory channelFactory = new GlassFishWebSocketChannelFactory(gravity);
		
		try {
	        SimpleGraniteContext.createThreadInstance(
	                gravity.getGraniteConfig(), gravity.getServicesConfig(), null, 
	                new HashMap<String, Object>()
	        );
			
			log.info("WebSocket connection");
			
			CommandMessage pingMessage = new CommandMessage();
			pingMessage.setOperation(CommandMessage.CLIENT_PING_OPERATION);
			
			Message message = gravity.handleMessage(channelFactory, pingMessage);
			
			GlassFishWebSocketChannel channel = gravity.getChannel(channelFactory, (String)message.getClientId());
			channel.setWebSocket(websocket);
		}
		finally {
			GraniteContext.release();
		}
    }

	@Override
	public boolean isApplicationRequest(Request request) {
        final String uri = request.requestURI().toString();
        return mapping.matcher(uri).matches();
	}
}
