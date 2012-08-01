package org.granite.gravity.glassfish;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;

import org.granite.context.GraniteContext;
import org.granite.gravity.Gravity;
import org.granite.logging.Logger;
import org.granite.messaging.webapp.ServletGraniteContext;

import com.sun.grizzly.tcp.Request;
import com.sun.grizzly.websockets.WebSocket;
import com.sun.grizzly.websockets.WebSocketApplication;

import flex.messaging.messages.CommandMessage;
import flex.messaging.messages.Message;


public class GlassFishWebSocketApplication extends WebSocketApplication {
	
	private static final Logger log = Logger.getLogger(GlassFishWebSocketApplication.class);
	
	private final ServletContext servletContext;
	private final Gravity gravity;
	private final Pattern mapping;
	
	private String connectMessageId;
	private String clientId;
	private String sessionId;


	public GlassFishWebSocketApplication(ServletContext servletContext, Gravity gravity, String mapping) {
		this.servletContext = servletContext;
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
			ServletGraniteContext.createThreadInstance(gravity.getGraniteConfig(), gravity.getServicesConfig(), servletContext, sessionId);
			
			log.info("WebSocket connection");
			
			CommandMessage pingMessage = new CommandMessage();
			pingMessage.setMessageId(connectMessageId != null ? connectMessageId : "OPEN_CONNECTION");
			pingMessage.setOperation(CommandMessage.CLIENT_PING_OPERATION);
			if (clientId != null)
				pingMessage.setClientId(clientId);
			
			Message ackMessage = gravity.handleMessage(channelFactory, pingMessage);
			
			GlassFishWebSocketChannel channel = gravity.getChannel(channelFactory, (String)ackMessage.getClientId());
			channel.setConnectMessageId(connectMessageId);
			if (!ackMessage.getClientId().equals(clientId))
				channel.setConnectAckMessage(ackMessage);
			channel.setWebSocket(websocket);
		}
		finally {
			GraniteContext.release();
		}
    }

	@Override
	public boolean isApplicationRequest(Request request) {
        final String uri = request.requestURI().toString();
        if (mapping.matcher(uri).matches()) {
			connectMessageId = request.getHeader("connectId") != null ? request.getHeader("connectId") : request.getParameters().getParameter("connectId");
			clientId = request.getHeader("GDSClientId") != null ? request.getHeader("GDSClientId") : request.getParameters().getParameter("GDSClientId");
			sessionId = null;
			if (request.getHeader("GDSSessionId") != null)
				sessionId = request.getHeader("GDSSessionId");
			if (sessionId == null && request.getParameters().getParameter("GDSSessionId") != null)
				sessionId = request.getParameters().getParameter("GDSSessionId");
        	return true;
        }
        return false;
	}
}
