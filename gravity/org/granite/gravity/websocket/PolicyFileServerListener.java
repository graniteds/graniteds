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
		}
		
		server.start();
	}

	public void contextDestroyed(ServletContextEvent sce) {
		server.stop();
	}

}
