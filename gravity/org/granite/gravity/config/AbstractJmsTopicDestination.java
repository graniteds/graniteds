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

package org.granite.gravity.config;

import org.granite.config.flex.Adapter;
import org.granite.config.flex.Destination;
import org.granite.util.XMap;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Scope;


@Scope(ScopeType.APPLICATION)
public class AbstractJmsTopicDestination extends AbstractMessagingDestination {

    ///////////////////////////////////////////////////////////////////////////
    // Instance fields.
   
    private String name = null;
    private String connectionFactoryJndiName = null;
    private String destinationJndiName = null;
    private String acknowledgeMode = null;
    private boolean textMessages = false;
    private boolean transactedSessions = false;
    
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getConnectionFactory() {
		return connectionFactoryJndiName;
	}

	public void setConnectionFactory(String connectionFactoryJndiName) {
		this.connectionFactoryJndiName = connectionFactoryJndiName;
	}

	public String getJndiName() {
		return destinationJndiName;
	}

	public void setJndiName(String jndiName) {
		this.destinationJndiName = jndiName;
	}

	public String getDestinationJndiName() {
		return destinationJndiName;
	}

	public void setDestinationJndiName(String jndiName) {
		this.destinationJndiName = jndiName;
	}

	public String getAcknowledgeMode() {
		return acknowledgeMode;
	}

	public void setAcknowledgeMode(String acknowledgeMode) {
		this.acknowledgeMode = acknowledgeMode;
	}
	
	public boolean isTextMessages() {
		return textMessages;
	}
	
	public void setTextMessages(boolean textMessages) {
		this.textMessages = textMessages;
	}

	public boolean isTransactedSessions() {
		return transactedSessions;
	}

	public void setTransactedSessions(boolean transactedSessions) {
		this.transactedSessions = transactedSessions;
	}

	
	@Override
	protected Adapter buildAdapter() {
		return new Adapter("jms-adapter", "org.granite.gravity.adapters.JMSServiceAdapter", new XMap());
	}
	
	@Override
	protected Destination buildDestination(Adapter adapter) {
		Destination destination = super.buildDestination(adapter);
		destination.getProperties().put("jms", null);
    	destination.getProperties().put("jms/destination-type", "Topic");
    	destination.getProperties().put("jms/destination-name", name);
    	destination.getProperties().put("jms/destination-jndi-name", destinationJndiName);
    	destination.getProperties().put("jms/connection-factory", connectionFactoryJndiName);
    	if (textMessages)
    		destination.getProperties().put("jms/message-type", "javax.jms.TextMessage");
    	destination.getProperties().put("jms/acknowledge-mode", acknowledgeMode);
    	destination.getProperties().put("jms/transacted-sessions", String.valueOf(transactedSessions));
    	destination.getProperties().put("jms/no-local", String.valueOf(isNoLocal()));
    	return destination;
	}
}
