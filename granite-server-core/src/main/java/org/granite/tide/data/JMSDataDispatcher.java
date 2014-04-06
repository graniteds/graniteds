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
package org.granite.tide.data;

import java.io.Serializable;
import java.util.Map;
import java.util.Map.Entry;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnectionFactory;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpSession;

import org.granite.context.GraniteContext;
import org.granite.gravity.adapters.JMSClient;
import org.granite.logging.Logger;
import org.granite.messaging.webapp.ServletGraniteContext;

/**
 *  Implementation for data update dispatchers using JMS to dispatch updates.
 * 
 *  @see DataDispatcher
 *  @see DataContext
 * 
 *  @author William Drai
 */
public class JMSDataDispatcher extends AbstractDataDispatcher {
    
    private static final Logger log = Logger.getLogger(JMSDataDispatcher.class);

    
    private boolean transacted = false;
    private TopicConnectionFactory connectionFactory = null;
    private Topic topic;
    private JMSClient jmsClient = null;
    
    
	public JMSDataDispatcher(String topicName, boolean transacted, Class<? extends DataTopicParams> dataTopicParamsClass) {
		super(topicName, dataTopicParamsClass);
		
		this.transacted = transacted;
		
		GraniteContext graniteContext = GraniteContext.getCurrentInstance();
		if (graniteContext instanceof ServletGraniteContext) {
			HttpSession session = ((ServletGraniteContext)graniteContext).getSession(false);
			if (session == null) {
				log.debug("Gravity not found or HTTP session not found, data dispatch disabled");
				return;
			}
			sessionId = session.getId();
			
			jmsClient = (JMSClient)((ServletGraniteContext)graniteContext).getSessionMap().get(JMSClient.JMSCLIENT_KEY_PREFIX + topicName);
		}
		else {
			// Server initiated dispatcher
			this.sessionId = SERVER_DISPATCHER_GDS_SESSION_ID;
			
			try {
				InitialContext ic = new InitialContext();
				this.connectionFactory = (TopicConnectionFactory)ic.lookup("java:comp/env/tide/ConnectionFactory");
				this.topic = (Topic)ic.lookup("java:comp/env/tide/topic/" + topicName);
			}
			catch (NamingException e) {
				log.warn(e, "Could not retrieve ConnectionFactory and Topic in JNDI for topic %s", topicName);
				return;
			}
		}
		
		enabled = true;
	}
	
	
	@Override
	protected void changeDataSelector(String dataSelector) {
		if (jmsClient != null) {
			try {
				jmsClient.subscribe(dataSelector, topicName, TIDE_DATA_SUBTOPIC);			
				log.debug("JMS Topic %s data selector changed: %s", topicName, dataSelector);
			}
			catch (Exception e) {
				log.error(e, "Could not change JMS Topic %s data selector: %s", topicName);
			}
		}
	}
	
	@Override
	public void publishUpdate(Map<String, String> params, Object body) {
		if (jmsClient != null) {
			try {
				jmsClient.send(params, body, 0L);
			}
			catch (Exception e) {
				log.error("Could not dispatch data update on topic %s using internal JMS client, message %s", topicName, body.toString());
			}
		}
		else if (enabled) {
			try {
				Connection jmsConnection = connectionFactory.createConnection();
				Session jmsSession = jmsConnection.createSession(transacted, Session.AUTO_ACKNOWLEDGE);
				MessageProducer jmsProducer = jmsSession.createProducer(topic);
				ObjectMessage jmsMessage = jmsSession.createObjectMessage((Serializable)body);
				for (Entry<String, String> hh : params.entrySet())
					jmsMessage.setStringProperty(hh.getKey(), hh.getValue());
			
				jmsProducer.send(jmsMessage);
				log.debug("Data message dispatched on JMS topic %s", topicName);
			}
			catch (JMSException e) {
				log.error("Could not dispatch data update on topic %s, message %s", topicName, body.toString());
			}
		}
	}
}
