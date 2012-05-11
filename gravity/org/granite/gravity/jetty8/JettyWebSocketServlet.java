package org.granite.gravity.jetty8;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketHandler;
import org.eclipse.jetty.websocket.WebSocketServlet;
import org.granite.gravity.GravityServletUtil;


public class JettyWebSocketServlet extends WebSocketServlet {
	
	private static final long serialVersionUID = 1L;
	
	private WebSocketHandler webSocketHandler;
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		
		GravityServletUtil.init(config);
		
		webSocketHandler = new JettyWebSocketHandler(getServletContext());
	}

	
	public WebSocket doWebSocketConnect(HttpServletRequest request, String protocol) {
		return webSocketHandler.doWebSocketConnect(request, protocol);
	}
}
