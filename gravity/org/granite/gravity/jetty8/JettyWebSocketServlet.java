package org.granite.gravity.jetty8;

import java.util.HashMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketServlet;
import org.granite.context.GraniteContext;
import org.granite.context.SimpleGraniteContext;
import org.granite.gravity.Gravity;
import org.granite.gravity.GravityManager;
import org.granite.gravity.GravityServletUtil;
import org.granite.logging.Logger;

import flex.messaging.messages.CommandMessage;
import flex.messaging.messages.Message;


public class JettyWebSocketServlet extends WebSocketServlet {
	
	private static final Logger log = Logger.getLogger(JettyWebSocketServlet.class);

	private static final long serialVersionUID = 1L;
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		
		GravityServletUtil.init(config);
	}

	
	public WebSocket doWebSocketConnect(HttpServletRequest request, String protocol) {
		Gravity gravity = GravityManager.getGravity(getServletContext());
		JettyWebSocketChannelFactory channelFactory = new JettyWebSocketChannelFactory(gravity);
		
		try {
	        SimpleGraniteContext.createThreadIntance(
	                gravity.getGraniteConfig(), gravity.getServicesConfig(),
	                new HashMap<String, Object>()
	        );
			
			String clientId = request.getParameter("GDSClientId");
			
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
}
