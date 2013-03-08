package org.granite.test.tide;

import org.granite.gravity.Channel;
import org.granite.gravity.ChannelFactory;


public class MockChannelFactory implements ChannelFactory<Channel> {

	@Override
	public Channel newChannel(String id, String clientType) {
		return null;
	}

	@Override
	public void destroy() {
	}
}