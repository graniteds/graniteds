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

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class EmbeddedJettyWebSocketServerListener implements ServletContextListener {
	
	private EmbeddedJettyWebSocketServer server = null;

	public void contextInitialized(ServletContextEvent sce) {
		server = new EmbeddedJettyWebSocketServer(sce.getServletContext());
		
		String serverPort = sce.getServletContext().getInitParameter("embedded-jetty-websocket-server-port");
		if (serverPort != null) {
			try {
				server.setServerPort(Integer.parseInt(serverPort));
			}
			catch (NumberFormatException e) {
				throw new RuntimeException("Incorrect port " + serverPort + " defined for embedded-jetty-websocket-server-port", e);
			}
		}
		
		try {
			server.start();
		}
		catch (Exception e) {
			throw new RuntimeException("Could not start embedded Jetty websocket server", e);
		}
	}

	public void contextDestroyed(ServletContextEvent sce) {
		try {
			if (server != null)
				server.stop();
		}
		catch (Exception e) {
			throw new RuntimeException("Could not stop embedded Jetty websocket server", e);
		}
	}

}
