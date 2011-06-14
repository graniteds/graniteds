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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpSession;

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

public class DataDispatcher {
    
    private static final Logger log = Logger.getLogger(DataDispatcher.class);

    
    private boolean enabled = false;
    private Gravity gravity = null;
    private String topic = null;
    private DataTopicParams paramsProvider = null;
    private String sessionId = null;
    private String clientId = null;
    private String subscriptionId = null;
    
    
	public DataDispatcher(Gravity gravity, String topic, Class<? extends DataTopicParams> dataTopicParamsClass) {
		GraniteContext graniteContext = GraniteContext.getCurrentInstance();
		if (graniteContext == null)
			return;
		
		this.topic = topic;
		try {
			paramsProvider = dataTopicParamsClass.newInstance();
		}
		catch (Exception e) {
			log.error("Could not instantiate class " + dataTopicParamsClass, e);
		}
		
		if (graniteContext instanceof HttpGraniteContext) {
			this.gravity = GravityManager.getGravity(((HttpGraniteContext)graniteContext).getServletContext());
			
			HttpSession session = ((HttpGraniteContext)graniteContext).getSession(false);
			if (this.gravity == null || session == null) {
				log.debug("Gravity not found or HTTP session not found, data dispatch disabled");
				return;
			}
			sessionId = session.getId();
			
			clientId = (String)session.getAttribute("org.granite.gravity.channel.clientId." + topic);
			if (clientId == null) {
				log.debug("Gravity channel clientId not defined, data dispatch disabled");
				return;
			}
			subscriptionId = (String)session.getAttribute("org.granite.gravity.channel.subscriptionId." + topic);
			if (subscriptionId == null) {
				log.debug("Gravity channel subscriptionId not defined, data dispatch disabled");
				return;
			}
		}
		else {
			if (gravity == null)
				log.debug("Gravity not defined, data dispatch disabled");
			
			this.gravity = gravity;
		}
		
		enabled = true;
	}
	
	
	public void observe() {
		// Prepare the selector even if we are not yet subscribed
		DataObserveParams params = null;
		if (paramsProvider != null) {
			// Collect selector parameters from component
			params = new DataObserveParams();
			paramsProvider.observes(params);
		}
		
		// Ensure that the current Gravity consumer listens about this data topic and params
		GraniteContext graniteContext = GraniteContext.getCurrentInstance();
		if (!(graniteContext instanceof HttpGraniteContext))
			return;
		
		HttpSession session = ((HttpGraniteContext)graniteContext).getSession(false);
		
		@SuppressWarnings("unchecked")
		List<DataObserveParams> selectors = (List<DataObserveParams>)session.getAttribute("org.granite.tide.dataSelectors." + topic);
		if (selectors == null) {
			selectors = new ArrayList<DataObserveParams>();
			session.setAttribute("org.granite.tide.dataSelectors." + topic, selectors);
		}
		
		String dataSelector = (String)session.getAttribute("org.granite.gravity.selector." + topic);
		if (params != null && !DataObserveParams.containsParams(selectors, params)) {
			StringBuilder sb = new StringBuilder("type = 'DATA'");
			
			if (!params.isEmpty())
				selectors.add(params);
			
			if (!selectors.isEmpty()) {
				sb.append(" AND (");
				boolean first = true;
				for (DataObserveParams selector : selectors) {
					if (first)
						first = false;
					else
						sb.append(" OR ");
					sb.append("(");
					selector.append(sb);
					sb.append(")");
				}
				sb.append(")");
			}
			
			session.setAttribute("org.granite.gravity.selector." + topic, sb.toString());
		}
		else if (dataSelector == null) {
			dataSelector = "type = 'UNINITIALIZED'";
			session.setAttribute("org.granite.tide.selector." + topic, dataSelector);
		}
		
		if (!enabled)
			return;
		
		String clientId = (String)session.getAttribute("org.granite.gravity.channel.clientId." + topic);	    				
		if (clientId != null) {
			String subscriptionId = (String)session.getAttribute("org.granite.gravity.channel.subscriptionId." + topic);
			
			CommandMessage message = new CommandMessage();
			message.setClientId(clientId);
			message.setHeader(AsyncMessage.DESTINATION_CLIENT_ID_HEADER, subscriptionId);
			message.setHeader(AsyncMessage.SUBTOPIC_HEADER, "tideDataTopic");
			message.setDestination(topic);
			message.setOperation(CommandMessage.SUBSCRIBE_OPERATION);
			
			message.setHeader(CommandMessage.SELECTOR_HEADER, dataSelector);
			
			gravity.handleMessage(message, true);
			
			log.debug("Topic %s data selector changed: %s", topic, dataSelector);
		}
	}
	
	
	public void publish(Set<Object[]> dataUpdates) {
		if (!enabled)
			return;
		
		try {
			AsyncMessage message = new AsyncMessage();
			message.setClientId(clientId);
			message.setHeader(AsyncMessage.SUBTOPIC_HEADER, "tideDataTopic");
			message.setDestination(topic);
			message.setHeader("GDSSessionID", sessionId);
			message.setHeader("type", "DATA");
			if (paramsProvider != null) {
				DataPublishParams params = new DataPublishParams();
				for (Object[] dataUpdate : dataUpdates)
					paramsProvider.publishes(params, dataUpdate[1]);
				
				params.setHeaders(message);
			}
			message.setBody(dataUpdates.toArray());
			
			Channel channel = gravity.getChannel(clientId);
			
			Message resultMessage = gravity.publishMessage(channel, message);
			if (resultMessage instanceof ErrorMessage)
				log.error("Could not dispatch data update on topic %s, message %s", topic, resultMessage.toString());
			else
				log.debug("Data message dispatched on topic %s", topic);
		}
		catch (Exception e) {
			log.error(e, "Could not dispatch data update on topic %s", topic);
		}
	}
}
