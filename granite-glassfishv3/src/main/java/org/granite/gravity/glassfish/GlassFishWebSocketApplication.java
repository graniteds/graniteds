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


	public GlassFishWebSocketApplication(ServletContext servletContext, Gravity gravity, String mapping) {
		this.servletContext = servletContext;
		this.gravity = gravity;
		this.mapping = Pattern.compile(".*" + mapping.replace("*", ".*") + "$");
	}

	@Override
	public List<String> getSupportedProtocols(List<String> subProtocol) {
		if (subProtocol.contains("org.granite.gravity"))
			return Collections.singletonList("org.granite.gravity");
		return Collections.emptyList();
	}

	@Override
	public boolean isApplicationRequest(Request request) {
        final String uri = request.requestURI().toString();
        if (!mapping.matcher(uri).matches())
        	return false;
        
    	request.getParameters().handleQueryParameters();	// Force parse of query parameters
		String connectMessageId = request.getHeader("connectId");
		if (connectMessageId == null && request.getParameters().getParameter("connectId") != null)
			connectMessageId = request.getParameters().getParameter("connectId");
		String clientId = request.getHeader("GDSClientId") != null ? request.getHeader("GDSClientId") : request.getParameters().getParameter("GDSClientId");
		String sessionId = null;
		
		for (int i = 0; i < request.getCookies().getCookieCount(); i++) {
			if ("JSESSIONID".equals(request.getCookies().getCookie(i).getName())) {
				sessionId = request.getCookies().getCookie(i).getValue().getString();
				break;
			}
		}
		String clientType = null;
		if (request.getHeader("GDSClientType") != null)
			clientType = request.getHeader("GDSClientType");
		if (clientType == null && request.getParameters().getParameter("GDSClientType") != null)
			clientType = request.getParameters().getParameter("GDSClientType");
		
		// Utterly hackish and ugly: we create the thread local here because there is no other way to access the request
		// It will be cleared in onConnect which executes later in the same thread
		ServletGraniteContext graniteContext = ServletGraniteContext.createThreadInstance(gravity.getGraniteConfig(), gravity.getServicesConfig(), 
				servletContext, sessionId, clientType);
		if (connectMessageId != null)
			graniteContext.getRequest().setAttribute("connectId", connectMessageId);
		if (clientId != null)
			graniteContext.getRequest().setAttribute("clientId", clientId);
		
		return true;
	}

	@Override
    public void onConnect(WebSocket websocket) {
		GlassFishWebSocketChannelFactory channelFactory = new GlassFishWebSocketChannelFactory(gravity);
		
		try {
			log.info("WebSocket connection");
			ServletGraniteContext graniteContext = (ServletGraniteContext)GraniteContext.getCurrentInstance();
			
			String connectMessageId = (String)graniteContext.getRequest().getAttribute("connectId");
			String clientId = (String)graniteContext.getRequest().getAttribute("clientId");
			
			CommandMessage pingMessage = new CommandMessage();
			pingMessage.setMessageId(connectMessageId != null ? connectMessageId : "OPEN_CONNECTION");
			pingMessage.setOperation(CommandMessage.CLIENT_PING_OPERATION);
			if (clientId != null)
				pingMessage.setClientId(clientId);
			
			Message ackMessage = gravity.handleMessage(channelFactory, pingMessage);
			
			GlassFishWebSocketChannel channel = gravity.getChannel(channelFactory, (String)ackMessage.getClientId());
			if (!ackMessage.getClientId().equals(clientId))
				channel.setConnectAckMessage(ackMessage);
			channel.setWebSocket(websocket);
		}
		finally {
			GraniteContext.release();
		}
    }
}
