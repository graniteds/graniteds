package org.granite.gravity.jetty8;

import org.granite.gravity.AbstractChannelFactory;
import org.granite.gravity.Gravity;

public class JettyWebSocketChannelFactory extends AbstractChannelFactory<JettyWebSocketChannel> {
	
	public JettyWebSocketChannelFactory(Gravity gravity) {
		super(gravity);
	}

	public JettyWebSocketChannel newChannel(String id) {
		return new JettyWebSocketChannel(getGravity(), id, this);
	}

}
