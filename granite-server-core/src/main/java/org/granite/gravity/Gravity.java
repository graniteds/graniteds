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

import org.granite.config.GraniteConfig;
import org.granite.config.flex.ServicesConfig;

import flex.messaging.messages.AsyncMessage;
import flex.messaging.messages.Message;

/**
 * @author Franck WOLFF
 */
public interface Gravity {

    ///////////////////////////////////////////////////////////////////////////
    // Granite/Services configs access.

	/**
	 * Current Gravity configuration
	 * @return config
	 */
    public GravityConfig getGravityConfig();
    
    /**
     * Current services configuration
     * @return config
     */
    public ServicesConfig getServicesConfig();
    
    /**
     * Current Granite configuration
     * @return config
     */
    public GraniteConfig getGraniteConfig();

    ///////////////////////////////////////////////////////////////////////////
    // Properties.

    /**
     * State of the Gravity component
     * @return true if started
     */
	public boolean isStarted();

    ///////////////////////////////////////////////////////////////////////////
    // Operations.
	
	/**
	 * Start the Gravity component
	 * @throws Exception
	 */
    public void start() throws Exception;
    
    /**
     * Update the current configuration and restarts
     * @param gravityConfig Gravity config
     * @param graniteConfig Granite config
     */
    public void reconfigure(GravityConfig gravityConfig, GraniteConfig graniteConfig);
    
    /**
     * Stop the Gravity component
     * @throws Exception
     */
    public void stop() throws Exception;
    
    /**
     * Stop the Gravity component, optionally forcing immediate shutdown (without waiting for channel closing)
     * @throws Exception
     */
    public void stop(boolean now) throws Exception;
    
    /**
     * Registers a listener which will be notified of channel events (connect/disconnect/subscription/unsubscription)
     * @param listener 
     */
    public void registerListener(Listener listener);
    
    /**
     * Unregisters a listener which will be notified of channel events (connect/disconnect/subscription/unsubscription)
     * @param listener 
     */
    public void unregisterListener(Listener listener);

    /**
     * Currently connected channels
     * @return list of channels
     */
    public List<Channel> getConnectedChannels();
    
    /**
     * Currently connected authenticated users
     * @return set of users
     */
    public Set<Principal> getConnectedUsers();
    
    /**
     * Channels currently connected for a specified destination
     * @param destination a destination
     * @return list of channels
     */
    public List<Channel> getConnectedChannelsByDestination(String destination);
    
    /**
     * Authenticated users currently connected for a specified destination
     * @param destination a destination
     * @return set of users
     */
    public Set<Principal> getConnectedUsersByDestination(String destination);
    
    /**
     * Find all channels for an authenticated user name
     * @param name user name
     * @return list of channels
     */
    public List<Channel> findConnectedChannelsByUser(String name);
    
    /**
     * Find the channel for a specified clientId
     * @param clientId client id
     * @return channel
     */
    public Channel findChannelByClientId(String clientId);
    
    /**
     * Current channel for the specified destination
     * @param destination destination
     * @return channel
     */
    public Channel findCurrentChannel(String destination);
    
    /**
     * Handle a message using the configured service adapter
     * @param message message
     * @return acknowledge or error
     */
    public Message handleMessage(Message message);
    
    /**
     * Handle a message using the configured service adapter, optionally skipping interceptors
     * @param message message
     * @param skipInterceptor skip interceptors
     * @return acknowledge or error
     */
    public Message handleMessage(Message message, boolean skipInterceptor);

    /**
     * Publish a message to connected users from the default server channel
     * @param message message to publish
     * @return acknowledge or error
     */
    public Message publishMessage(AsyncMessage message);
    
    /**
     * Publish a message to connected users from the specified user channel
     * @param fromChannel originating channel
     * @param message message to publish
     * @return acknowledge or error
     */
    public Message publishMessage(Channel fromChannel, AsyncMessage message);
    
    /**
     * Send a server-to-client request, waiting synchronously for the response
     * @param fromChannel originating channel
     * @param message request
     * @return response
     */
    public Message sendRequest(Channel fromChannel, AsyncMessage message);
    
    
    
    public interface Listener {
    	
    	public void connected(Channel channel);
    	
    	public void subscribed(Channel channel, String subscriptionId);
    	
    	public void authenticated(Channel channel, Principal principal);
    	
    	public void disconnected(Channel channel);
    	
    	public void unsubscribed(Channel channel, String subscriptionId);
    }
}
