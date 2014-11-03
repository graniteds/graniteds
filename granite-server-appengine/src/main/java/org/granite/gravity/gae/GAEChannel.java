/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2014 GRANITE DATA SERVICES S.A.S.
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
package org.granite.gravity.gae;

import java.io.Serializable;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.granite.gravity.AsyncHttpContext;
import org.granite.gravity.AsyncPublishedMessage;
import org.granite.gravity.Channel;
import org.granite.gravity.MessagePublishingException;
import org.granite.gravity.MessageReceivingException;
import org.granite.gravity.Subscription;
import org.granite.logging.Logger;

import com.google.appengine.api.memcache.Expiration;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;

import flex.messaging.messages.AsyncMessage;
import flex.messaging.messages.Message;

/**
 * @author William DRAI
 */
public class GAEChannel implements Channel, Serializable {

	private static final long serialVersionUID = 5129029435795219401L;
	
	private static final Logger log = Logger.getLogger(GAEChannel.class);
    
    static final String MSG_COUNT_PREFIX = "org.granite.gravity.channel.msgCount.";
    static final String MSG_PREFIX = "org.granite.gravity.channel.msg.";
    
    private static MemcacheService gaeCache = MemcacheServiceFactory.getMemcacheService();

    protected final String id;
    protected final String clientType;
    protected final GAEGravity gravity;
    protected final GAEChannelFactory factory;

    private final Map<String, Subscription> subscriptions = new HashMap<String, Subscription>();
    private Principal userPrincipal;
    private final long expiration;


    GAEChannel(GAEGravity gravity, String id, GAEChannelFactory factory, String clientType) {
        if (id == null)
        	throw new NullPointerException("id cannot be null");
        
        this.id = id;
        this.clientType = clientType;
        this.factory = factory;
        this.gravity = gravity;
    	this.expiration = gravity.getGravityConfig().getChannelIdleTimeoutMillis();
    }

	public String getId() {
		return id;
	}
	
	public String getClientType() {
		return clientType;
	}
	
	public GAEChannelFactory getFactory() {
		return factory;
	}
	
	public GAEGravity getGravity() {
		return gravity;
	}
    
    private Long msgCount() {
    	return (Long)gaeCache.get(MSG_COUNT_PREFIX + id);
    }
    
    
    public void close() {
    }
    
	public void destroy(boolean timeout) {
    	Long msgCount = msgCount();
    	if (msgCount != null) {
	    	List<Object> list = new ArrayList<Object>();
	    	list.add(MSG_COUNT_PREFIX + id);
	    	for (long i = 0; i < msgCount; i++)
	    		list.add(MSG_PREFIX + id + "#" + i);
	    	gaeCache.deleteAll(list);
    	}
        this.subscriptions.clear();
    }

    
	public void publish(AsyncPublishedMessage message) throws MessagePublishingException {
		message.publish(this);
    }

	public void receive(AsyncMessage message) throws MessageReceivingException {
        log.debug("Publish message to channel %s", id);
//        System.err.println("Publish messages to channel " + id);
        synchronized (this) {
        	Long msgCount = msgCount();
        	gaeCache.put(MSG_PREFIX + id + "#" + msgCount, message, Expiration.byDeltaMillis((int)expiration));
        	gaeCache.increment(MSG_COUNT_PREFIX + id, 1);
        }
	}
    
    public List<Message> takeMessages() {
        log.debug("Try to take messages for channel %s", id);
//        System.err.println("Try to take messages for channel " + id);
        synchronized (this) {
        	Long msgCount = msgCount();
        	if (msgCount == null || msgCount == 0)
                return null;

            log.debug("Taking %s messages", msgCount);
//            System.err.println("Taking " + msgCount + " messages");
        	List<Object> list = new ArrayList<Object>();
        	for (int i = 0; i < msgCount; i++)
        		list.add(MSG_PREFIX + id + "#" + i);
        	Map<Object, Object> msgs = gaeCache.getAll(list);
            List<Message> messages = new ArrayList<Message>();
        	for (int i = 0; i < msgCount; i++) {
        		Message msg = (Message)msgs.get(list.get(i));
        		if (msg != null)
        			messages.add(msg);
        	}
        	
        	gaeCache.deleteAll(list);
        	gaeCache.put(MSG_COUNT_PREFIX + id, 0L, Expiration.byDeltaMillis((int)expiration));
        	
            return messages.isEmpty() ? null : messages;
        }
    }


    public Subscription addSubscription(String destination, String subTopicId, String subscriptionId, boolean noLocal) {
    	Subscription subscription = new Subscription(this, destination, subTopicId, subscriptionId, noLocal);
    	subscriptions.put(subscriptionId, subscription);
    	return subscription;
    }

    public Collection<Subscription> getSubscriptions() {
    	return subscriptions.values();
    }
    
    public Subscription removeSubscription(String subscriptionId) {
    	return subscriptions.remove(subscriptionId);
    }
    
    public boolean isConnected() {
    	return true;
    }
    
    public boolean isAuthenticated() {
    	return userPrincipal != null;
    }

    public Principal getUserPrincipal() {
        return userPrincipal;
    }
    public void setUserPrincipal(Principal principal) {
        this.userPrincipal = principal;
    }

    
    @Override
    public boolean equals(Object obj) {
        return (obj instanceof GAEChannel && id.equals(((GAEChannel)obj).id));
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

	@Override
    public String toString() {
        return getClass().getName() + " {id=" + id + ", subscriptions=" + subscriptions + "}";
    }


	public boolean hasPublishedMessage() {
		return false;
	}

	public boolean runPublish() {
		return false;
	}

	public boolean hasReceivedMessage() {
		return false;
	}

	public boolean runReceive() {
		return false;
	}

	public boolean runReceived(AsyncHttpContext asyncHttpContext) {
		return false;
	}

	public void run() {
	}
}
