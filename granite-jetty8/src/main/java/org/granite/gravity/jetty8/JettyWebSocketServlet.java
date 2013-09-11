/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of Granite Data Services.
 *
 *   Granite Data Services is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU Library General Public License as published by
 *   the Free Software Foundation; either version 2 of the License, or (at your
 *   option) any later version.
 *
 *   Granite Data Services is distributed in the hope that it will be useful, but
 *   WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *   FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
 *   for more details.
 *
 *   You should have received a copy of the GNU Library General Public License
 *   along with this library; if not, see <http://www.gnu.org/licenses/>.
 */
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
