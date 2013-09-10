package org.granite.gravity.tomcat;

import javax.servlet.ServletContext;

import org.granite.gravity.AbstractChannelFactory;
import org.granite.gravity.Gravity;

public class TomcatWebSocketChannelFactory extends AbstractChannelFactory<TomcatWebSocketChannel> {
	
	private ServletContext servletContext;
	
	public TomcatWebSocketChannelFactory(Gravity gravity, ServletContext servletContext) {
		super(gravity);
		this.servletContext = servletContext;
	}

	public TomcatWebSocketChannel newChannel(String id, String clientType) {
		return new TomcatWebSocketChannel(getGravity(), id, this, servletContext, clientType);
	}

}
