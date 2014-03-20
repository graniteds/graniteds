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

import java.security.Principal;
import java.util.List;
import java.util.Set;

import org.granite.config.GraniteConfig;
import org.granite.config.flex.ServicesConfig;
import org.granite.gravity.Channel;
import org.granite.gravity.Gravity;
import org.granite.gravity.GravityConfig;
import org.granite.messaging.jmf.SharedContext;

import flex.messaging.messages.AsyncMessage;
import flex.messaging.messages.Message;

public class MockGravity implements Gravity {

    private GraniteConfig graniteConfig;
    private SharedContext sharedContext;

	private Message lastMessage;
	
	public Message getLastMessage() {
		return lastMessage;
	}

    public void resetLastMessage() {
        lastMessage = null;
    }

    public void setGraniteConfig(GraniteConfig graniteConfig) {
        this.graniteConfig = graniteConfig;
    }

	public GraniteConfig getGraniteConfig() {
		return graniteConfig;
	}

    public void setSharedContext(SharedContext sharedContext) {
        this.sharedContext = sharedContext;
    }

    public SharedContext getSharedContext() {
        return sharedContext;
    }

    public ServicesConfig getServicesConfig() {
        return null;
    }

	public GravityConfig getGravityConfig() {
		return null;
	}

	public Message handleMessage(Message message) {
		return null;
	}

    public Message handleMessage(Message message, boolean skip) {
        return null;
    }

	public boolean isStarted() {
		return false;
	}

	public Message publishMessage(AsyncMessage message) {
		lastMessage = message;
		return null;
	}

	public Message publishMessage(Channel channel, AsyncMessage message) {
		lastMessage = message;
		return null;
	}

    public Message sendRequest(Channel channel, AsyncMessage message) {
        return null;
    }

	public void reconfigure(GravityConfig arg0, GraniteConfig arg1) {
	}

	public void start() throws Exception {
	}

	public void stop() throws Exception {
	}

	public void stop(boolean arg0) throws Exception {
	}

    @Override
    public List<Channel> getConnectedChannels() {
        return null;
    }

    @Override
    public Set<Principal> getConnectedUsers() {
        return null;
    }

    @Override
    public List<Channel> getConnectedChannelsByDestination(String destination) {
        return null;
    }

    @Override
    public Set<Principal> getConnectedUsersByDestination(String destination) {
        return null;
    }

    @Override
    public List<Channel> findConnectedChannelsByUser(String name) {
        return null;
    }

    @Override
    public Channel findConnectedChannelByClientId(String clientId) {
        return null;
    }
}