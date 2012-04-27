package org.granite.gravity.jetty8;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketServlet;
import org.granite.context.GraniteContext;
import org.granite.gravity.Gravity;
import org.granite.gravity.GravityManager;
import org.granite.gravity.GravityServletUtil;
import org.granite.logging.Logger;
import org.granite.messaging.webapp.ServletGraniteContext;

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
		JettyWebSocketChannelFactory channelFactory = new JettyWebSocketChannelFactory(gravity, getServletContext());
		
		try {
			String clientId = request.getParameter("GDSClientId");
			String sessionId = null;
			HttpSession session = request.getSession(false);
			if (session != null)
				sessionId = session.getId();
			if (request.getParameter("GDSSessionId") != null)
				sessionId = request.getParameter("GDSSessionId");
			
	        ServletGraniteContext.createThreadInstance(gravity.getGraniteConfig(), gravity.getServicesConfig(), getServletContext(), sessionId); 
			
			log.info("WebSocket connection started %s clientId %s", protocol, clientId);
			
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
