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

import java.util.Map;
import java.util.Map.Entry;

import org.granite.clustering.DistributedData;
import org.granite.clustering.DistributedDataFactory;
import org.granite.config.GraniteConfig;
import org.granite.context.GraniteContext;
import org.granite.gravity.Channel;
import org.granite.gravity.Gravity;
import org.granite.gravity.GravityManager;
import org.granite.logging.Logger;
import org.granite.messaging.webapp.ServletGraniteContext;

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
    
    
	public DefaultDataDispatcher(Gravity gravity, String topicName, Class<? extends DataTopicParams> dataTopicParamsClass) {
		super(topicName, dataTopicParamsClass);
		
		GraniteContext graniteContext = GraniteContext.getCurrentInstance();
		if (gravity == null && (graniteContext == null || !(graniteContext instanceof ServletGraniteContext)))
			return;

		DistributedDataFactory distributedDataFactory = gravity != null
                ? gravity.getGraniteConfig().getDistributedDataFactory()
                : ((GraniteConfig)graniteContext.getGraniteConfig()).getDistributedDataFactory();
		DistributedData gdd = distributedDataFactory.getInstance();
		if (gdd != null) {
			this.gravity = GravityManager.getGravity(((ServletGraniteContext)graniteContext).getServletContext());
			
			if (this.gravity == null) {
				log.debug("Gravity not found or HTTP session not found, data dispatch disabled");
				return;
			}
			
			clientId = gdd.getDestinationClientId(topicName);
			subscriptionId = gdd.getDestinationSubscriptionId(topicName);
			sessionId = graniteContext.getSessionId();
		}
		else {
            if (gravity == null)
                gravity = GravityManager.getGravity(((ServletGraniteContext)graniteContext).getServletContext());

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
		DistributedDataFactory distributedDataFactory = ((GraniteConfig)GraniteContext.getCurrentInstance().getGraniteConfig()).getDistributedDataFactory();		
		DistributedData gdd = distributedDataFactory.getInstance();
		if (gdd != null) {
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
		AsyncMessage message = new AsyncMessage();
		message.setDestination(topicName);
		for (Entry<String, String> hh : params.entrySet())
			message.setHeader(hh.getKey(), hh.getValue());
		message.setBody(body);
		
		Message resultMessage = null;
		if (clientId != null) {
			Channel channel = gravity.findConnectedChannelByClientId(clientId);
			message.setClientId(clientId);
			resultMessage = gravity.publishMessage(channel, message);
		}
		else
			resultMessage = gravity.publishMessage(message);
		
		if (resultMessage instanceof ErrorMessage)
			log.error("Could not dispatch data update on topic %s, message %s", topicName, resultMessage.toString());
		else
			log.debug("Data message dispatched on topic %s", topicName);
	}
}
