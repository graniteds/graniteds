/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2015 GRANITE DATA SERVICES S.A.S.
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
import java.util.concurrent.CopyOnWriteArraySet;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextAttributeEvent;
import javax.servlet.ServletContextAttributeListener;

import org.granite.config.GraniteConfig;
import org.granite.config.flex.ServicesConfig;

import flex.messaging.messages.AsyncMessage;
import flex.messaging.messages.ErrorMessage;
import flex.messaging.messages.Message;

/**
 * @author William DRAI
 */
public class GravityProxy implements Gravity {

	private ServletContext servletContext;
	
	private Set<Listener> listeners = new CopyOnWriteArraySet<Listener>();

    public GravityProxy() {
    }

	public GravityProxy(ServletContext servletContext) {
		this.servletContext = servletContext;
		
		try {
			servletContext.addListener(gravityListener);
		}
		catch (Throwable t) {
			// Not Servlet 3.0 compatible
		}
	}
	
	protected Gravity getGravity() {
		return GravityManager.getGravity(servletContext);
	}
	
    ///////////////////////////////////////////////////////////////////////////
    // Granite/Services configs access.

	public GravityConfig getGravityConfig() {
        if (!isStarted())
        	return null;
        
    	return getGravity().getGravityConfig();
    }
    public ServicesConfig getServicesConfig() {
        if (!isStarted())
        	return null;
        
    	return getGravity().getServicesConfig();
    }
    public GraniteConfig getGraniteConfig() {
        if (!isStarted())
        	return null;
        
    	return getGravity().getGraniteConfig();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Properties.

	public boolean isStarted() {
		if (getGravity() == null)
			return false;
		
		return getGravity().isStarted();
	}

    @Override
    public void start() throws Exception {
        getGravity().start();
    }

    @Override
    public void reconfigure(GravityConfig gravityConfig, GraniteConfig graniteConfig) {
        if (!isStarted())
        	return;
        
        getGravity().reconfigure(gravityConfig, graniteConfig);
    }

    @Override
    public void stop() throws Exception {
        if (!isStarted())
        	return;
    	
        getGravity().stop();
    }

    @Override
    public void stop(boolean now) throws Exception {
        if (!isStarted())
        	return;
    	
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
    public Channel findChannelByClientId(String clientId) {
        return getGravity().findChannelByClientId(clientId);
    }
    @Override
    public Channel findCurrentChannel(String destination) {
        return getGravity().findCurrentChannel(destination);
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
        if (!isStarted())
            return new ErrorMessage(message, new IllegalStateException("Gravity not initialized"));

    	return getGravity().publishMessage(fromChannel, message);
    }

    @Override
    public Message sendRequest(Channel fromChannel, AsyncMessage message) {
        if (!isStarted())
            return new ErrorMessage(message, new IllegalStateException("Gravity not initialized"));

        return getGravity().sendRequest(fromChannel, message);
    }

	@Override
	public void registerListener(Listener listener) {
		if (getGravity() == null) {
			listeners.add(listener);
			return;
		}
		
        getGravity().registerListener(listener);
	}

	@Override
	public void unregisterListener(Listener listener) {
		if (getGravity() == null) {
			listeners.remove(listener);
			return;
		}
		
        getGravity().unregisterListener(listener);
	}
	
	private ServletContextAttributeListener gravityListener = new ServletContextAttributeListener() {
		
		@Override
		public void attributeReplaced(ServletContextAttributeEvent event) {
		}
		
		@Override
		public void attributeRemoved(ServletContextAttributeEvent event) {
		}
		
		@Override
		public void attributeAdded(ServletContextAttributeEvent event) {
			if (event.getName().equals(GravityManager.GRAVITY_KEY)) {
				Gravity gravity = (Gravity)event.getValue();
				for (Listener listener : listeners)
					gravity.registerListener(listener);
				listeners.clear();
			}
		}
	};
}
