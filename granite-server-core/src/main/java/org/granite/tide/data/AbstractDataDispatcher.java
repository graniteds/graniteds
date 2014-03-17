/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.granite.clustering.DistributedData;
import org.granite.context.GraniteContext;
import org.granite.logging.Logger;

import flex.messaging.messages.AsyncMessage;


/**
 *  Base implementation for data update dispatchers.
 *  It should be built at beginning of each request during initialization of <code>DataContext</code>.
 *  The dispatch is a three step process :
 * 
 * 	<ul>
 *  <li>Initialization in the constructor</li>
 *  <li><code>observe()</code> builds the server selector depending on the data that are processed</li>
 *  <li><code>publish()</code> handles the actual publishing</li>
 *  </ul>
 *  
 *  Actual implementations should only override <code>changeDataSelector</code> and <code>publishUpdate</code>/
 * 
 *  @see DataDispatcher
 *  @see DataContext
 * 
 *  @author William Drai
 */
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
		if (graniteContext == null)
			return;
		
		DistributedData gdd = graniteContext.getGraniteConfig().getDistributedDataFactory().getInstance();
		if (gdd == null)
			return;	// Session expired
		
		List<DataObserveParams> selectors = DataObserveParams.fromSerializableForm(gdd.getDestinationDataSelectors(topicName));
		List<DataObserveParams> newSelectors = new ArrayList<DataObserveParams>(selectors);
		
		boolean dataSelectorChanged = false;
		String dataSelector = gdd.getDestinationSelector(topicName);
		if (params != null) {
			String newDataSelector = params.updateDataSelector(dataSelector, newSelectors);
			dataSelectorChanged = !newDataSelector.equals(dataSelector);
			if (dataSelectorChanged) {
				log.debug("Data selector changed: %s", newDataSelector);
				gdd.setDestinationSelector(topicName, newDataSelector);
				dataSelector = newDataSelector;
			}
		}
		
		if (!DataObserveParams.containsSame(selectors, newSelectors)) {
			log.debug("Selectors changed: %s", newSelectors);
			gdd.setDestinationDataSelectors(topicName, DataObserveParams.toSerializableForm(newSelectors));
		}
		
		if (!enabled)
			return;
		
		if (dataSelectorChanged)
			changeDataSelector(dataSelector);
	}
	
	protected abstract void changeDataSelector(String dataSelector);
	
	
	public void publish(Object[][] dataUpdates) {
		if (!enabled)
			return;
		
		try {
			Map<Map<String, Object>, List<Object>> updates = new HashMap<Map<String, Object>, List<Object>>();
			if (paramsProvider != null) {
				for (Object[] dataUpdate : dataUpdates) {
					DataPublishParams params = new DataPublishParams();
					paramsProvider.publishes(params, dataUpdate[1]);
					
					Map<String, Object> headers = params.getHeaders();
					List<Object> list = updates.get(headers);
					if (list == null) {
						list = new ArrayList<Object>();
						updates.put(headers, list);
					}
					list.add(dataUpdate);
				}
			}
			
			for (Entry<Map<String, Object>, List<Object>> me : updates.entrySet()) {
				Map<String, Object> headers = new HashMap<String, Object>(me.getKey());
				headers.put(AsyncMessage.SUBTOPIC_HEADER, TIDE_DATA_SUBTOPIC);
				headers.put(GDS_SESSION_ID, sessionId);
				headers.put(TIDE_DATA_TYPE_KEY, TIDE_DATA_TYPE_VALUE);
				publishUpdate(headers, me.getValue().toArray());
			}
		}
		catch (Exception e) {
			log.error(e, "Could not publish data update on topic %s", topicName);
		}
	}
	
	protected abstract void publishUpdate(Map<String, Object> params, Object body);
}
