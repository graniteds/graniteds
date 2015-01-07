/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2015 GRANITE DATA SERVICES S.A.S.
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

import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.granite.config.ConvertersConfig;
import org.granite.context.GraniteContext;
import org.granite.logging.Logger;
import org.granite.messaging.amf.io.convert.Converters;
import org.granite.messaging.amf.io.util.ClassGetter;
import org.granite.messaging.service.ServiceException;
import org.granite.util.Entity;
import org.granite.util.Introspector;
import org.granite.util.PropertyDescriptor;
import org.granite.util.TypeUtil;


/**
 * 	Utility class that applies a ChangeSet on a persistence context
 * 	@author William DRAI
 *
 */
public class ChangeSetApplier {
	
	private static final Logger log = Logger.getLogger(ChangeSetApplier.class);

    private TidePersistenceAdapter persistenceAdapter;

    public ChangeSetApplier(TidePersistenceAdapter persistenceAdapter) {
        this.persistenceAdapter = persistenceAdapter;
    }

    protected long getVersion(Entity e) {
        if (!e.isVersioned())
            throw new IllegalStateException("Cannot apply change set on non versioned entity " + e.getName());
        
        Number version = (Number)e.getVersion();
        if (version == null)
        	throw new IllegalStateException("Cannot apply changes on non persistent entity " + e.getName() + ":" + e.getIdentifier());
        
        return version.longValue();
    }


    protected Object mergeObject(Object entity, IdentityHashMap<Object, Object> cache) {
        if (entity == null)
            return null;

        ConvertersConfig config = GraniteContext.getCurrentInstance().getGraniteConfig();
        ClassGetter classGetter = config.getClassGetter();
        Converters converters = config.getConverters();
        
        if (entity != null && !classGetter.isInitialized(null, null, entity)) {
            // cache.contains() cannot be called on an unintialized proxy because hashCode will fail !!
            Class<?> cls = classGetter.getClass(entity);
            Entity e = new Entity(cls);
            return persistenceAdapter.find(cls, (Serializable)converters.convert(classGetter.getIdentifier(entity), e.getIdentifierType()));
        }
        
        if (cache.containsKey(entity))
            return cache.get(entity);
        
        if (entity instanceof ChangeRef) {
            ChangeRef ref = (ChangeRef)entity;
            try {
                Class<?> entityClass = TypeUtil.forName(ref.getClassName());
                Entity e = new Entity(entityClass);                
                Serializable refId = (Serializable)converters.convert(ref.getId(), e.getIdentifierType());
                
                // Lookup in current merge cache, if not found then lookup from persistence adapter
                for (Object cached : cache.values()) {
                	if (cached.getClass().equals(entityClass) && refId.equals(e.getIdentifier(cached)))
                		return cached;
                }
                
                Object managedEntity = persistenceAdapter.find(entityClass, refId);
                cache.put(entity, managedEntity);
                return managedEntity;
            }
            catch (ClassNotFoundException cnfe) {
                throw new ServiceException("Could not find class " + ref.getClassName(), cnfe);
            }
        }

        cache.put(entity, entity);

        if (entity != null && classGetter.isEntity(entity) && classGetter.isInitialized(null, null, entity)) {
            // If the entity has an id, we should get the managed instance
            Entity e = new Entity(entity);
            Object id = e.getIdentifier();
            if (id != null) {
                Object managedEntity = persistenceAdapter.find(entity.getClass(), (Serializable)id);
                cache.put(entity, managedEntity);
                return managedEntity;
            }

            cache.put(entity, entity);
            
            // If there is no id, traverse the object graph to merge associated objects
            List<Object[]> fieldValues = classGetter.getFieldValues(entity);
            for (Object[] fieldValue : fieldValues) {
                Object value = fieldValue[1];
                Field field = (Field)fieldValue[0];
                if (value == null)
                    continue;
                
                if (!classGetter.isInitialized(entity, field.getName(), value)) {                	
                	if (!classGetter.getClass(value).isAnnotationPresent(javax.persistence.Entity.class))
                		continue;
            	
                    try {
                    	// Lookup in cache
                    	Serializable valueId = classGetter.getIdentifier(value);
                    	Object newValue = null;
                    	Entity ve = new Entity(field.getType());
                    	for (Object cached : cache.values()) {
                    		if (field.getType().isInstance(cached) && valueId.equals(ve.getIdentifier(cached))) {
                    			newValue = cached;
                    			break;
                    		}
                    	}                    	
                    	if (newValue == null)
                    		newValue = persistenceAdapter.find(field.getType(), valueId);
                    	
                        field.set(entity, newValue);
                    }
                    catch (IllegalAccessException e1) {
                        throw new ServiceException("Could not set entity field value on " + value.getClass() + "." + field.getName());
                    }
                }
                
                if (value instanceof Collection<?>) {
                    @SuppressWarnings("unchecked")
					Collection<Object> coll = (Collection<Object>)value;
                    Iterator<Object> icoll = coll.iterator();
                    Set<Object> addedElements = new HashSet<Object>();
                    int idx = 0;
                    while (icoll.hasNext()) {
                        Object element = icoll.next();
                        if (element != null) {
	                        Object newElement = mergeObject(element, cache);
	                        if (newElement != element) {
	                            if (coll instanceof List<?>)
	                                ((List<Object>)coll).set(idx, newElement);
	                            else {
	                                icoll.remove();
	                                addedElements.add(newElement);
	                            }
	                        }
                        }
                        idx++;
                    }
                    if (!(coll instanceof List<?>))
                        coll.addAll(addedElements);
                }
                else if (value.getClass().isArray()) {
                    for (int idx = 0; idx < Array.getLength(value); idx++) {
                        Object element = Array.get(value, idx);
                        if (element == null)
                            continue;
                        Object newElement = mergeObject(element, cache);
                        if (newElement != element)
                            Array.set(value, idx, newElement);
                    }
                }
                else if (value instanceof Map<?, ?>) {
                    @SuppressWarnings("unchecked")
					Map<Object, Object> map = (Map<Object, Object>)value;
                    Iterator<Entry<Object, Object>> ime = map.entrySet().iterator();
                    Map<Object, Object> addedElements = new HashMap<Object, Object>();
                    while (ime.hasNext()) {
                        Entry<Object, Object> me = ime.next();
                        Object val = me.getValue();
                        if (val != null) {
                            Object newVal = mergeObject(val, cache);
                            if (newVal != val)
                                me.setValue(newVal);
                        }
                        Object key = me.getKey();
                        if (key != null) {
                            Object newKey = mergeObject(key, cache);
                            if (newKey != key) {
                                ime.remove();
                                addedElements.put(newKey, me.getValue());
                            }
                        }
                    }
                    map.putAll(addedElements);
                }
                else if (classGetter.isEntity(value)) {
                    Object newValue = mergeObject(value, cache);
                    if (newValue != value) {
                        try {
                            field.set(entity, newValue);
                        }
                        catch (IllegalAccessException e1) {
                            throw new ServiceException("Could not set entity field value on " + value.getClass() + "." + field.getName());
                        }
                    }
                }
            }
        }

        return entity;
    }

	public Object[] applyChanges(ChangeSet changeSet) {
		IdentityHashMap<Object, Object> cache = new IdentityHashMap<Object, Object>();
		Object[] appliedChanges = new Object[changeSet.getChanges().length];
		for (int i = 0; i < changeSet.getChanges().length; i++)
			appliedChanges[i] = applyChange(changeSet.getChanges()[i], cache);
		
		return appliedChanges;
	}
	
	@SuppressWarnings("unchecked")
	private Object applyChange(Change change, IdentityHashMap<Object, Object> cache) {
		Converters converters = ((ConvertersConfig)GraniteContext.getCurrentInstance().getGraniteConfig()).getConverters();
		
		Object appliedChange = null;
		try {
			Class<?> entityClass = TypeUtil.forName(change.getClassName());
			if (change.getId() != null) {
                Entity e = new Entity(entityClass);
                Type identifierType = e.getIdentifierType();
				Object entity = persistenceAdapter.find(entityClass, (Serializable)converters.convert(change.getId(), identifierType));
				if (entity == null) {
					log.debug("Entity not found, maybe has already been deleted by cascading");
					return null;
				}
				
				e = new Entity(entity);
                Long version = getVersion(e);
				if ((change.getVersion() != null && change.getVersion().longValue() < version) || (change.getVersion() == null && version != null))
					persistenceAdapter.throwOptimisticLockException(entity);
				
				appliedChange = entity;
				
				for (Entry<String, Object> me : change.getChanges().entrySet()) {
					try {
						PropertyDescriptor[] propertyDescriptors = Introspector.getPropertyDescriptors(entityClass);
						PropertyDescriptor propertyDescriptor = null;
						for (PropertyDescriptor pd : propertyDescriptors) {
							if (pd.getName().equals(me.getKey())) {
								propertyDescriptor = pd;
								break;
							}
						}
						if (propertyDescriptor == null) {
							log.warn("Could not find property " + me.getKey() + " on class " + change.getClassName());
						}
						else {
							if (me.getValue() instanceof CollectionChanges) {
								Object collection = propertyDescriptor.getReadMethod().invoke(entity);
								
								CollectionChanges collectionChanges = (CollectionChanges)me.getValue();
								for (CollectionChange collectionChange : collectionChanges.getChanges()) {
									if (collectionChange.getType() == 1) {
										if (collection instanceof Set<?>) {
											Type collectionType = propertyDescriptor.getReadMethod().getGenericReturnType();
											Type elementType = Object.class;
											if (collectionType instanceof ParameterizedType)
												elementType = ((ParameterizedType)collectionType).getActualTypeArguments()[0];
											
											Object value = converters.convert(mergeObject(collectionChange.getValue(), cache), elementType);
											((Set<Object>)collection).add(value);
										}
										else if (collection instanceof List<?>) {
											Type collectionType = propertyDescriptor.getReadMethod().getGenericReturnType();
											Type elementType = Object.class;
											if (collectionType instanceof ParameterizedType)
												elementType = ((ParameterizedType)collectionType).getActualTypeArguments()[0];
											
											Object value = converters.convert(mergeObject(collectionChange.getValue(), cache), elementType);
											((List<Object>)collection).add((Integer)collectionChange.getKey(), value);
										}
										else if (collection instanceof Map<?, ?>) {
											Type mapType = propertyDescriptor.getReadMethod().getGenericReturnType();
											Type keyType = Object.class;
											Type valueType = Object.class;
											if (mapType instanceof ParameterizedType) {
												keyType = ((ParameterizedType)mapType).getActualTypeArguments()[0];
												valueType = ((ParameterizedType)mapType).getActualTypeArguments()[1];
											}
											
											Object key = converters.convert(mergeObject(collectionChange.getKey(), cache), keyType);
											Object value = converters.convert(mergeObject(collectionChange.getValue(), cache), valueType);
											((Map<Object, Object>)collection).put(key, value);
										}
									}
									else if (collectionChange.getType() == -1) {
										if (collection instanceof Set<?>) {
											Type collectionType = propertyDescriptor.getReadMethod().getGenericReturnType();
											Type elementType = Object.class;
											if (collectionType instanceof ParameterizedType)
												elementType = ((ParameterizedType)collectionType).getActualTypeArguments()[0];
											
											Object value = converters.convert(mergeObject(collectionChange.getValue(), cache), elementType);
											Object removed = ((Set<Object>)collection).remove(value);
											cache.put(removed, value);
										}
										else if (collection instanceof List<?>) {
											int index = (Integer)collectionChange.getKey();
											Object removed = ((List<Object>)collection).remove(index);
											cache.put(removed, removed);
										}
										else if (collection instanceof Map<?, ?>) {
											Type mapType = propertyDescriptor.getReadMethod().getGenericReturnType();
											Type keyType = Object.class;
											if (mapType instanceof ParameterizedType)
												keyType = ((ParameterizedType)mapType).getActualTypeArguments()[0];
											
											Object key = converters.convert(mergeObject(collectionChange.getKey(), cache), keyType);
											Object removed = ((Map<Object, Object>)collection).remove(key);
											cache.put(removed, key);
										}
									}
									else if (collectionChange.getType() == 0) {
										if (collection instanceof Set<?>) {
											// This should not happen with a Set !!
											throw new IllegalStateException("Cannot replace an indexed element on a Set, don't use setItemAt on an ArrayCollection representing a Set !");
										}
										else if (collection instanceof List<?>) {
											int index = (Integer)collectionChange.getKey();
											Type collectionType = propertyDescriptor.getReadMethod().getGenericReturnType();
											Type elementType = Object.class;
											if (collectionType instanceof ParameterizedType)
												elementType = ((ParameterizedType)collectionType).getActualTypeArguments()[0];
											
											Object value = converters.convert(mergeObject(collectionChange.getValue(), cache), elementType);
											((List<Object>)collection).set(index, value);
										}
										else if (collection instanceof Map<?, ?>) {
											Type mapType = propertyDescriptor.getReadMethod().getGenericReturnType();
											Type keyType = Object.class;
											Type valueType = Object.class;
											if (mapType instanceof ParameterizedType) {
												keyType = ((ParameterizedType)mapType).getActualTypeArguments()[0];
												valueType = ((ParameterizedType)mapType).getActualTypeArguments()[1];
											}
											
											Object key = converters.convert(mergeObject(collectionChange.getKey(), cache), keyType);
											Object value = converters.convert(mergeObject(collectionChange.getValue(), cache), valueType);
											((Map<Object, Object>)collection).put(key, value);
										}
									}
								}
							}
							else {
								if (propertyDescriptor.getWriteMethod() != null) {
									Object value = mergeObject(me.getValue(), cache);
									value = converters.convert(value, propertyDescriptor.getWriteMethod().getGenericParameterTypes()[0]);
									propertyDescriptor.getWriteMethod().invoke(entity, value);
								}
								else
									log.warn("Property " + me.getKey() + " on class " + change.getClassName() + " is not writeable");
							}
						}
					}
					catch (InvocationTargetException ite) {
						throw new ServiceException("Could not set property " + me.getKey(), ite.getTargetException());
					}
					catch (IllegalAccessException iae) {
						throw new ServiceException("Could not set property " + me.getKey(), iae);
					}
				}
			}
			
			return appliedChange;
		}
		catch (ClassNotFoundException cnfe) {
			throw new ServiceException("Could not find class " + change.getClassName(), cnfe);
		}
	}

}