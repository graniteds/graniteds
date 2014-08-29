/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2014 GRANITE DATA SERVICES S.A.S.
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
package org.granite.gravity.jetty9;

import flex.messaging.messages.CommandMessage;
import flex.messaging.messages.Message;

import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;
import org.granite.context.GraniteContext;
import org.granite.gravity.GravityInternal;
import org.granite.gravity.GravityManager;
import org.granite.gravity.websocket.WebSocketUtil;
import org.granite.logging.Logger;
import org.granite.messaging.webapp.ServletGraniteContext;
import org.granite.util.ContentType;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

public class JettyWebSocketCreator implements WebSocketCreator {
	
	private static final Logger log = Logger.getLogger(JettyWebSocketCreator.class);
	
	private final ServletContext servletContext;

	public JettyWebSocketCreator(ServletContext servletContext) {
		this.servletContext = servletContext;
	}

    @Override
    public Object createWebSocket(ServletUpgradeRequest servletUpgradeRequest, ServletUpgradeResponse servletUpgradeResponse) {
        String protocol = null;
        for (String p : servletUpgradeRequest.getSubProtocols()) {
            if (p.startsWith("org.granite.gravity")) {
                protocol = p;
                break;
            }
        }
        if (protocol == null)
            return null;

        GravityInternal gravity = (GravityInternal)GravityManager.getGravity(servletContext);
		JettyWebSocketChannelFactory channelFactory = new JettyWebSocketChannelFactory(gravity);
		
		try {
			String connectMessageId = getHeaderOrParameter(servletUpgradeRequest, "connectId");
			String clientId = getHeaderOrParameter(servletUpgradeRequest, "GDSClientId");
			String clientType = getHeaderOrParameter(servletUpgradeRequest, "GDSClientType");
			
			String sessionId = null;
			HttpSession session = servletUpgradeRequest.getSession();
			if (session != null) {
		        ServletGraniteContext.createThreadInstance(gravity.getGraniteConfig(), gravity.getServicesConfig(), 
		        		this.servletContext, session, clientType);
		        
				sessionId = session.getId();
			}
			else if (servletUpgradeRequest.getCookies() != null) {
				for (int i = 0; i < servletUpgradeRequest.getCookies().size(); i++) {
					if ("JSESSIONID".equals(servletUpgradeRequest.getCookies().get(i).getName())) {
						sessionId = servletUpgradeRequest.getCookies().get(i).getValue();
						break;
					}
				}				
		        ServletGraniteContext.createThreadInstance(gravity.getGraniteConfig(), gravity.getServicesConfig(), 
		        		this.servletContext, sessionId, clientType);
			}
            else {
                ServletGraniteContext.createThreadInstance(gravity.getGraniteConfig(), gravity.getServicesConfig(),
                        this.servletContext, (String)null, clientType);
            }
			
			CommandMessage pingMessage = new CommandMessage();
			pingMessage.setMessageId(connectMessageId != null ? connectMessageId : "OPEN_CONNECTION");
			pingMessage.setOperation(CommandMessage.CLIENT_PING_OPERATION);
			if (clientId != null)
				pingMessage.setClientId(clientId);
			
			Message ackMessage = gravity.handleMessage(channelFactory, pingMessage);
            if (sessionId != null)
                ackMessage.setHeader("JSESSIONID", sessionId);

            log.info("WebSocket connection started %s connectId %s clientId %s ackClientId %s sessionId %s", protocol, pingMessage.getMessageId(), clientId, ackMessage.getClientId(), sessionId);

            if (gravity.getGraniteConfig().getSecurityService() != null)
                gravity.getGraniteConfig().getSecurityService().prelogin(session, servletUpgradeRequest.getHttpServletRequest(), null);

            JettyWebSocketChannel channel = gravity.getChannel(channelFactory, (String)ackMessage.getClientId());
            channel.setSession(session);

            String ctype = servletUpgradeRequest.getHeader("Content-Type");
            ContentType contentType = WebSocketUtil.getContentType(ctype, protocol);
			channel.setContentType(contentType);
			
            channel.setConnectAckMessage(ackMessage);
			
			return channel;
		}
		finally {
			GraniteContext.release();
		}
    }
    
    private static String getHeaderOrParameter(ServletUpgradeRequest servletUpgradeRequest, String key) {
    	String value = servletUpgradeRequest.getHeader(key);
    	if (value == null)
    		value = servletUpgradeRequest.getHttpServletRequest().getParameter(key);
    	return value;
    }
}
