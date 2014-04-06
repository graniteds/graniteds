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
package org.granite.gravity.adapters;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.granite.clustering.DistributedDataFactory;
import org.granite.clustering.TransientReference;
import org.granite.config.GraniteConfig;
import org.granite.context.GraniteContext;
import org.granite.gravity.Channel;
import org.granite.gravity.GravityInternal;
import org.granite.gravity.MessageReceivingException;
import org.granite.logging.Logger;
import org.granite.messaging.amf.io.AMF3Deserializer;
import org.granite.messaging.amf.io.AMF3Serializer;
import org.granite.messaging.service.ServiceException;
import org.granite.messaging.webapp.ServletGraniteContext;
import org.granite.util.XMap;

import flex.messaging.messages.AcknowledgeMessage;
import flex.messaging.messages.AsyncMessage;
import flex.messaging.messages.CommandMessage;
import flex.messaging.messages.ErrorMessage;

/**
 * @author William DRAI
 */
public class JMSServiceAdapter extends ServiceAdapter {

    private static final Logger log = Logger.getLogger(JMSServiceAdapter.class);
    
    public static final long DEFAULT_FAILOVER_RETRY_INTERVAL = 1000L;
    public static final long DEFAULT_RECONNECT_RETRY_INTERVAL = 20000L;
    public static final int DEFAULT_FAILOVER_RETRY_COUNT = 4;

    protected ConnectionFactory jmsConnectionFactory = null;
    protected javax.jms.Destination jmsDestination = null;
    protected ConcurrentMap<String, JMSClient> jmsClients = new ConcurrentHashMap<String, JMSClient>();
    protected String destinationName = null;
    protected boolean textMessages = false;
    protected boolean transactedSessions = false;
    protected int acknowledgeMode = Session.AUTO_ACKNOWLEDGE;
    protected int messagePriority = javax.jms.Message.DEFAULT_PRIORITY;
    protected int deliveryMode = javax.jms.Message.DEFAULT_DELIVERY_MODE;
    protected boolean noLocal = false;
    protected boolean sessionSelector = false;
    
    protected long failoverRetryInterval = DEFAULT_FAILOVER_RETRY_INTERVAL;
    protected int failoverRetryCount = DEFAULT_FAILOVER_RETRY_COUNT;
    protected long reconnectRetryInterval = DEFAULT_RECONNECT_RETRY_INTERVAL;


    @Override
    public void configure(XMap adapterProperties, XMap destinationProperties) throws ServiceException {
        super.configure(adapterProperties, destinationProperties);

    	log.info("Using JMS configuration: %s", destinationProperties.getOne("jms"));
    	
        destinationName = destinationProperties.get("jms/destination-name");
        
        if (Boolean.TRUE.toString().equals(destinationProperties.get("jms/transacted-sessions")))
            transactedSessions = true;
        
        String ackMode = destinationProperties.get("jms/acknowledge-mode");
        if ("AUTO_ACKNOWLEDGE".equals(ackMode))
            acknowledgeMode = Session.AUTO_ACKNOWLEDGE;
        else if ("CLIENT_ACKNOWLEDGE".equals(ackMode))
            acknowledgeMode = Session.CLIENT_ACKNOWLEDGE;
        else if ("DUPS_OK_ACKNOWLEDGE".equals(ackMode))
            acknowledgeMode = Session.DUPS_OK_ACKNOWLEDGE;
        else if (ackMode != null)
        	log.warn("Unsupported acknowledge mode: %s (using default AUTO_ACKNOWLEDGE)", ackMode);
        
        if ("javax.jms.TextMessage".equals(destinationProperties.get("jms/message-type")))
            textMessages = true;
        
        if (Boolean.TRUE.toString().equals(destinationProperties.get("jms/no-local")))
        	noLocal = true;

        if (Boolean.TRUE.toString().equals(destinationProperties.get("session-selector")))
        	sessionSelector = true;
        
        failoverRetryInterval = destinationProperties.get("jms/failover-retry-interval", Long.TYPE, DEFAULT_FAILOVER_RETRY_INTERVAL);
    	if (failoverRetryInterval <= 0) {
    		log.warn("Illegal failover retry interval: %d (using default %d)", failoverRetryInterval, DEFAULT_FAILOVER_RETRY_INTERVAL);
    		failoverRetryInterval = DEFAULT_FAILOVER_RETRY_INTERVAL;
    	}
        
    	failoverRetryCount = destinationProperties.get("jms/failover-retry-count", Integer.TYPE, DEFAULT_FAILOVER_RETRY_COUNT);
    	if (failoverRetryCount <= 0) {
    		log.warn("Illegal failover retry count: %s (using default %d)", failoverRetryCount, DEFAULT_FAILOVER_RETRY_COUNT);
    		failoverRetryCount = DEFAULT_FAILOVER_RETRY_COUNT;
    	}
        
        reconnectRetryInterval = destinationProperties.get("jms/reconnect-retry-interval", Long.TYPE, DEFAULT_RECONNECT_RETRY_INTERVAL);
    	if (reconnectRetryInterval <= 0) {
    		log.warn("Illegal reconnect retry interval: %d (using default %d)", reconnectRetryInterval, DEFAULT_RECONNECT_RETRY_INTERVAL);
    		reconnectRetryInterval = DEFAULT_RECONNECT_RETRY_INTERVAL;
    	}

        Properties environment = new Properties();
        for (XMap property : destinationProperties.getAll("jms/initial-context-environment/property")) {
        	String name = property.get("name");
        	String value = property.get("value");
        	
        	if ("Context.PROVIDER_URL".equals(name))
        		environment.put(Context.PROVIDER_URL, value);
        	else if ("Context.INITIAL_CONTEXT_FACTORY".equals(name))
        		environment.put(Context.INITIAL_CONTEXT_FACTORY, value);
        	else if ("Context.URL_PKG_PREFIXES".equals(name))
        		environment.put(Context.URL_PKG_PREFIXES, value);
        	else if ("Context.SECURITY_PRINCIPAL".equals(name))
        		environment.put(Context.SECURITY_PRINCIPAL, value);
        	else if ("Context.SECURITY_CREDENTIALS".equals(name))
        		environment.put(Context.SECURITY_CREDENTIALS, value);
        	else
        		log.warn("Unknown InitialContext property: %s (ignored)", name);
        }

        InitialContext initialContext = null;
        try {
        	initialContext = new InitialContext(environment.size() > 0 ? environment : null);
        }
	    catch (NamingException e) {
	     	log.error(e, "Could not initialize JNDI context");
	        throw new ServiceException("Error configuring JMS Adapter", e);
	    }
	    
        String cfJndiName = destinationProperties.get("jms/connection-factory");
        try {
	        jmsConnectionFactory = (ConnectionFactory)initialContext.lookup(cfJndiName);
        }
        catch (NamingException e) {
        	log.error(e, "Could not find JMS ConnectionFactory named %s in JNDI", cfJndiName);
            throw new ServiceException("Error configuring JMS Adapter", e);
        }
        
        String dsJndiName = destinationProperties.get("jms/destination-jndi-name");
        try {
	        jmsDestination = (Destination)initialContext.lookup(dsJndiName);
        }
        catch (NamingException e) {
        	log.error(e, "Could not find JMS destination named %s in JNDI", dsJndiName);
            throw new ServiceException("Error configuring JMS Adapter", e);
        }
    }

    protected javax.jms.Destination getProducerDestination(String topic) {
        return jmsDestination;
    }

    protected javax.jms.Destination getConsumerDestination(String topic) {
        return jmsDestination;
    }

    @Override
    public void start() throws ServiceException {
        super.start();
    }

    @Override
    public void stop() throws ServiceException {
        super.stop();

        for (JMSClient jmsClient : jmsClients.values()) {
        	try {
        		jmsClient.close();
        	}
        	catch (Exception e) {
        		log.warn(e, "Could not close JMSClient: %s", jmsClient);
        	}
        }
        jmsClients.clear();
    }


    private synchronized JMSClient connectJMSClient(Channel client, String destination) throws Exception {
        JMSClient jmsClient = jmsClients.get(client.getId());
        if (jmsClient == null) {
            jmsClient = new JMSClientImpl(client);
            jmsClient.connect();
            jmsClients.put(client.getId(), jmsClient);
            if (sessionSelector && GraniteContext.getCurrentInstance() instanceof ServletGraniteContext)
                ((ServletGraniteContext)GraniteContext.getCurrentInstance()).getSessionMap().put(JMSClient.JMSCLIENT_KEY_PREFIX + destination, jmsClient);
            log.debug("JMS client connected for channel " + client.getId());
        }
        return jmsClient;
    }

    private synchronized void closeJMSClientIfNecessary(Channel channel, String destination) throws Exception {
        JMSClient jmsClient = jmsClients.get(channel.getId());
        if (jmsClient != null && !jmsClient.hasActiveConsumer()) {
            jmsClient.close();
            jmsClients.remove(channel.getId());
            if (sessionSelector && GraniteContext.getCurrentInstance() instanceof ServletGraniteContext)
                ((ServletGraniteContext)GraniteContext.getCurrentInstance()).getSessionMap().remove(JMSClient.JMSCLIENT_KEY_PREFIX + destination);
            log.debug("JMS client closed for channel " + channel.getId());
        }
    }

    @Override
    public Object invoke(Channel fromClient, AsyncMessage message) {
    	String topicId = (String)message.getHeader(AsyncMessage.SUBTOPIC_HEADER);
    	
    	if (getSecurityPolicy().canPublish(fromClient, topicId, message)) {
    		try {
	            JMSClient jmsClient = connectJMSClient(fromClient, message.getDestination());
	            jmsClient.send(message);
	
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
    public Object manage(Channel fromChannel, CommandMessage message) {
    	String topicId = (String)message.getHeader(AsyncMessage.SUBTOPIC_HEADER);

        if (message.getOperation() == CommandMessage.SUBSCRIBE_OPERATION) {
        	if (getSecurityPolicy().canSubscribe(fromChannel, topicId, message)) {
	            try {
	                JMSClient jmsClient = connectJMSClient(fromChannel, message.getDestination());
	                jmsClient.subscribe(message);
	
	                AsyncMessage reply = new AcknowledgeMessage(message);
	                return reply;
	            }
	            catch (Exception e) {
	                throw new RuntimeException("JMSAdapter subscribe error on topic: " + message, e);
	            }
        	}

        	log.debug("Channel %s tried to subscribe to topic %s", fromChannel, topicId);
        	ErrorMessage error = new ErrorMessage(message, null);
            error.setFaultString("Server.Subscribe.Denied");
            return error;
        }
        else if (message.getOperation() == CommandMessage.UNSUBSCRIBE_OPERATION) {
            try {
                JMSClient jmsClient = connectJMSClient(fromChannel, message.getDestination());
                jmsClient.unsubscribe(message);
                closeJMSClientIfNecessary(fromChannel, message.getDestination());

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
    private class JMSClientImpl implements JMSClient {

        private Channel channel = null;
        private String topic = null;
        private javax.jms.Connection jmsConnection = null;
        private javax.jms.Session jmsProducerSession = null;
        private javax.jms.MessageProducer jmsProducer = null;
        private Map<String, JMSConsumer> consumers = new HashMap<String, JMSConsumer>();
        private boolean useGlassFishNoExceptionListenerWorkaround = false;
        private boolean useGlassFishNoCommitWorkaround = false;
        
        private ExceptionListener connectionExceptionListener = new ConnectionExceptionListener();
        
        private class ConnectionExceptionListener implements ExceptionListener {

			public void onException(JMSException ex) {
				// Connection failure, force reconnection of the producer on next send
				jmsProducer = null;
				for (JMSConsumer consumer : consumers.values())
					consumer.reset();
				consumers.clear();
				jmsConnection = null;
				jmsProducerSession = null;
			}
        }


        public JMSClientImpl(Channel channel) {
            this.channel = channel;            
        }

        public boolean hasActiveConsumer() {
            return consumers != null && !consumers.isEmpty();
        }


        public void connect() throws ServiceException {
        	if (jmsConnection != null)
        		return;
        	
            try {
                jmsConnection = jmsConnectionFactory.createConnection();
                if (!useGlassFishNoExceptionListenerWorkaround) {
                	try {
                		jmsConnection.setExceptionListener(connectionExceptionListener);
                	}
                	catch (JMSException e) {
                		if (e.getMessage().startsWith("MQJMSRA_DC2001: Unsupported:setExceptionListener()"))
                			useGlassFishNoExceptionListenerWorkaround = true;
                		else
                			throw e;
                	}
                }
                jmsConnection.start();
                log.debug("JMS client connected for channel " + channel.getId());
            }
            catch (JMSException e) {
                throw new ServiceException("JMS Initialize error", e);
            }
        }

        public void close() throws ServiceException {
            try {
                if (jmsProducer != null)
                    jmsProducer.close();
            }
            catch (JMSException e) {
            	log.error(e, "Could not close JMS Producer for channel " + channel.getId());
            }
            finally {
            	try {
	                if (jmsProducerSession != null)
	                    jmsProducerSession.close();
	            }
	            catch (JMSException e) {
	            	log.error(e, "Could not close JMS Producer Session for channel " + channel.getId());
	            }
            }
            for (JMSConsumer consumer : consumers.values()) {
            	try {
            		consumer.close();
            	}
            	catch (JMSException e) {
            		log.error(e, "Could not close JMS Consumer " + consumer.subscriptionId + " for channel " + channel.getId());
            	}
            }
            try {
                jmsConnection.stop();
            }
            catch (JMSException e) {
            	log.debug(e, "Could not stop JMS Connection for channel " + channel.getId());
            }
            finally {
	        	try {
	        		jmsConnection.close();
	        	}
	            catch (JMSException e) {
	                throw new ServiceException("JMS Stop error", e);
	            }
	        	finally {
		        	consumers.clear();
	        	}
            }
        }
        
        private void createProducer(String topic) throws Exception {
            try {
            	// When failing over, JMS can be in a temporary illegal state. Give it some time to recover. 
            	int retryCount = failoverRetryCount;
            	do {
	                try {
	                	jmsProducer = jmsProducerSession.createProducer(getProducerDestination(topic != null ? topic : this.topic));
	                	if (retryCount < failoverRetryCount)	// We come from a failover, try to recover session
	                		jmsProducerSession.recover();
	                	break;
	                }
	                catch (Exception e) {
	                	if (retryCount <= 0)
	                		throw e;
	                	
	                	if (log.isDebugEnabled())
	                		log.debug(e, "Could not create JMS Producer (retrying %d time)", retryCount);
	                	else
	                		log.info("Could not create JMS Producer (retrying %d time)", retryCount);
	                	
	                	try {
	                		Thread.sleep(failoverRetryInterval);
	                	}
	                	catch (Exception f) {
	                		throw new ServiceException("Could not sleep when retrying to create JMS Producer", f.getMessage(), e);
	                	}
	                }
            	}
                while (retryCount-- > 0);
                
                jmsProducer.setPriority(messagePriority);
                jmsProducer.setDeliveryMode(deliveryMode);
                log.debug("Created JMS Producer for channel %s", channel.getId());
            }
            catch (JMSException e) {
            	jmsProducerSession.close();
            	jmsProducerSession = null;
            	throw e;
            }
        }

        public void send(AsyncMessage message) throws Exception {
            Object msg = null;
            if (Boolean.TRUE.equals(message.getHeader(GravityInternal.BYTEARRAY_BODY_HEADER))) {
            	byte[] byteArray = (byte[])message.getBody();
            	ByteArrayInputStream bais = new ByteArrayInputStream(byteArray);
            	AMF3Deserializer deser = new AMF3Deserializer(bais);
            	msg = deser.readObject();
            	deser.close(); // makes jdk7 happy (Resource leak: 'deser' is never closed)...
            }
            else
            	msg = message.getBody();
            
            internalSend(message.getHeaders(), msg, message.getMessageId(), message.getCorrelationId(), message.getTimestamp(), message.getTimeToLive());
        }

        public void send(Map<String, ?> params, Object msg, long timeToLive) throws Exception {
        	internalSend(params, msg, null, null, new Date().getTime(), timeToLive);
        }
        
        public void internalSend(Map<String, ?> headers, Object msg, String messageId, String correlationId, long timestamp, long timeToLive) throws Exception {
            String topic = (String)headers.get(AsyncMessage.SUBTOPIC_HEADER);
                
            if (jmsProducerSession == null) {
                jmsProducerSession = jmsConnection.createSession(transactedSessions, acknowledgeMode);
                log.debug("Created JMS Producer Session for channel %s (transacted: %s, ack: %s)", channel.getId(), transactedSessions, acknowledgeMode);
            }
            
            if (jmsProducer == null)
            	createProducer(topic);
            
            javax.jms.Message jmsMessage = null;
            if (textMessages)
                jmsMessage = jmsProducerSession.createTextMessage(msg.toString());
            else
                jmsMessage = jmsProducerSession.createObjectMessage((Serializable)msg);

            jmsMessage.setJMSMessageID(normalizeJMSMessageID(messageId));
            jmsMessage.setJMSCorrelationID(normalizeJMSMessageID(correlationId));
            jmsMessage.setJMSTimestamp(timestamp);
            jmsMessage.setJMSExpiration(timeToLive);
            
            for (Map.Entry<String, ?> me : headers.entrySet()) {
                if ("JMSType".equals(me.getKey())) {
                    if (me.getValue() instanceof String)
                        jmsMessage.setJMSType((String)me.getValue());
                }
                else if ("JMSPriority".equals(me.getKey())) {
                    if (me.getValue() instanceof Integer)
                        jmsMessage.setJMSPriority(((Integer)me.getValue()).intValue());
                }
                else if (me.getValue() instanceof String)
                    jmsMessage.setStringProperty(me.getKey(), (String)me.getValue());
                else if (me.getValue() instanceof Boolean)
                    jmsMessage.setBooleanProperty(me.getKey(), ((Boolean)me.getValue()).booleanValue());
                else if (me.getValue() instanceof Integer)
                    jmsMessage.setIntProperty(me.getKey(), ((Integer)me.getValue()).intValue());
                else if (me.getValue() instanceof Long)
                    jmsMessage.setLongProperty(me.getKey(), ((Long)me.getValue()).longValue());
                else if (me.getValue() instanceof Double)
                    jmsMessage.setDoubleProperty(me.getKey(), ((Double)me.getValue()).doubleValue());
                else
                    jmsMessage.setObjectProperty(me.getKey(), me.getValue());
            }

            jmsProducer.send(jmsMessage);
            
            if (transactedSessions && !useGlassFishNoCommitWorkaround) {
            	// If we are in a container-managed transaction (data dispatch from an EJB interceptor for ex.), we should not commit the session
            	// but the behaviour is different between JBoss and GlassFish
            	try {
            		jmsProducerSession.commit();
            	}
            	catch (JMSException e) {
            		if (e.getMessage() != null && e.getMessage().startsWith("MQJMSRA_DS4001"))
                    	useGlassFishNoCommitWorkaround = true;
                    else
            			log.error(e, "Could not commit JMS Session for channel %s", channel.getId());
            	}
            }
        }

		private String normalizeJMSMessageID(String messageId) {
            if (messageId != null && !messageId.startsWith("ID:"))
            	messageId = "ID:" + messageId;
			return messageId;
		}

        public void subscribe(CommandMessage message) throws Exception {
            String subscriptionId = (String)message.getHeader(AsyncMessage.DESTINATION_CLIENT_ID_HEADER);
            String selector = (String)message.getHeader(CommandMessage.SELECTOR_HEADER);
            this.topic = (String)message.getHeader(AsyncMessage.SUBTOPIC_HEADER);

            internalSubscribe(subscriptionId, selector, message.getDestination(), this.topic);
        }
        
        public void subscribe(String selector, String destination, String topic) throws Exception {
        	GraniteConfig graniteConfig = GraniteContext.getCurrentInstance().getGraniteConfig();
        	DistributedDataFactory distributedDataFactory = graniteConfig.getDistributedDataFactory();
    		String subscriptionId = distributedDataFactory.getInstance().getDestinationSubscriptionId(destination);
    		if (subscriptionId != null)
    			internalSubscribe(subscriptionId, selector, destination, topic);
        }
        
        private void internalSubscribe(String subscriptionId, String selector, String destination, String topic) throws Exception {
            synchronized (consumers) {
                JMSConsumer consumer = consumers.get(subscriptionId);
                if (consumer == null) {
                    consumer = new JMSConsumer(subscriptionId, selector, noLocal);
                    consumer.connect(selector);
                    consumers.put(subscriptionId, consumer);
                }
                else
                    consumer.setSelector(selector);
                channel.addSubscription(destination, topic, subscriptionId, false);
            }
        }

        public void unsubscribe(CommandMessage message) throws Exception {
            String subscriptionId = (String)message.getHeader(AsyncMessage.DESTINATION_CLIENT_ID_HEADER);

            synchronized (consumers) {
                JMSConsumer consumer = consumers.get(subscriptionId);
                try {
	                if (consumer != null)
	                    consumer.close();
                }
                finally {
	                consumers.remove(subscriptionId);
	                channel.removeSubscription(subscriptionId);
                }
            }
        }


        private class JMSConsumer implements MessageListener {

            private String subscriptionId = null;
            private javax.jms.Session jmsConsumerSession = null;
            private javax.jms.MessageConsumer jmsConsumer = null;
            private boolean noLocal = false;
            private String selector = null;
            private boolean useJBossTCCLDeserializationWorkaround = false;
            private boolean useGlassFishNoCommitWorkaround = false;
            private boolean reconnected = false;
            private Timer reconnectTimer = null;

            public JMSConsumer(String subscriptionId, String selector, boolean noLocal) throws Exception {
                this.subscriptionId = subscriptionId;
                this.noLocal = noLocal;
                this.selector = selector;
            }
            
            public void connect(String selector) throws Exception {
                if (jmsConsumer != null)
            		return;
            	
            	this.selector = selector;
            	
            	// Reconnect to the JMS provider in case no producer has already done it
            	JMSClientImpl.this.connect();
                if (jmsConsumerSession == null) {
                    jmsConsumerSession = jmsConnection.createSession(transactedSessions, acknowledgeMode);
                    if (reconnected)
                        jmsConsumerSession.recover();
                    log.debug("Created JMS Consumer Session for channel %s (transacted: %s, ack: %s)", channel.getId(), transactedSessions, acknowledgeMode);
                }
                
                if (reconnectTimer != null)
                	reconnectTimer.cancel();
                
                try {	                
                	// When failing over, JMS can be in a temporary illegal state. Give it some time to recover. 
                	int retryCount = failoverRetryCount;
                	do {
		                try {
		                	jmsConsumer = jmsConsumerSession.createConsumer(getConsumerDestination(topic), selector, noLocal);
		                	if (retryCount < failoverRetryCount)	// We come from a failover, try to recover session
		                		reconnected = true;
		                	break;
		                }
		                catch (Exception e) {
		                	if (retryCount <= 0)
		                		throw e;
		                	
		                	if (log.isDebugEnabled())
		                		log.debug(e, "Could not create JMS Consumer (retrying %d time)", retryCount);
		                	else
		                		log.info("Could not create JMS Consumer (retrying %d time)", retryCount);
		                	
		                	try {
		                		Thread.sleep(failoverRetryInterval);
		                	}
		                	catch (Exception f) {
		                		throw new ServiceException("Could not sleep when retrying to create JMS Consumer", f.getMessage(), e);
		                	}
		                }
                	}
	                while (retryCount-- > 0);
	                
	                jmsConsumer.setMessageListener(this);
	                log.debug("Created JMS Consumer for channel %s", channel.getId());
                }
                catch (Exception e) {
                	close();
                	throw e;
                }
            }

            public void setSelector(String selector) throws Exception {
                // To change the selector, just close the consumer but keep the session
                if (jmsConsumer != null) {
                    jmsConsumer.close();
                    jmsConsumer = null;
                }

                connect(selector);
                log.debug("Changed selector to %s for JMS Consumer of channel %s", selector, channel.getId());
            }
            
            public void reset() {
            	jmsConsumer = null;
            	jmsConsumerSession = null;
            	
            	final TimerTask reconnectTask = new TimerTask() {
					@Override
					public void run() {
						try {
							connect(selector);
							reconnectTimer.cancel();
							reconnectTimer = null;
						}
						catch (Exception e) {
							// Wait for next task run
						}
					}
				};
				if (reconnectTimer != null)
					reconnectTimer.cancel();
				
				reconnectTimer = new Timer();
				reconnectTimer.schedule(reconnectTask, failoverRetryInterval, reconnectRetryInterval);
            }

            public void close() throws JMSException {
				if (reconnectTimer != null)
					reconnectTimer.cancel();
				
                try {
	            	if (jmsConsumer != null) {
	                    jmsConsumer.close();
	                    jmsConsumer = null;
	                }
                }
                finally {
	                if (jmsConsumerSession != null) {
	                    jmsConsumerSession.close();
	                    jmsConsumerSession = null;
	                }
                }
            }
            
            public void onMessage(javax.jms.Message message) {
                if (!(message instanceof ObjectMessage) && !(message instanceof TextMessage)) {
                    log.error("JMS Adapter message type not allowed: %s", message.getClass().getName());

                    try {
                        if (acknowledgeMode == Session.CLIENT_ACKNOWLEDGE)
                            message.acknowledge();

                        if (transactedSessions)
                            jmsConsumerSession.commit();
                    }
                    catch (JMSException e) {
                        log.error(e, "Could not ack/commit JMS onMessage");
                    }
                }

                log.debug("Delivering JMS message to channel %s subscription %s", channel.getId(), subscriptionId);
                
                AsyncMessage dmsg = new AsyncMessage();
                try {
                    Serializable msg = null;

                    if (textMessages) {
                        TextMessage jmsMessage = (TextMessage)message;
                        msg = jmsMessage.getText();
                    }
                    else {
                        ObjectMessage jmsMessage = (ObjectMessage)message;
                        if (useJBossTCCLDeserializationWorkaround) {
                        	// On JBoss 6, try to deserialize with application class loader if the previous attempt fails
                            ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
                            try {
	                            Thread.currentThread().setContextClassLoader(JMSServiceAdapter.this.getClass().getClassLoader());
	                            msg = jmsMessage.getObject();
                            }
                            finally {
                            	Thread.currentThread().setContextClassLoader(contextClassLoader);
                            }
                        }
                        try {
                        	msg = jmsMessage.getObject();
                        }
                        catch (JMSException e) {
                        	// On JBoss 6, try to deserialize with application class loader if the previous attempt fails
                            ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
                            try {
	                            Thread.currentThread().setContextClassLoader(JMSServiceAdapter.this.getClass().getClassLoader());
	                            msg = jmsMessage.getObject();
	                            useJBossTCCLDeserializationWorkaround = true;
                            }
                            finally {
                            	Thread.currentThread().setContextClassLoader(contextClassLoader);
                            }
                        }
                    }

                    dmsg.setDestination(getDestination().getId());
                    
                    if (Boolean.TRUE.equals(message.getBooleanProperty(GravityInternal.BYTEARRAY_BODY_HEADER))) {
                        getGravity().initThread(null, channel.getClientType());
                        try {
	                        ByteArrayOutputStream baos = new ByteArrayOutputStream(100);
	                        AMF3Serializer ser = new AMF3Serializer(baos);
	                        ser.writeObject(msg);
	                        ser.close();
	                        baos.close();
	                        dmsg.setBody(baos.toByteArray());
                        }
                        finally {
                        	getGravity().releaseThread();
                        }
                    }
                    else
                    	dmsg.setBody(msg);
                    
                    dmsg.setMessageId(denormalizeJMSMessageID(message.getJMSMessageID()));
                    dmsg.setCorrelationId(denormalizeJMSMessageID(message.getJMSCorrelationID()));
                    dmsg.setTimestamp(message.getJMSTimestamp());
                    dmsg.setTimeToLive(message.getJMSExpiration());

                    Enumeration<?> ename = message.getPropertyNames();
                    while (ename.hasMoreElements()) {
                        String pname = (String)ename.nextElement();
                        dmsg.setHeader(pname, message.getObjectProperty(pname));
                    }
            		
                    dmsg.setHeader("JMSType", message.getJMSType());
                    dmsg.setHeader("JMSPriority", Integer.valueOf(message.getJMSPriority()));
                    dmsg.setHeader("JMSRedelivered", Boolean.valueOf(message.getJMSRedelivered()));
                    dmsg.setHeader("JMSDeliveryMode", Integer.valueOf(message.getJMSDeliveryMode()));
                    dmsg.setHeader(AsyncMessage.DESTINATION_CLIENT_ID_HEADER, subscriptionId);
                    
                    channel.receive(dmsg);
                }
                catch (IOException e) {
                    if (transactedSessions) {
                        try {
                            jmsConsumerSession.rollback();
                        }
                        catch (JMSException f) {
                            log.error("Could not rollback JMS session, messageId: %s", dmsg.getMessageId());
                        }
                    }

                    throw new RuntimeException("IO Error", e);
                }
                catch (JMSException e) {
                    if (transactedSessions) {
                        try {
                            jmsConsumerSession.rollback();
                        }
                        catch (JMSException f) {
                            log.error("Could not rollback JMS session, messageId: %s", dmsg.getMessageId());
                        }
                    }

                    throw new RuntimeException("JMS Error", e);
                }
                catch (MessageReceivingException e) {
                    if (transactedSessions) {
                        try {
                            jmsConsumerSession.rollback();
                        }
                        catch (JMSException f) {
                            log.error("Could not rollback JMS session, messageId: %s", dmsg.getMessageId());
                        }
                    }

                    throw new RuntimeException("Channel delivery Error", e);
                }

                try {
                    if (acknowledgeMode == Session.CLIENT_ACKNOWLEDGE)
                        message.acknowledge();

                    if (transactedSessions && !useGlassFishNoCommitWorkaround)
                        jmsConsumerSession.commit();
                }
                catch (JMSException e) {
                    if (e.getMessage() != null && e.getMessage().startsWith("MQJMSRA_DS4001"))
                    	useGlassFishNoCommitWorkaround = true;
                    else
                        log.error(e, "Could not ack/commit JMS onMessage, messageId: %s", dmsg.getMessageId());

                    // Message already delivered to client, should rollback or not ?
                }
            }

    		private String denormalizeJMSMessageID(String messageId) {
                if (messageId != null && messageId.startsWith("ID:"))
                	messageId = messageId.substring(3);
    			return messageId;
    		}
        }
    }
}
