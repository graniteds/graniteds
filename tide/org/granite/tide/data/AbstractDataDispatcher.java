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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpSession;

import org.granite.context.GraniteContext;
import org.granite.logging.Logger;
import org.granite.messaging.webapp.HttpGraniteContext;

public abstract class AbstractDataDispatcher implements DataDispatcher {
    
    private static final Logger log = Logger.getLogger(AbstractDataDispatcher.class);

    
    protected boolean enabled;
    protected String topicName = null;
    protected DataTopicParams paramsProvider = null;
    protected String sessionId = null;
    protected String clientId = null;
    protected String subscriptionId = null;
    
    
	public AbstractDataDispatcher(String topicName, Class<? extends DataTopicParams> dataTopicParamsClass) {
		this.topicName = topicName;
		
		try {
			paramsProvider = dataTopicParamsClass.newInstance();
		}
		catch (Exception e) {
			log.error("Could not instantiate class " + dataTopicParamsClass, e);
		}
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
		List<DataObserveParams> selectors = (List<DataObserveParams>)session.getAttribute("org.granite.tide.dataSelectors." + topicName);
		if (selectors == null) {
			selectors = new ArrayList<DataObserveParams>();
			session.setAttribute("org.granite.tide.dataSelectors." + topicName, selectors);
		}
		
		String dataSelector = (String)session.getAttribute("org.granite.gravity.selector." + topicName);
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
			
			session.setAttribute("org.granite.gravity.selector." + topicName, sb.toString());
		}
		else if (dataSelector == null) {
			dataSelector = "type = 'UNINITIALIZED'";
			session.setAttribute("org.granite.tide.selector." + topicName, dataSelector);
		}
		
		if (!enabled)
			return;
		
		changeDataSelector(dataSelector);
	}
	
	protected abstract void changeDataSelector(String dataSelector);
	
	
	public void publish(Set<Object[]> dataUpdates) {
		if (!enabled)
			return;
		
		try {
			Map<Map<String, String>, List<Object>> updates = new HashMap<Map<String, String>, List<Object>>();
			if (paramsProvider != null) {
				for (Object[] dataUpdate : dataUpdates) {
					DataPublishParams params = new DataPublishParams();
					paramsProvider.publishes(params, dataUpdate[1]);
					
					Map<String, String> headers = params.getHeaders();
					List<Object> list = updates.get(headers);
					if (list == null) {
						list = new ArrayList<Object>();
						updates.put(headers, list);
					}
					list.add(dataUpdate);
				}
			}
			
			for (Entry<Map<String, String>, List<Object>> me : updates.entrySet()) {
				publishUpdate(me.getKey(), me.getValue());
			}
		}
		catch (Exception e) {
			log.error(e, "Could not publish data update on topic %s", topicName);
		}
	}
	
	protected abstract void publishUpdate(Map<String, String> params, Object body);
}
