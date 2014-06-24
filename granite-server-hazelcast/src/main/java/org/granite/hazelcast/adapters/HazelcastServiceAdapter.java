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
package org.granite.hazelcast.adapters;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.granite.clustering.TransientReference;
import org.granite.context.GraniteContext;
import org.granite.gravity.Channel;
import org.granite.gravity.MessageReceivingException;
import org.granite.gravity.Subscription;
import org.granite.gravity.adapters.ServiceAdapter;
import org.granite.gravity.selector.TopicMatcher;
import org.granite.logging.Logger;
import org.granite.messaging.service.ServiceException;
import org.granite.messaging.webapp.ServletGraniteContext;
import org.granite.util.XMap;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ITopic;
import com.hazelcast.core.Message;

import flex.messaging.messages.AcknowledgeMessage;
import flex.messaging.messages.AsyncMessage;
import flex.messaging.messages.CommandMessage;
import flex.messaging.messages.ErrorMessage;

/**
 * @author William DRAI
 */
public class HazelcastServiceAdapter extends ServiceAdapter {

    private static final Logger log = Logger.getLogger(HazelcastServiceAdapter.class);
    
	private static final String HAZELCASTCLIENT_KEY_PREFIX = "org.granite.gravity.hazelcastClient.";
	
    protected ConcurrentMap<String, HazelcastClient> hazelcastClients = new ConcurrentHashMap<String, HazelcastClient>();
    private HazelcastInstance hazelcastInstance;
    protected boolean noLocal = false;
    protected boolean sessionSelector = false;

    @Override
    public void configure(XMap adapterProperties, XMap destinationProperties) throws ServiceException {    	
        if (Boolean.TRUE.toString().equals(destinationProperties.get("no-local")))
        	noLocal = true;
        
        if (Boolean.TRUE.toString().equals(destinationProperties.get("session-selector")))
        	sessionSelector = true;
        
    	String instanceName = destinationProperties.get("hazelcast-instance-name");
    	if (instanceName == null)
    		instanceName = "__GDS_HAZELCAST_INSTANCE__";
        
    	Config config = new Config();
    	config.setInstanceName(instanceName);
    	hazelcastInstance = Hazelcast.getOrCreateHazelcastInstance(config);
    }
    
    @Override
    public void start() throws ServiceException {
        super.start();
    }

    @Override
    public void stop() throws ServiceException {
        super.stop();
        
        for (HazelcastClient hazelcastClient : hazelcastClients.values())
        	hazelcastClient.close();
        hazelcastClients.clear();
    }


    private synchronized HazelcastClient initClient(Channel client, ITopic<flex.messaging.messages.Message> destination) throws Exception {
        HazelcastClient hazelcastClient = hazelcastClients.get(client.getId());
        if (hazelcastClient == null) {
        	hazelcastClient = new HazelcastClient(client, destination);
        	hazelcastClients.put(client.getId(), hazelcastClient);
            if (sessionSelector && GraniteContext.getCurrentInstance() instanceof ServletGraniteContext)
                ((ServletGraniteContext)GraniteContext.getCurrentInstance()).getSessionMap().put(HAZELCASTCLIENT_KEY_PREFIX + destination.getName(), hazelcastClient);
            log.debug("Hazelcast client started for channel " + client.getId());
        }
        return hazelcastClient;
    }
    
    private synchronized void closeClientIfNecessary(Channel client, ITopic<flex.messaging.messages.Message> destination) throws Exception {
    	HazelcastClient hazelcastClient = hazelcastClients.get(client.getId());
        if (hazelcastClient != null && !hazelcastClient.hasActiveSubscription()) {
        	hazelcastClient.close();
        	hazelcastClients.remove(client.getId());
            if (sessionSelector && GraniteContext.getCurrentInstance() instanceof ServletGraniteContext)
                ((ServletGraniteContext)GraniteContext.getCurrentInstance()).getSessionMap().remove(HAZELCASTCLIENT_KEY_PREFIX + destination.getName());
            log.debug("Hazelcast client closed for channel " + client.getId());
        }
    }
    
    @Override
    public Object invoke(Channel fromClient, AsyncMessage message) {
    	String topicId = (String)message.getHeader(AsyncMessage.SUBTOPIC_HEADER);
    	
    	if (getSecurityPolicy().canPublish(fromClient, topicId, message)) {
    		try {
    			ITopic<flex.messaging.messages.Message> topic = hazelcastInstance.getTopic(message.getDestination());
    			topic.publish(message);
    			
	            AsyncMessage reply = new AcknowledgeMessage(message);
	            reply.setMessageId(message.getMessageId());
	            
	            return reply;
	        }
	        catch (Exception e) {
	        	log.error(e, "Error sending message");
	            ErrorMessage error = new ErrorMessage(message, null);
	            error.setFaultString("JMS Adapter error " + e.getMessage());
	            
	            return error;
	        }
    	}
    	
    	log.debug("Channel %s tried to publish a message to topic %s", fromClient, topicId);
    	ErrorMessage error = new ErrorMessage(message, null);
    	error.setFaultString("Server.Publish.Denied");
        return error;
    }
    
    @Override
    public Object manage(Channel fromClient, CommandMessage message) {
    	String topicId = (String)message.getHeader(AsyncMessage.SUBTOPIC_HEADER);

        if (message.getOperation() == CommandMessage.SUBSCRIBE_OPERATION) {
        	if (getSecurityPolicy().canSubscribe(fromClient, topicId, message)) {
	            try {
	            	ITopic<flex.messaging.messages.Message> destination = hazelcastInstance.getTopic(message.getDestination());
	            	HazelcastClient hazelcastClient = initClient(fromClient, destination);
	            	hazelcastClient.subscribe(message);
	            	
	                AsyncMessage reply = new AcknowledgeMessage(message);
	                return reply;
	            }
	            catch (Exception e) {
	                throw new RuntimeException("JMSAdapter subscribe error on topic: " + message, e);
	            }
        	}

        	log.debug("Channel %s tried to subscribe to topic %s", fromClient, topicId);
        	ErrorMessage error = new ErrorMessage(message, null);
            error.setFaultString("Server.Subscribe.Denied");
            return error;
        }
        else if (message.getOperation() == CommandMessage.UNSUBSCRIBE_OPERATION) {
            try {
            	ITopic<flex.messaging.messages.Message> destination = hazelcastInstance.getTopic(message.getDestination());
            	HazelcastClient hazelcastClient = initClient(fromClient, destination);
            	hazelcastClient.unsubscribe(message);
            	closeClientIfNecessary(fromClient, destination);
            	
                AsyncMessage reply = new AcknowledgeMessage(message);
                return reply;
            }
            catch (Exception e) {
                throw new RuntimeException("JMSAdapter unsubscribe error on topic: " + message, e);
            }
        }

        return null;
    }

    
    @TransientReference
    private class HazelcastClient {
    	
    	private final Channel client;
    	private final ITopic<flex.messaging.messages.Message> destination;
    	private String topic;
    	private final ConcurrentMap<String, HazelcastSubscription> subscriptions = new ConcurrentHashMap<String, HazelcastSubscription>();
    	
    	public HazelcastClient(Channel client, ITopic<flex.messaging.messages.Message> destination) {
    		this.client = client;
    		this.destination = destination;    		
    	}

        public boolean hasActiveSubscription() {
            return subscriptions != null && !subscriptions.isEmpty();
        }
        
    	public void close() {
    		for (HazelcastSubscription subscription : subscriptions.values())
    			subscription.close();
    		subscriptions.clear();
    	}
    	
        public void subscribe(CommandMessage message) throws Exception {
            String subscriptionId = (String)message.getHeader(AsyncMessage.DESTINATION_CLIENT_ID_HEADER);
            String selector = (String)message.getHeader(CommandMessage.SELECTOR_HEADER);
            this.topic = (String)message.getHeader(AsyncMessage.SUBTOPIC_HEADER);

            internalSubscribe(subscriptionId, selector, this.topic);
        }
        
        private void internalSubscribe(String subscriptionId, String selector, String topic) throws Exception {
            synchronized (subscriptions) {
                HazelcastSubscription subscription = subscriptions.get(subscriptionId);
                Subscription sub = client.addSubscription(destination.getName(), topic, subscriptionId, noLocal);
                if (subscription == null) {
                	subscription = new HazelcastSubscription(client, sub, destination, topic);
                	subscriptions.put(subscriptionId, subscription);
                }
                else
                	sub.setSelector(selector);
            }
        }

        public void unsubscribe(CommandMessage message) throws Exception {
            String subscriptionId = (String)message.getHeader(AsyncMessage.DESTINATION_CLIENT_ID_HEADER);

            synchronized (subscriptions) {
            	HazelcastSubscription subscription = subscriptions.get(subscriptionId);
                try {
	                if (subscription != null)
	                	subscription.close();
                }
                finally {
                	subscriptions.remove(subscriptionId);
	                client.removeSubscription(subscriptionId);
                }
            }
        }
    }

    @TransientReference
    private class HazelcastSubscription implements com.hazelcast.core.MessageListener<flex.messaging.messages.Message> {

        private final String id;
        private final Channel client;
        private final Subscription subscription;
        private final ITopic<flex.messaging.messages.Message> destination;
        private final String topic;


        public HazelcastSubscription(Channel client, Subscription subscription, ITopic<flex.messaging.messages.Message> destination, String topic) {
            this.client = client;
            this.subscription = subscription;
            this.destination = destination;
            this.topic = topic;
            this.id = destination.addMessageListener(this);
        }
        
        public void close() {
        	destination.removeMessageListener(id);
        }
        
		@Override
		public void onMessage(Message<flex.messaging.messages.Message> message) {

            log.debug("Delivering message to channel %s subscription %s", client.getId(), subscription.getSubscriptionId());
            
            if (message.getMessageObject() instanceof AsyncMessage) {
            	String topic = (String)message.getMessageObject().getHeader(AsyncMessage.SUBTOPIC_HEADER);
            	if (!TopicMatcher.matchesTopic(this.topic, topic))
            		return;
            	
            	if (!subscription.accept(client, (AsyncMessage)message.getMessageObject()))
            		return;
            	
	            message.getMessageObject().setDestination(getDestination().getId());
	            message.getMessageObject().setHeader(AsyncMessage.DESTINATION_CLIENT_ID_HEADER, subscription.getSubscriptionId());
	            
	            try {
	            	client.receive((AsyncMessage)message.getMessageObject());
	            }
                catch (MessageReceivingException e) {
                    throw new RuntimeException("Channel delivery Error", e);
                }
            }
            else
            	log.warn("Received message of unknown type %s", message.getMessageObject());
        }
    }
}
