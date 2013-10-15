/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of the Granite Data Services Platform.
 *
 *   Granite Data Services is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Lesser General Public
 *   License as published by the Free Software Foundation; either
 *   version 2.1 of the License, or (at your option) any later version.
 *
 *   Granite Data Services is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 *   General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public
 *   License along with this library; if not, write to the Free Software
 *   Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301,
 *   USA, or see <http://www.gnu.org/licenses/>.
 */
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
import org.granite.gravity.udp.UdpReceiverFactory;

import flex.messaging.messages.AsyncMessage;
import flex.messaging.messages.Message;

public class MockGravity implements Gravity {

    private GraniteConfig graniteConfig;

	private Message lastMessage;
	
	public Message getLastMessage() {
		return lastMessage;
	}

    public void resetLastMessage() {
        lastMessage = null;
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

    public void setGraniteConfig(GraniteConfig graniteConfig) {
        this.graniteConfig = graniteConfig;
    }

	public GraniteConfig getGraniteConfig() {
		return graniteConfig;
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

	public GraniteContext initThread(String sessionId, String clientType) {
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

    @Override
    public boolean hasUdpReceiverFactory() {
        return false;
    }

    @Override
    public UdpReceiverFactory getUdpReceiverFactory() {
        return null;
    }

    @Override
    public Channel removeChannel(String clientId, boolean timeout) {
        return null;
    }
}