/*
  GRANITE DATA SERVICES
  Copyright (C) 2011 GRANITE DATA SERVICES S.A.S.

  This file is part of Granite Data Services.

  Granite Data Services is free software; you can redistribute it and/or modify
  it under the terms of the GNU Library General Public License as published by
  the Free Software Foundation; either version 2 of the License, or (at your
  option) any later version.

  Granite Data Services is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
  for more details.

  You should have received a copy of the GNU Library General Public License
  along with this library; if not, see <http://www.gnu.org/licenses/>.
*/

package org.granite.gravity.gae;

import org.granite.config.GraniteConfig;
import org.granite.config.flex.ServicesConfig;
import org.granite.gravity.Channel;
import org.granite.gravity.DefaultGravity;
import org.granite.gravity.GravityConfig;
import org.granite.gravity.Subscription;
import org.granite.util.UUIDUtil;

import com.google.appengine.api.memcache.Expiration;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;

import flex.messaging.messages.Message;

/**
 * @author William DRAI
 * @author Franck WOLFF
 */
public class GAEGravity extends DefaultGravity {

    ///////////////////////////////////////////////////////////////////////////
    // Fields.
    
    static final String CHANNEL_PREFIX = "org.granite.gravity.gae.channel.";
    
    private static MemcacheService gaeCache = MemcacheServiceFactory.getMemcacheService();
    
    ///////////////////////////////////////////////////////////////////////////
    // Constructor.

    public GAEGravity(GravityConfig gravityConfig, ServicesConfig servicesConfig, GraniteConfig graniteConfig) {
    	super(gravityConfig, servicesConfig, graniteConfig);
    }
    

    ///////////////////////////////////////////////////////////////////////////
    // Channel's operations.

    @Override
    protected Channel createChannel() {
    	Channel channel = getGravityConfig().getChannelFactory().newChannel(UUIDUtil.randomUUID());
    	Expiration expiration = Expiration.byDeltaMillis((int)getGravityConfig().getChannelIdleTimeoutMillis());
        gaeCache.put(CHANNEL_PREFIX + channel.getId(), channel, expiration);
    	gaeCache.put(GAEChannel.MSG_COUNT_PREFIX + channel.getId(), 0L, expiration);
        return channel;
    }

    @Override
    public Channel getChannel(String channelId) {
        if (channelId == null)
            return null;

        return (Channel)gaeCache.get(CHANNEL_PREFIX + channelId);
    }


    @Override
    public Channel removeChannel(String channelId) {
        if (channelId == null)
            return null;

        Channel channel = (Channel)gaeCache.get(CHANNEL_PREFIX + channelId);
        if (channel != null) {
            for (Subscription subscription : channel.getSubscriptions()) {
            	Message message = subscription.getUnsubscribeMessage();
            	handleMessage(message, true);
            }

            channel.destroy();
            gaeCache.delete(CHANNEL_PREFIX + channelId);
        	gaeCache.delete(GAEChannel.MSG_COUNT_PREFIX + channelId);
        }

        return channel;
    }

    @Override
    public boolean access(String channelId) {
    	return true;
    }
    
    
    @Override
    public void internalStart() {
    	// Avoid starting thread pool
    }

    
    @Override
    protected void postManage(Channel channel) {
    	Expiration expiration = Expiration.byDeltaMillis((int)getGravityConfig().getChannelIdleTimeoutMillis());
    	gaeCache.put(CHANNEL_PREFIX + channel.getId(), channel, expiration);
    }
}
