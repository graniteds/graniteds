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

import java.util.HashSet;
import java.util.Set;

import org.granite.tide.data.DataEnabled.PublishMode;


/**
 * @author William DRAI
 */
public class DataContext {
    
    private static ThreadLocal<DataContext> dataContext = new ThreadLocal<DataContext>();
    
    private DataDispatcher dataDispatcher = null;
    private PublishMode publishMode = null;
    
    
    public static void init(String topic, Class<? extends DataTopicParams> dataTopicParamsClass, PublishMode publishMode) {
    	DataContext dc = new DataContext(topic, dataTopicParamsClass, publishMode);
    	dataContext.set(dc);
    }
    
    private DataContext(String topic, Class<? extends DataTopicParams> dataTopicParamsClass, PublishMode publishMode) {
		dataDispatcher = new DataDispatcher(topic, dataTopicParamsClass);
		this.publishMode = publishMode;
    }
    
    public static DataContext get() {
        return dataContext.get();
    }
    
    public static void remove() {
    	dataContext.remove();
    }
    
    private final Set<Object[]> dataUpdates = new HashSet<Object[]>();

    
    public Set<Object[]> getDataUpdates() {
        return dataUpdates;
    }
    
    public static void addUpdate(EntityUpdateType type, Object entity) {
    	DataContext dc = get();
    	if (dc != null)
    		dc.dataUpdates.add(new Object[] { type.name(), entity });
    }
    
    
    public static void observe() {
    	DataContext dc = get();
    	if (dc != null)
    		dc.dataDispatcher.observe();
    }
    
    public static void publish() {
    	publish(PublishMode.MANUAL);
    }
    public static void publish(PublishMode publishMode) {
    	DataContext dc = get();
    	if (dc != null && !dc.dataUpdates.isEmpty() 
    		&& (publishMode == PublishMode.MANUAL || (dc.publishMode.equals(publishMode)))) {
    		dc.dataDispatcher.publish(dc.dataUpdates);
    		dc.dataUpdates.clear();
    	}
    }
    
    
    public enum EntityUpdateType {
    	PERSIST,
    	UPDATE,
    	REMOVE
    }
}
