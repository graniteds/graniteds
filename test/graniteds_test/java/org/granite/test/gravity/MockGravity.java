/**
 * 
 */
package org.granite.test.gravity;

import org.granite.config.GraniteConfig;
import org.granite.config.flex.ServicesConfig;
import org.granite.context.GraniteContext;
import org.granite.gravity.AsyncChannelRunner;
import org.granite.gravity.Channel;
import org.granite.gravity.ChannelFactory;
import org.granite.gravity.Gravity;
import org.granite.gravity.GravityConfig;
import org.granite.gravity.adapters.ServiceAdapter;

import flex.messaging.messages.AsyncMessage;
import flex.messaging.messages.Message;

public class MockGravity implements Gravity {
	
	private Message lastMessage;
	
	public Message getLastMessage() {
		return lastMessage;
	}
	
	public boolean access(String arg0) {
		return false;
	}

	public boolean cancel(AsyncChannelRunner arg0) {
		return false;
	}

	public void execute(AsyncChannelRunner arg0) {
	}

	public <C extends Channel> C getChannel(ChannelFactory<C> channelFactory, String channelId) {
		return null;
	}

	public GraniteConfig getGraniteConfig() {
		return null;
	}

	public GravityConfig getGravityConfig() {
		return null;
	}

	public ServiceAdapter getServiceAdapter(String arg0, String arg1) {
		return null;
	}

	public ServicesConfig getServicesConfig() {
		return null;
	}

	public Message handleMessage(Message arg0) {
		return null;
	}

	public Message handleMessage(ChannelFactory<? extends Channel> channelFactory, Message message) {
		return null;
	}

	public Message handleMessage(ChannelFactory<? extends Channel> channelFactory, Message message, boolean b) {
		return null;
	}

	public GraniteContext initThread() {
		return null;
	}

	public boolean isStarted() {
		return false;
	}

	public Message publishMessage(AsyncMessage message) {
		lastMessage = message;
		return null;
	}

	public Message publishMessage(Channel channelId, AsyncMessage message) {
		lastMessage = message;
		return null;
	}

	public void reconfigure(GravityConfig arg0, GraniteConfig arg1) {
	}

	public void releaseThread() {
	}

	public Channel removeChannel(String arg0) {
		return null;
	}

	public void start() throws Exception {
	}

	public void stop() throws Exception {
	}

	public void stop(boolean arg0) throws Exception {
	}
}