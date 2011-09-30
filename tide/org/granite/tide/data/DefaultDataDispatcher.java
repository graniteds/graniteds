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

package org.granite.tide.data;

import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpSession;

import org.granite.clustering.GraniteDistributedData;
import org.granite.clustering.GraniteDistributedDataFactory;
import org.granite.context.GraniteContext;
import org.granite.gravity.Channel;
import org.granite.gravity.Gravity;
import org.granite.gravity.GravityManager;
import org.granite.logging.Logger;
import org.granite.messaging.webapp.HttpGraniteContext;

import flex.messaging.messages.AsyncMessage;
import flex.messaging.messages.CommandMessage;
import flex.messaging.messages.ErrorMessage;
import flex.messaging.messages.Message;


/**
 *  Default implementation for data update dispatchers using the Gravity API to dispatch updates.
 * 
 *  @see DataDispatcher
 *  @see DataContext
 * 
 *  @author William Drai
 */
public class DefaultDataDispatcher extends AbstractDataDispatcher {
    
    private static final Logger log = Logger.getLogger(DefaultDataDispatcher.class);

    
    private Gravity gravity = null;
    private String clientId = null;
    private String subscriptionId = null;
    
    
	public DefaultDataDispatcher(Gravity gravity, String topicName, Class<? extends DataTopicParams> dataTopicParamsClass) {
		super(topicName, dataTopicParamsClass);
		
		GraniteContext graniteContext = GraniteContext.getCurrentInstance();
		if (gravity == null && graniteContext == null)
			return;
		
		if (graniteContext instanceof HttpGraniteContext) {
			this.gravity = GravityManager.getGravity(((HttpGraniteContext)graniteContext).getServletContext());
			
			HttpSession session = ((HttpGraniteContext)graniteContext).getSession(false);
			if (this.gravity == null || session == null) {
				log.debug("Gravity not found or HTTP session not found, data dispatch disabled");
				return;
			}
			sessionId = session.getId();
			GraniteDistributedData gdd = GraniteDistributedDataFactory.getInstance();
			
			clientId = gdd.getDestinationClientId(topicName);
			if (clientId == null) {
				log.debug("Gravity channel clientId not defined, data dispatch disabled");
				return;
			}
			subscriptionId = gdd.getDestinationSubscriptionId(topicName);
			if (subscriptionId == null) {
				log.debug("Gravity channel subscriptionId not defined, data dispatch disabled");
				return;
			}
		}
		else {
			if (gravity == null) {
				log.debug("Gravity not defined, data dispatch disabled");
				return;
			}
			
			this.gravity = gravity;
			this.sessionId = DataDispatcher.SERVER_DISPATCHER_GDS_SESSION_ID;
		}
		
		enabled = true;
	}
	
	
	@Override
	protected void changeDataSelector(String dataSelector) {
		HttpSession session = ((HttpGraniteContext)GraniteContext.getCurrentInstance()).getSession(false);
		
		if (session != null) {
			GraniteDistributedData gdd = GraniteDistributedDataFactory.getInstance();
			String clientId = gdd.getDestinationClientId(topicName);
			String subscriptionId = gdd.getDestinationSubscriptionId(topicName);
			
			if (clientId != null) {
				CommandMessage message = new CommandMessage();
				message.setClientId(clientId);
				message.setHeader(AsyncMessage.DESTINATION_CLIENT_ID_HEADER, subscriptionId);
				message.setHeader(AsyncMessage.SUBTOPIC_HEADER, TIDE_DATA_SUBTOPIC);
				message.setDestination(topicName);
				message.setOperation(CommandMessage.SUBSCRIBE_OPERATION);
				
				message.setHeader(CommandMessage.SELECTOR_HEADER, dataSelector);
				
				gravity.handleMessage(message, true);
				
				log.debug("Topic %s data selector changed: %s", topicName, dataSelector);
			}
		}
	}
	
	@Override
	public void publishUpdate(Map<String, String> params, Object body) {
		Channel channel = gravity.getChannel(clientId);
		AsyncMessage message = new AsyncMessage();
		message.setClientId(clientId);
		message.setDestination(topicName);
		for (Entry<String, String> hh : params.entrySet())
			message.setHeader(hh.getKey(), hh.getValue());
		message.setBody(body);
		
		Message resultMessage = gravity.publishMessage(channel, message);
		if (resultMessage instanceof ErrorMessage)
			log.error("Could not dispatch data update on topic %s, message %s", topicName, resultMessage.toString());
		else
			log.debug("Data message dispatched on topic %s", topicName);
	}
}
