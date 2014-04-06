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
package org.granite.client.tide.data.spi;

import java.util.Map;

/**
 * SPI for integrating with different types of data objects (javabeans, javafx bindable beans, ...)
 *
 * @author William DRAI
 */
public interface DataManager {

    /**
     * Is the object an entity (i.e. annotated with {@link org.granite.client.persistence.Entity}) ?
     * @param object object
     * @return true if entity
     */
    public boolean isEntity(Object object);

    /**
     * Return the persistent id of an entity (field annotated with {@link org.granite.client.persistence.Id}) ?
     * @param entity object
     * @return id if field found, error if no id field
     */
    public Object getId(Object entity);

    /**
     * Has the entity an id field ?
     * @param entity object
     * @return true if id field found
     */
    public boolean hasIdProperty(Object entity);

    /**
     * Name of the id field for the entity
     * @param entity object
     * @return property name or null if not found
     */
    public String getIdPropertyName(Object entity);

    /**
     * Return the persistent detached state of an entity (private field name __detachedState__)
     * @param entity object
     * @return detached state if field found, error if field not found
     */
    public String getDetachedState(Object entity);

    /**
     * Init state of a proxy object
     * @param dest proxy object
     * @param id proxy id
     * @param initialized is initialized
     * @param detachedState detached state
     */
    public void initProxy(Object dest, Object id, boolean initialized, String detachedState);

    /**
     * Define the target object as a proxy for the source entity
     * @param target target object
     * @param source source entity
     * @return true if proxy was setup, false if source was not an entity or did not have any detached state
     */
    public boolean defineProxy(Object target, Object source);

    /**
     * Copy the proxy state (fields __initialized__ and __detachedState__) from source to target
     * @param target target object
     * @param source source entity
     */
    public void copyProxyState(Object target, Object source);

    /**
     * Copy the uid field (annotated with {@link org.granite.client.persistence.Uid}) from source to target
     * @param target target object
     * @param source source entity
     */
    public void copyUid(Object target, Object source);

    /**
     * Return the version (field annotated with {@link org.granite.client.persistence.Version}) for an entity
     * @param entity entity
     * @return version or error if no version field found
     */
    public Object getVersion(Object entity);

    /**
     * Has the entity a version field (annotated with {@link org.granite.client.persistence.Version}) ?
     * @param entity object
     * @return true if version field found
     */
    public boolean hasVersionProperty(Object entity);

    /**
     * Name of the version field for the entity
     * @param entity object
     * @return property name or null if not found
     */
    public String getVersionPropertyName(Object entity);

    /**
     * Get the uid field (annotated with {@link org.granite.client.persistence.Uid}) for the entity
     * Note that the data manager implementation is allowed to generate a reasonable uid value from other fields
     * if no uid field is present
     * @param entity object
     * @return uid or error if uid could not be generated
     */
    public String getUid(Object entity);

    /**
     * Name of the uid field for the entity
     * @param entity object
     * @return property name or null if not found
     */
    public String getUidPropertyName(Object entity);

    /**
     * Return the value of the property for the specified entity instance
     * @param entity object instance
     * @param name property name
     * @return property exists
     */
    public boolean hasProperty(Object entity, String name);

    /**
     * Return a map of property values for the specified entity instance
     * @param entity object instance
     * @param excludeIdUid true to exclude id and uid fields from the map
     * @param excludeVersion true to exclude version field from the map
     * @param includeReadOnly true to include readonly fields in the map
     * @return a map of values keyed by property names
     */
    public Map<String, Object> getPropertyValues(Object entity, boolean excludeIdUid, boolean excludeVersion, boolean includeReadOnly);

    /**
     * Return a map of property values for the specified entity instance
     * @param entity object instance
     * @param excludeVersion true to exclude version field from the map
     * @param includeReadOnly true to include readonly fields in the map
     * @return a map of values keyed by property names
     */
    public Map<String, Object> getPropertyValues(Object entity, boolean excludeVersion, boolean includeReadOnly);

    /**
     * Return the value of the property for the specified entity instance
     * @param entity object instance
     * @param name property name
     * @return property value
     */
    public Object getPropertyValue(Object entity, String name);

    /**
     * Set the value of the property for the specified entity instance
     * @param entity object instance
     * @param name property name
     * @param value property value
     */
    public void setPropertyValue(Object entity, String name, Object value);

    /**
     * Is the specified property lazy ?
     * A property is considered lazy if it is annotated with {@link org.granite.client.persistence.Lazy} or if the
     * server has returned an initialized value
     * @param entity entity instance
     * @param name property name
     * @return true if property defined as lazy
     */
    public boolean isLazyProperty(Object entity, String name);

    /**
     * Define a property as lazy
     * @param entity entity instance
     * @param name property name
     */
    public void setLazyProperty(Object entity, String name);

    /**
     * Return a unique key for the entity to be used in a local cache
     * @param entity entity instance
     * @return unique key
     */
    public String getCacheKey(Object entity);

    /**
     * Is the entity initialized (field __initialized__ true) ?
     * @param entity entity instance
     * @return true if initialized
     */
    public boolean isInitialized(Object entity);

    /**
     * Is the data manager dirty (any managed entity instance has been modified since last received from server) ?
     * @return true if modified
     */
    public boolean isDirty();

    /**
     * Is the entity instance dirty (modified since last received from server) ?
     * @return true if modified
     */
    public boolean isDirtyEntity(Object entity);

    /**
     * Is the entity graph dirty (any object in the graph of this entity has been modified since last received from server) ?
     * @return true if modified
     */
    public boolean isDeepDirtyEntity(Object entity);

    /**
     * Create an empty instance of the same class as the source
      * @param source source object
     * @param cast expected type
     * @param <T> expected type
     * @return new empty instance
     */
    public <T> T newInstance(Object source, Class<T> cast) throws IllegalAccessException, InstantiationException;


    /**
     * Register a handler that will be notified when any managed entity is modified
     * @param trackingHandler tracking handler
     */
    public void setTrackingHandler(TrackingHandler trackingHandler);

    /**
     * Types of tracked events
     */
    public static enum TrackingType {        
        LIST,
        SET,
        MAP,
        ENTITY_PROPERTY,
        ENTITY_LIST,
        ENTITY_SET,
        ENTITY_MAP
    }

    /**
     * Kinds of changes for collections and maps
     */
    public static enum ChangeKind {
        ADD,
        REMOVE,
        REPLACE,
        UPDATE
    }

    
    /**
     *  Start tracking for the specified object / parent
     *
     *  @param previous previously existing object in the entity manager cache (null if no existing object)
     *  @param parent parent object for collections
     */
    public void startTracking(Object previous, Object parent);
    
    /**
     *  Stop tracking for the specified object / parent
     *
     *  @param previous previously existing object in the entity manager cache (null if no existing object)
     *  @param parent parent object for collections
     */
    public void stopTracking(Object previous, Object parent);

    /**
     *  Reset all currently tracked objects
     */
    public void clear();

    /**
     * Callback interface for objects that need to be notified when modifications are made on managed entities
     */
    public static interface TrackingHandler {

        /**
         * Callback called when a collection is modified
         * @param kind kind of collection change
         * @param target modified collection
         * @param location index/key of the modification
         * @param items array of modified items
         */
        public void collectionChangeHandler(ChangeKind kind, Object target, Integer location, Object[] items);

        /**
         * Callback called when a map is modified
         * @param kind kind of map change
         * @param target modified map
         * @param location key of the modification
         * @param items array of modified entries (each entry is an array [ key, value ])
         */
        public void mapChangeHandler(ChangeKind kind, Object target, Integer location, Object[] items);

        /**
         * Callback when a property of an entity is modified
         * @param target modified entity
         * @param property property name
         * @param oldValue previous value
         * @param newValue new value
         */
        public void entityPropertyChangeHandler(Object target, String property, Object oldValue, Object newValue);

        /**
         * Callback called when a collection owned by an entity (x-to-many association) is modified
         * @param kind kind of collection change
         * @param target modified collection
         * @param location index/key of the modification
         * @param items array of modified items
         */
        public void entityCollectionChangeHandler(ChangeKind kind, Object target, Integer location, Object[] items);

        /**
         * Callback called when a map owned by an entity (x-to-many association) is modified
         * @param kind kind of map change
         * @param target modified map
         * @param location key of the modification
         * @param items array of modified entries (each entry is an array [ key, value ])
         */
        public void entityMapChangeHandler(ChangeKind kind, Object target, Integer location, Object[] items);
    }

    /**
     * Notify listeners that the dirty state of this data manager has changed
     * Called by dirty checking
     * @param oldDirty old value
     * @param dirty new value
     * @see org.granite.client.tide.data.spi.DirtyCheckContext
     */
    public void notifyDirtyChange(boolean oldDirty, boolean dirty);

    /**
     * Notify listeners that the dirty state of the specified has changed
     * @param entity entity instance
     * @param oldDirtyEntity old value
     * @param newDirtyEntity new value
     * @see org.granite.client.tide.data.spi.DirtyCheckContext
     */
    public void notifyEntityDirtyChange(Object entity, boolean oldDirtyEntity, boolean newDirtyEntity);

}
