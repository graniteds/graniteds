/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2015 GRANITE DATA SERVICES S.A.S.
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
package org.granite.gravity.jetty8;

import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketHandler;
import org.eclipse.jetty.websocket.WebSocketServlet;
import org.granite.gravity.GravityServletUtil;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;


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
