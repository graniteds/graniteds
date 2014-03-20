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
package org.granite.gravity;

import java.security.Principal;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletContext;

import org.granite.config.GraniteConfig;
import org.granite.config.flex.ServicesConfig;
import org.granite.messaging.jmf.SharedContext;

import flex.messaging.messages.AsyncMessage;
import flex.messaging.messages.ErrorMessage;
import flex.messaging.messages.Message;

/**
 * @author William DRAI
 */
public class GravityProxy implements Gravity {

	private ServletContext servletContext;

    public GravityProxy() {
    }

	public GravityProxy(ServletContext servletContext) {
		this.servletContext = servletContext;
	}
	
	protected Gravity getGravity() {
		return GravityManager.getGravity(servletContext);
	}

    ///////////////////////////////////////////////////////////////////////////
    // Granite/Services configs access.

	public GravityConfig getGravityConfig() {
    	return getGravity().getGravityConfig();
    }
    public ServicesConfig getServicesConfig() {
    	return getGravity().getServicesConfig();
    }
    public GraniteConfig getGraniteConfig() {
    	return getGravity().getGraniteConfig();
    }
    public SharedContext getSharedContext() {
        return getGravity().getSharedContext();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Properties.

	public boolean isStarted() {
		return getGravity().isStarted();
	}

    @Override
    public void start() throws Exception {
        getGravity().start();
    }

    @Override
    public void reconfigure(GravityConfig gravityConfig, GraniteConfig graniteConfig) {
        getGravity().reconfigure(gravityConfig, graniteConfig);
    }

    @Override
    public void stop() throws Exception {
        getGravity().stop();
    }

    @Override
    public void stop(boolean now) throws Exception {
        getGravity().stop(now);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Operations.

    @Override
    public List<Channel> getConnectedChannels() {
        return getGravity().getConnectedChannels();
    }
    @Override
    public Set<Principal> getConnectedUsers() {
        return getGravity().getConnectedUsers();
    }
    @Override
    public List<Channel> getConnectedChannelsByDestination(String destination) {
        return getGravity().getConnectedChannelsByDestination(destination);
    }
    @Override
    public Set<Principal> getConnectedUsersByDestination(String destination) {
        return getGravity().getConnectedUsersByDestination(destination);
    }
    @Override
    public List<Channel> findConnectedChannelsByUser(String name) {
        return getGravity().findConnectedChannelsByUser(name);
    }

    @Override
    public Channel findConnectedChannelByClientId(String clientId) {
        return getGravity().findConnectedChannelByClientId(clientId);
    }

    @Override
    public Message handleMessage(Message message) {
        return getGravity().handleMessage(message);
    }

    @Override
    public Message handleMessage(Message message, boolean skipInterceptor) {
        return getGravity().handleMessage(message, skipInterceptor);
    }

    public Message publishMessage(AsyncMessage message) {
    	return publishMessage(null, message);
    }
    public Message publishMessage(Channel fromChannel, AsyncMessage message) {
        // Should probably throw an exception, not intended to be used through the proxy
        Gravity gravity = getGravity();
        if (gravity == null)
            return new ErrorMessage(message, new IllegalStateException("Gravity not initialized"));

    	return getGravity().publishMessage(fromChannel, message);
    }

    @Override
    public Message sendRequest(Channel fromChannel, AsyncMessage message) {
        Gravity gravity = getGravity();
        if (gravity == null)
            return new ErrorMessage(message, new IllegalStateException("Gravity not initialized"));

        return getGravity().sendRequest(fromChannel, message);
    }
}
