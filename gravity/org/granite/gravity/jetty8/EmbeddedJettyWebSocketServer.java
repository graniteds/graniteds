package org.granite.gravity.jetty8;

import java.util.HashMap;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketHandler;
import org.granite.context.GraniteContext;
import org.granite.context.SimpleGraniteContext;
import org.granite.gravity.Gravity;
import org.granite.gravity.GravityManager;
import org.granite.logging.Logger;

import flex.messaging.messages.CommandMessage;
import flex.messaging.messages.Message;

public class EmbeddedJettyWebSocketServer extends Server {
	
	private static final Logger log = Logger.getLogger(EmbeddedJettyWebSocketServer.class);

	private final ServletContext servletContext;
	private int serverPort = 81;
	
	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}
	
	
    public EmbeddedJettyWebSocketServer(ServletContext servletContext) {
    	this.servletContext = servletContext;
    	
    	SelectChannelConnector connector = new SelectChannelConnector();
        connector.setPort(serverPort);
        addConnector(connector);
        
        WebSocketHandler handler = new WebSocketHandler() {           		
            public WebSocket doWebSocketConnect(HttpServletRequest request, String protocol) {
        		Gravity gravity = GravityManager.getGravity(request.getServletContext());
        		JettyWebSocketChannelFactory channelFactory = new JettyWebSocketChannelFactory(gravity, EmbeddedJettyWebSocketServer.this.servletContext);
        		
        		try {
        			String clientId = request.getParameter("GDSClientId");
        			String sessionId = null;
        			HttpSession session = request.getSession(false);
        			if (session != null)
        				sessionId = session.getId();
        			if (request.getHeader("GDSSessionId") != null)
        				sessionId = request.getHeader("GDSSessionId");
        			
        	        SimpleGraniteContext.createThreadInstance(
        	                gravity.getGraniteConfig(), gravity.getServicesConfig(),
        	                sessionId, 
        	                new HashMap<String, Object>()
        	        );
        			
        			log.info("WebSocket connection " + protocol);
        			
        			CommandMessage pingMessage = new CommandMessage();
        			pingMessage.setOperation(CommandMessage.CLIENT_PING_OPERATION);
        			if (clientId != null)
        				pingMessage.setClientId(clientId);
        			
        			Message message = gravity.handleMessage(channelFactory, pingMessage);
        			
        			return gravity.getChannel(channelFactory, (String)message.getClientId());
        		}
        		finally {
        			GraniteContext.release();
        		}
            }
        };

        setHandler(handler);
    }
}