package org.granite.test.tide;

import javax.servlet.ServletConfig;

import org.granite.gravity.Channel;
import org.granite.gravity.ChannelFactory;
import org.granite.gravity.GravityConfig;


public class MockChannelFactory implements ChannelFactory {

	@Override
	public void init(GravityConfig gravityConfig, ServletConfig servletConfig) {
	}

	@Override
	public Channel newChannel(String id) {
		return null;
	}

	@Override
	public void destroy() {
	}
}