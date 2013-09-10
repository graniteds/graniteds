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
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.granite.gravity.Gravity;
import org.granite.logging.Logger;
import org.granite.tide.data.DataEnabled.PublishMode;


/**
 * @author William DRAI
 */
public class DataContext {
    
	private static final Logger log = Logger.getLogger(DataContext.class);
	
    private static ThreadLocal<DataContext> dataContext = new ThreadLocal<DataContext>();
    
    private static DataContext NULL_DATA_CONTEXT = new NullDataContext(); 
    
    private DataDispatcher dataDispatcher = null;
    private PublishMode publishMode = null;
    private Object[][] updates = null;
    private DataUpdatePostprocessor dataUpdatePostprocessor = null;
    private Map<Object, Object> entityExtraDataMap = new IdentityHashMap<Object, Object>();
    
    
    public static void init() {
    	if (dataContext.get() == null)
    		dataContext.set(NULL_DATA_CONTEXT);
    }
    
    public static void init(String topic, Class<? extends DataTopicParams> dataTopicParamsClass, PublishMode publishMode) {
    	DataContext dc = new DataContext(null, topic, dataTopicParamsClass, publishMode);
    	dataContext.set(dc);
    }
    
    public static void init(Gravity gravity, String topic, Class<? extends DataTopicParams> dataTopicParamsClass, PublishMode publishMode) {
    	DataContext dc = new DataContext(gravity, topic, dataTopicParamsClass, publishMode);
    	dataContext.set(dc);
    }
    
    public static void init(DataDispatcher dataDispatcher, PublishMode publishMode) {
		DataContext dc = new DataContext(dataDispatcher, publishMode);
		dataContext.set(dc);
    }
    
    private DataContext(Gravity gravity, String topic, Class<? extends DataTopicParams> dataTopicParamsClass, PublishMode publishMode) {
		log.debug("Init Gravity data context for topic %s and mode %s", topic, publishMode);
		this.dataDispatcher = new DefaultDataDispatcher(gravity, topic, dataTopicParamsClass);
		this.publishMode = publishMode;
    }

    private DataContext(DataDispatcher dataDispatcher, PublishMode publishMode) {
		log.debug("Init data context with custom dispatcher %s and mode %s", dataDispatcher, publishMode);
		this.dataDispatcher = dataDispatcher;
		this.publishMode = publishMode;
    }

    public static DataContext get() {
        return dataContext.get();
    }
    
    public static void remove() {
		log.debug("Remove data context");
    	dataContext.remove();
    }
    
    public static boolean isNull() {
    	return dataContext.get() == NULL_DATA_CONTEXT;
    }
    
    private final List<EntityUpdate> dataUpdates = new ArrayList<EntityUpdate>();
    private boolean published = false;

    
    public List<EntityUpdate> getDataUpdates() {
        return dataUpdates;
    }
    
    public Object[][] getUpdates() {
    	if (updates != null)
    		return updates;
    	
    	if (dataUpdates == null || dataUpdates.isEmpty())
    		return null;
    	
    	List<EntityUpdate> processedDataUpdates = dataUpdates;
    	if (dataUpdatePostprocessor != null)
    		processedDataUpdates = dataUpdatePostprocessor.process(dataUpdates);
    	
    	// Order updates : persist then updates then removals 
    	Collections.sort(processedDataUpdates);
    	
    	updates = new Object[processedDataUpdates.size()][];
    	int i = 0;
    	Iterator<EntityUpdate> iu = processedDataUpdates.iterator();
    	while (iu.hasNext()) {
    		EntityUpdate u = iu.next();
    		updates[i++] = u.toArray(); 
    	}
		return updates;
    }
    
    public void setDataUpdatePostprocessor(DataUpdatePostprocessor dataUpdatePostprocessor) {
    	this.dataUpdatePostprocessor = dataUpdatePostprocessor;
    }
    
    public static void addUpdate(EntityUpdateType type, Object entity) {
    	addUpdate(type, entity, 0);    	
    }
    public static void addUpdate(EntityUpdateType type, Object entity, int priority) {
    	DataContext dc = get();
    	if (dc != null && dc.dataDispatcher != null) {
    		for (EntityUpdate update : dc.dataUpdates) {
    			if (update.type.equals(type) && update.entity.equals(entity)) {
    				if (update.priority < priority)
    					update.priority = priority;
    				return;
    			}
    		}
    		dc.dataUpdates.add(new EntityUpdate(type, entity, priority));
    		dc.updates = null;
    	}
    }
    
    public static void addEntityExtraData(Object entity, Object extraData) {
    	DataContext dc = get();
    	if (dc != null && dc.entityExtraDataMap != null)
    		dc.entityExtraDataMap.put(entity, extraData);
    }
    
    public static Object getEntityExtraData(Object entity) {
    	DataContext dc = get();
    	return dc != null && dc.entityExtraDataMap != null ? dc.entityExtraDataMap.get(entity) : null;
    }
        
    public static void observe() {
    	DataContext dc = get();
    	if (dc != null && dc.dataDispatcher != null) {
    		log.debug("Observe data updates");
    		dc.dataDispatcher.observe();
    	}
    }
    
    public static void publish() {
    	publish(PublishMode.MANUAL);
    }
    public static void publish(PublishMode publishMode) {
    	DataContext dc = get();
    	if (dc != null && dc.dataDispatcher != null && !dc.dataUpdates.isEmpty() && !dc.published 
    		&& (publishMode == PublishMode.MANUAL || (dc.publishMode.equals(publishMode)))) {
    		log.debug("Publish %s data updates with mode %s", dc.dataUpdates.size(), dc.publishMode);
    		dc.dataDispatcher.publish(dc.getUpdates());
    		// Publish can be called only once but we have to keep the updates until the end of a GraniteDS request
    		dc.published = true;	
    	}
    }
    
    
    public enum EntityUpdateType {
    	PERSIST,
    	UPDATE,
    	REMOVE
    }
    
    public static class EntityUpdate implements Comparable<EntityUpdate> {
    	public EntityUpdateType type;
    	public Object entity;
    	public int priority = 0;

    	public EntityUpdate(EntityUpdateType type, Object entity, int priority) {
    		this.type = type;
    		this.entity = entity;
    		this.priority = priority;
    	}

		public int compareTo(EntityUpdate u) {
			if (type.ordinal() != u.type.ordinal())
				return type.ordinal() - u.type.ordinal();
		    if (!entity.equals(u.entity))
		        return entity.hashCode() - u.entity.hashCode();
	        return priority - u.priority;
		}
		
		public Object[] toArray() {
			return new Object[] { type.name(), entity };
		}
    }
    
    private static class NullDataContext extends DataContext {
    	
    	public NullDataContext() {
    		super(null, null);
    	}
    	
    	@Override
        public List<EntityUpdate> getDataUpdates() {
        	return Collections.emptyList();
        }
    }
}
