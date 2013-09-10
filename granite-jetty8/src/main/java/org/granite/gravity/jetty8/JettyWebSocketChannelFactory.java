package org.granite.gravity.jetty8;

import javax.servlet.ServletContext;

import org.granite.gravity.AbstractChannelFactory;
import org.granite.gravity.Gravity;

public class JettyWebSocketChannelFactory extends AbstractChannelFactory<JettyWebSocketChannel> {
	
	private ServletContext servletContext;
	
	public JettyWebSocketChannelFactory(Gravity gravity, ServletContext servletContext) {
		super(gravity);
		this.servletContext = servletContext;
	}

	public JettyWebSocketChannel newChannel(String id, String clientType) {
		return new JettyWebSocketChannel(getGravity(), id, this, servletContext, clientType);
	}

}
