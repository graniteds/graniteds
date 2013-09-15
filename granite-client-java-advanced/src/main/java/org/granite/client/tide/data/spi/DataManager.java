/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of the Granite Data Services Platform.
 *
 *                               ***
 *
 *   Community License: GPL 3.0
 *
 *   This file is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published
 *   by the Free Software Foundation, either version 3 of the License,
 *   or (at your option) any later version.
 *
 *   This file is distributed in the hope that it will be useful, but
 *   WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *                               ***
 *
 *   Available Commercial License: GraniteDS SLA 1.0
 *
 *   This is the appropriate option if you are creating proprietary
 *   applications and you are not prepared to distribute and share the
 *   source code of your application under the GPL v3 license.
 *
 *   Please visit http://www.granitedataservices.com/license for more
 *   details.
 */
package org.granite.client.tide.data.spi;

import java.util.Map;

/**
 * @author William DRAI
 */
public interface DataManager {
    
    public boolean isEntity(Object entity);
    
    public Object getId(Object entity);
    
    public boolean hasIdProperty(Object entity);
    
    public String getDetachedState(Object entity);
    
    public boolean defineProxy(Object target, Object source);
    
    public void copyProxyState(Object target, Object source);
    
    public void copyUid(Object target, Object source);
    
    public Object getVersion(Object entity);
    
    public boolean hasVersionProperty(Object entity);
    
    public String getVersionPropertyName(Object entity);
    
    public String getUid(Object entity);
    
    public Map<String, Object> getPropertyValues(Object entity, boolean excludeIdUid, boolean excludeVersion, boolean includeReadOnly);
    
    public Map<String, Object> getPropertyValues(Object entity, boolean excludeVersion, boolean includeReadOnly);
    
    public Object getPropertyValue(Object entity, String name);
    
    public void setPropertyValue(Object entity, String name, Object value);
    
    public boolean isLazyProperty(Object entity, String name);
    
    public void setLazyProperty(Object entity, String name);
    
    public String getCacheKey(Object entity);
    
    public boolean isInitialized(Object entity);
    
    public boolean isDirty();
    
    public boolean isDirtyEntity(Object entity);
    
    public boolean isDeepDirtyEntity(Object entity);
    
    
    public void setTrackingHandler(TrackingHandler trackingHandler);

    
    public static enum TrackingType {        
        LIST,
        SET,
        MAP,
        ENTITY_PROPERTY,
        ENTITY_LIST,
        ENTITY_SET,
        ENTITY_MAP
    }
    
    public static enum ChangeKind {
        ADD,
        REMOVE,
        REPLACE,
        UPDATE
    }

    
    /**
     *  Start tracking for specific object / parent
     *
     *  @param previous previously existing object in the entity manager cache (null if no existing object)
     *  @param parent parent object for collections
     */
    public void startTracking(Object previous, Object parent);
    
    /**
     *  Stop tracking for specific object / parent
     *
     *  @param previous previously existing object in the entity manager cache (null if no existing object)
     *  @param parent parent object for collections
     */
    public void stopTracking(Object previous, Object parent);


    public void notifyDirtyChange(boolean oldDirty, boolean dirty);

    public void notifyEntityDirtyChange(Object entity, boolean oldDirtyEntity, boolean newDirtyEntity);
    
    
    /**
     *  Reset all currently tracked objects
     */
    public void clear();          
    
    
    public static interface TrackingHandler {
        
        public void collectionChangeHandler(ChangeKind kind, Object target, Integer location, Object[] items);
        
        public void mapChangeHandler(ChangeKind kind, Object target, Integer location, Object[] items);

        public void entityPropertyChangeHandler(Object target, String property, Object oldValue, Object newValue);

        public void entityCollectionChangeHandler(ChangeKind kind, Object target, Integer location, Object[] items);
        
        public void entityMapChangeHandler(ChangeKind kind, Object target, Integer location, Object[] items);
    }


}
