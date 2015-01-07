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
package org.granite.gravity.websocket;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class PolicyFileServerListener implements ServletContextListener {
	
	private PolicyFileServer server = null;

	public void contextInitialized(ServletContextEvent sce) {
		server = new PolicyFileServer();
		
		String serverPort = sce.getServletContext().getInitParameter("flashPolicyFileServer-port");
		if (serverPort != null) {
			try {
				server.setServerPort(Integer.parseInt(serverPort));
			}
			catch (NumberFormatException e) {
				throw new RuntimeException("Incorrect port " + serverPort + " defined for flash-policy-file-server-port", e);
			}
		}
		
		String allow = sce.getServletContext().getInitParameter("flashPolicyFileServer-allowDomains");
		if (allow != null) {
			String[] a = allow.split(",");
			String[] domains = new String[a.length];
			String[] ports = new String[a.length];
			for (int i = 0; i < a.length; i++) {
				int idx = a[i].indexOf(":");
				if (idx < 0) {
					domains[i] = a[i];
					ports[i] = "80,443";
				}
				else {
					domains[i] = a[i].substring(0, idx);
					ports[i] = a[i].substring(idx+1);
				}
			}
			server.setAllowDomains(domains);
			server.setAllowPorts(ports);
		}
		
		server.start();
	}

	public void contextDestroyed(ServletContextEvent sce) {
		server.stop();
	}

}
