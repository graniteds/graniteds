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

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.granite.context.GraniteContext;
import org.granite.logging.Logger;
import org.granite.messaging.amf.io.convert.Converters;
import org.granite.messaging.service.ServiceException;
import org.granite.util.ClassUtil;


/**
 * 	Utility class that applies a ChangeSet on a persistence context
 * 	@author William DRAI
 *
 */
public abstract class AbstractChangeSetApplier {
	
	private static final Logger log = Logger.getLogger(AbstractChangeSetApplier.class);
	
	protected abstract Object find(Class<?> entityClass, Serializable id);
	
	protected abstract long getVersion(Object entity);
	
	protected abstract void throwOptimisticLockException(Object entity);
	
	protected abstract Object dereference(Object entity);

	
	@SuppressWarnings({ "unchecked" })
	public Set<Object> applyChanges(ChangeSet changeSet) {
		Converters converters = GraniteContext.getCurrentInstance().getGraniteConfig().getConverters();
		
		Set<Object> appliedChanges = new HashSet<Object>();
		for (Change change : changeSet.getChanges()) {
			try {
				Class<?> entityClass = ClassUtil.forName(change.getClassName());
				if (change.getId() != null) {
					Object entity = find(entityClass, change.getId());
					Long version = getVersion(entity);
					if (change.getVersion() != null && change.getVersion().longValue() < version)
						throwOptimisticLockException(entity);
					
					appliedChanges.add(entity);
					
					for (Entry<String, Object> me : change.getChanges().entrySet()) {
						try {
							PropertyDescriptor[] propertyDescriptors = ClassUtil.getProperties(entityClass);
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
												
												Object value = converters.convert(dereference(collectionChange.getValue()), elementType);
												((Set<Object>)collection).add(value);
											}
											else if (collection instanceof List<?>) {
												Type collectionType = propertyDescriptor.getReadMethod().getGenericReturnType();
												Type elementType = Object.class;
												if (collectionType instanceof ParameterizedType)
													elementType = ((ParameterizedType)collectionType).getActualTypeArguments()[0];
												
												Object value = converters.convert(dereference(collectionChange.getValue()), elementType);
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
												
												Object key = converters.convert(dereference(collectionChange.getKey()), keyType);
												Object value = converters.convert(dereference(collectionChange.getValue()), valueType);
												((Map<Object, Object>)collection).put(key, value);
											}
										}
										else if (collectionChange.getType() == -1) {
											if (collection instanceof Set<?>) {
												Type collectionType = propertyDescriptor.getReadMethod().getGenericReturnType();
												Type elementType = Object.class;
												if (collectionType instanceof ParameterizedType)
													elementType = ((ParameterizedType)collectionType).getActualTypeArguments()[0];
												
												Object value = converters.convert(dereference(collectionChange.getValue()), elementType);
												((Set<Object>)collection).remove(value);
											}
											else if (collection instanceof List<?>) {
												int index = (Integer)collectionChange.getKey();
												((List<Object>)collection).remove(index);
											}
											else if (collection instanceof Map<?, ?>) {
												Type mapType = propertyDescriptor.getReadMethod().getGenericReturnType();
												Type keyType = Object.class;
												if (mapType instanceof ParameterizedType)
													keyType = ((ParameterizedType)mapType).getActualTypeArguments()[0];
												
												Object key = converters.convert(dereference(collectionChange.getKey()), keyType);
												((Map<Object, Object>)collection).remove(key);
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
												
												Object value = converters.convert(dereference(collectionChange.getValue()), elementType);
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
												
												Object key = converters.convert(dereference(collectionChange.getKey()), keyType);
												Object value = converters.convert(dereference(collectionChange.getValue()), valueType);
												((Map<Object, Object>)collection).put(key, value);
											}
										}
									}
								}
								else {
									if (propertyDescriptor.getWriteMethod() != null) {
										Object value = dereference(me.getValue());
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
			}
			catch (ClassNotFoundException cnfe) {
				throw new ServiceException("Could not find class " + change.getClassName(), cnfe);
			}
		}
		
		return appliedChanges;
	}

}