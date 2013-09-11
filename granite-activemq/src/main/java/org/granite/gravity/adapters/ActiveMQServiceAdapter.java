/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of Granite Data Services.
 *
 *   Granite Data Services is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU Library General Public License as published by
 *   the Free Software Foundation; either version 2 of the License, or (at your
 *   option) any later version.
 *
 *   Granite Data Services is distributed in the hope that it will be useful, but
 *   WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *   FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
 *   for more details.
 *
 *   You should have received a copy of the GNU Library General Public License
 *   along with this library; if not, see <http://www.gnu.org/licenses/>.
 */

package org.granite.gravity.adapters;

import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTopic;
import org.granite.logging.Logger;
import org.granite.messaging.service.ServiceException;
import org.granite.util.XMap;

/**
 * @author William DRAI
 */
public class ActiveMQServiceAdapter extends JMSServiceAdapter {

    private static final Logger log = Logger.getLogger(ActiveMQServiceAdapter.class);

    @Override
    public void configure(XMap adapterProperties, XMap destinationProperties) throws ServiceException {
        try {
            destinationName = destinationProperties.get("jms/destination-name");
            if (Boolean.TRUE.toString().equals(destinationProperties.get("jms/transacted-sessions")))
                transactedSessions = true;
            if ("AUTO_ACKNOWLEDGE".equals(destinationProperties.get("jms/acknowledge-mode")))
                acknowledgeMode = Session.AUTO_ACKNOWLEDGE;
            else if ("CLIENT_ACKNOWLEDGE".equals(destinationProperties.get("jms/acknowledge-mode")))
                acknowledgeMode = Session.CLIENT_ACKNOWLEDGE;
            else if ("DUPS_OK_ACKNOWLEDGE".equals(destinationProperties.get("jms/acknowledge-mode")))
                acknowledgeMode = Session.DUPS_OK_ACKNOWLEDGE;
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

            StringBuilder sb = null;
            if (destinationProperties.get("server/broker-url") != null && !"".equals(destinationProperties.get("server/broker-url").trim())) {
            	sb = new StringBuilder(destinationProperties.get("server/broker-url"));
            }
            else {
	            sb = new StringBuilder("vm://");
	            sb.append(getId());
	            if (Boolean.FALSE.toString().equals(destinationProperties.get("server/create-broker"))) {
	                sb.append("?create=false");
	                String startupWait = destinationProperties.get("server/wait-for-start");
	                if (startupWait != null)
	                    sb.append("&waitForStart=" + startupWait);
	            } 
	            else
	                sb.append("?create=true");
	            
	            if (Boolean.TRUE.toString().equals(destinationProperties.get("server/durable"))) {
	                sb.append("&broker.persistent=true");
	                if (destinationProperties.containsKey("server/file-store-root"))
	                    sb.append("&broker.dataDirectory=").append(destinationProperties.get("server/file-store-root"));
	            }
	            else
	                sb.append("&broker.persistent=false");
            }

            String brokerURL = sb.toString();
            if (destinationProperties.get("server/username") != null && !"".equals(destinationProperties.get("server/username").trim()) 
            		&& destinationProperties.get("server/password") != null && !"".equals(destinationProperties.get("server/password").trim())) {
            	String username = destinationProperties.get("server/username");
            	String password = destinationProperties.get("server/password"); 
                jmsConnectionFactory = new ActiveMQConnectionFactory(username, password, brokerURL);
            }
            else
            	jmsConnectionFactory = new ActiveMQConnectionFactory(brokerURL);
        }
        catch (Exception e) {
            throw new ServiceException("Error when configuring JMS Adapter", e);
        }
    }

    @Override
    protected javax.jms.Destination getProducerDestination(String topic) {
        return new ActiveMQTopic(topic != null ? destinationName + "." + topic.replaceAll("\\*\\*", ">") : destinationName);
    }

    @Override
    protected javax.jms.Destination getConsumerDestination(String topic) {
        return new ActiveMQTopic(topic != null ? destinationName + "." + topic.replaceAll("\\*\\*", ">") : destinationName);
    }
}
