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
