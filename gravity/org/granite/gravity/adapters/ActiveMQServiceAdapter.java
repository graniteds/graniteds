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

import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTopic;
import org.granite.messaging.service.ServiceException;
import org.granite.util.XMap;

/**
 * @author William DRAI
 */
public class ActiveMQServiceAdapter extends JMSServiceAdapter {

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
            
            jmsConnectionFactory = new ActiveMQConnectionFactory(sb.toString());
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
