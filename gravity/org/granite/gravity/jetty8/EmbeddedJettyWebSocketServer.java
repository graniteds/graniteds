package org.granite.gravity.jetty8;

import javax.servlet.ServletContext;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.websocket.WebSocketHandler;


public class EmbeddedJettyWebSocketServer extends Server {
	
	private int serverPort = 81;
	
	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}
	
	
    public EmbeddedJettyWebSocketServer(ServletContext servletContext) {
    	SelectChannelConnector connector = new SelectChannelConnector();
        connector.setPort(serverPort);
        addConnector(connector);
        
        WebSocketHandler handler = new JettyWebSocketHandler(servletContext);           		
        setHandler(handler);
    }
}