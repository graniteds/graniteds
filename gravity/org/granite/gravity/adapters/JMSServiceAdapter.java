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

package org.granite.gravity.adapters;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.granite.gravity.Channel;
import org.granite.gravity.Gravity;
import org.granite.gravity.MessageReceivingException;
import org.granite.logging.Logger;
import org.granite.messaging.amf.io.AMF3Deserializer;
import org.granite.messaging.amf.io.AMF3Serializer;
import org.granite.messaging.service.ServiceException;
import org.granite.util.XMap;

import flex.messaging.messages.AcknowledgeMessage;
import flex.messaging.messages.AsyncMessage;
import flex.messaging.messages.CommandMessage;
import flex.messaging.messages.ErrorMessage;
import flex.messaging.messages.Message;

/**
 * @author William DRAI
 */
public class JMSServiceAdapter extends ServiceAdapter {

    private static final Logger log = Logger.getLogger(JMSServiceAdapter.class);
    
    public static final long DEFAULT_FAILOVER_RETRY_INTERVAL = 1000L;
    public static final int DEFAULT_FAILOVER_RETRY_COUNT = 4;

    protected ConnectionFactory jmsConnectionFactory = null;
    protected javax.jms.Destination jmsDestination = null;
    protected Map<String, JMSClient> jmsClients = new HashMap<String, JMSClient>();
    protected String destinationName = null;
    protected boolean textMessages = false;
    protected boolean transactedSessions = false;
    protected int acknowledgeMode = Session.AUTO_ACKNOWLEDGE;
    protected int messagePriority = javax.jms.Message.DEFAULT_PRIORITY;
    protected int deliveryMode = javax.jms.Message.DEFAULT_DELIVERY_MODE;
    protected boolean noLocal = false;
    
    protected long failoverRetryInterval = DEFAULT_FAILOVER_RETRY_INTERVAL;
    protected int failoverRetryCount = DEFAULT_FAILOVER_RETRY_COUNT;

    @Override
    public void configure(XMap adapterProperties, XMap destinationProperties) throws ServiceException {
        super.configure(adapterProperties, destinationProperties);

        try {
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

            InitialContext ic = new InitialContext(environment.size() > 0 ? environment : null);
            
            String cfJndiName = destinationProperties.get("jms/connection-factory");
            jmsConnectionFactory = (ConnectionFactory)ic.lookup(cfJndiName);

            String dsJndiName = destinationProperties.get("jms/destination-jndi-name");
            jmsDestination = (Destination)ic.lookup(dsJndiName);
        }
        catch (Exception e) {
            throw new ServiceException("Error when configuring JMS Adapter", e);
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

        for (JMSClient jmsClient : jmsClients.values())
            jmsClient.close();
    }


    private synchronized JMSClient createJMSClient(Channel client) throws Exception {
        JMSClient jmsClient = jmsClients.get(client.getId());
        if (jmsClient == null) {
            jmsClient = new JMSClient(client);
            jmsClient.connect();
            jmsClients.put(client.getId(), jmsClient);
        }
        return jmsClient;
    }

    @Override
    public Object invoke(Channel fromClient, AsyncMessage message) {
        try {
            JMSClient jmsClient = createJMSClient(fromClient);
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

    @Override
    public Object manage(Channel fromChannel, CommandMessage message) {
        if (message.getOperation() == CommandMessage.SUBSCRIBE_OPERATION) {
            try {
                JMSClient jmsClient = createJMSClient(fromChannel);
                jmsClient.subscribe(message);
                
                AsyncMessage reply = new AcknowledgeMessage(message);
                return reply;
            }
            catch (Exception e) {
                throw new RuntimeException("JMSAdapter subscribe error on topic: " + message, e);
            }
        }
        else if (message.getOperation() == CommandMessage.UNSUBSCRIBE_OPERATION) {
            try {
                JMSClient jmsClient = createJMSClient(fromChannel);
                jmsClient.unsubscribe(message);

                AsyncMessage reply = new AcknowledgeMessage(message);
                return reply;
            }
            catch (Exception e) {
                throw new RuntimeException("JMSAdapter unsubscribe error on topic: " + message, e);
            }
        }

        return null;
    }


    private class JMSClient {

        private Channel channel = null;
        private String topic = null;
        private javax.jms.Connection jmsConnection = null;
        private javax.jms.Session jmsProducerSession = null;
        private javax.jms.MessageProducer jmsProducer = null;
        private Map<String, JMSConsumer> consumers = new HashMap<String, JMSConsumer>();


        public JMSClient(Channel channel) {
            this.channel = channel;
        }

        public void connect() throws ServiceException {
            try {
                jmsConnection = jmsConnectionFactory.createConnection();
                jmsConnection.start();
            }
            catch (JMSException e) {
                throw new ServiceException("JMS Initialize error", e);
            }
        }

        public void close() throws ServiceException {
            try {
                if (jmsProducer != null)
                    jmsProducer.close();
                if (jmsProducerSession != null)
                    jmsProducerSession.close();
                for (JMSConsumer consumer : consumers.values())
                    consumer.close();
                jmsConnection.stop();
                jmsConnection.close();
            }
            catch (JMSException e) {
                throw new ServiceException("JMS Stop error", e);
            }
        }
        

        public void send(Message message) throws Exception {
            if (jmsProducerSession == null)
                jmsProducerSession = jmsConnection.createSession(transactedSessions, acknowledgeMode);
            if (jmsProducer == null) {
                try {
	                
                	// When failing over, JMS can be in a temporary illegal state. Give it some time to recover. 
                	int retryCount = failoverRetryCount;
                	do {
		                try {
		                	jmsProducer = jmsProducerSession.createProducer(getProducerDestination(topic));
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
                }
                catch (Exception e) {
                	jmsProducerSession.close();
                	jmsProducerSession = null;
                	throw e;
                }
            }
            
            Object msg = null;
            if (Boolean.TRUE.equals(message.getHeader(Gravity.BYTEARRAY_BODY_HEADER))) {
            	byte[] byteArray = (byte[])message.getBody();
            	ByteArrayInputStream bais = new ByteArrayInputStream(byteArray);
            	AMF3Deserializer deser = new AMF3Deserializer(bais);
            	msg = deser.readObject();
            }
            else
            	msg = message.getBody();

            javax.jms.Message jmsMessage = null;
            if (textMessages)
                jmsMessage = jmsProducerSession.createTextMessage(msg.toString());
            else
                jmsMessage = jmsProducerSession.createObjectMessage((Serializable)msg);

            jmsMessage.setJMSMessageID(normalizeJMSMessageID(message.getMessageId()));
            jmsMessage.setJMSCorrelationID(normalizeJMSMessageID(((AsyncMessage)message).getCorrelationId()));
            jmsMessage.setJMSTimestamp(message.getTimestamp());
            jmsMessage.setJMSExpiration(message.getTimeToLive());

            for (Map.Entry<String, Object> me : message.getHeaders().entrySet()) {
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
            if (transactedSessions)
                jmsProducerSession.commit();
        }

		private String normalizeJMSMessageID(String messageId) {
            if (messageId != null && !messageId.startsWith("ID:"))
            	messageId = "ID:" + messageId;
			return messageId;
		}

        public void subscribe(Message message) throws Exception {
            String subscriptionId = (String)message.getHeader(AsyncMessage.DESTINATION_CLIENT_ID_HEADER);
            String selector = (String)message.getHeader(CommandMessage.SELECTOR_HEADER);
            this.topic = (String)message.getHeader(AsyncMessage.SUBTOPIC_HEADER);

            synchronized (consumers) {
                JMSConsumer consumer = consumers.get(subscriptionId);
                if (consumer == null) {
                    consumer = new JMSConsumer(subscriptionId, selector, noLocal);
                    consumers.put(subscriptionId, consumer);
                }
                else
                    consumer.setSelector(selector);
                channel.addSubscription(message.getDestination(), topic, subscriptionId, false);
            }
        }

        public void unsubscribe(Message message) throws Exception {
            String subscriptionId = (String)message.getHeader(AsyncMessage.DESTINATION_CLIENT_ID_HEADER);

            synchronized (consumers) {
                JMSConsumer consumer = consumers.get(subscriptionId);
                if (consumer != null)
                    consumer.close();
                consumers.remove(subscriptionId);
                channel.removeSubscription(subscriptionId);
            }
        }


        private class JMSConsumer implements MessageListener {

            private String subscriptionId = null;
            private javax.jms.Session jmsConsumerSession = null;
            private javax.jms.MessageConsumer jmsConsumer = null;
            private boolean noLocal = false;

            public JMSConsumer(String subscriptionId, String selector, boolean noLocal) throws Exception {
                this.subscriptionId = subscriptionId;
                this.noLocal = noLocal;
                
                jmsConsumerSession = jmsConnection.createSession(transactedSessions, acknowledgeMode);
                try {
	                
                	// When failing over, JMS can be in a temporary illegal state. Give it some time to recover. 
                	int retryCount = failoverRetryCount;
                	do {
		                try {
		                	jmsConsumer = jmsConsumerSession.createConsumer(getConsumerDestination(topic), selector, noLocal);
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
                }
                catch (Exception e) {
                	close();
                	throw e;
                }
            }

            public void setSelector(String selector) throws JMSException {
                if (jmsConsumer != null) {
                    jmsConsumer.close();
                    jmsConsumer = null;
                }
                jmsConsumer = jmsConsumerSession.createConsumer(getConsumerDestination(topic), selector, noLocal);
                jmsConsumer.setMessageListener(this);
            }

            public void close() throws JMSException {
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

                log.debug("Delivering JMS message");

                AsyncMessage dmsg = new AsyncMessage();
                try {
                    Serializable msg = null;

                    if (textMessages) {
                        TextMessage jmsMessage = (TextMessage)message;
                        msg = jmsMessage.getText();
                    }
                    else {
                        ObjectMessage jmsMessage = (ObjectMessage)message;
                        msg = jmsMessage.getObject();
                    }

                    dmsg.setDestination(getDestination().getId());
                    
                    if (Boolean.TRUE.equals(message.getBooleanProperty(Gravity.BYTEARRAY_BODY_HEADER))) {
                        getGravity().initThread();
                        try {
	                        ByteArrayOutputStream baos = new ByteArrayOutputStream(100);
	                        AMF3Serializer ser = new AMF3Serializer(baos, false);
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

                    if (transactedSessions)
                        jmsConsumerSession.commit();
                }
                catch (JMSException e) {
                    log.error("Could not ack/commit JMS onMessage, messageId: %s", dmsg.getMessageId());

                    // Message already delivered, should rollback or not ?
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
