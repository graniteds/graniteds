package org.granite.gravity.glassfish;

import org.granite.gravity.AbstractChannelFactory;
import org.granite.gravity.Gravity;

public class GlassFishWebSocketChannelFactory extends AbstractChannelFactory<GlassFishWebSocketChannel> {
	
	public GlassFishWebSocketChannelFactory(Gravity gravity) {
		super(gravity);
	}

	public GlassFishWebSocketChannel newChannel(String id) {
		return new GlassFishWebSocketChannel(getGravity(), id, this);
	}

}
