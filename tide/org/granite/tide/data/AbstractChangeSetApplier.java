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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.granite.logging.Logger;
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
											if (collection instanceof Set<?>)
												((Set<Object>)collection).add(dereference(collectionChange.getValue()));
											else if (collection instanceof List<?>)
												((List<Object>)collection).add((Integer)collectionChange.getKey(), dereference(collectionChange.getValue()));
											else if (collection instanceof Map<?, ?>)
												((Map<Object, Object>)collection).put(dereference(collectionChange.getKey()), dereference(collectionChange.getValue()));
										}
										else if (collectionChange.getType() == -1) {
											if (collection instanceof Set<?>)
												((Set<Object>)collection).remove(dereference(collectionChange.getValue()));
											else if (collection instanceof List<?>) {
												int index = (Integer)collectionChange.getKey();
												((List<Object>)collection).remove(index);
											}
											else if (collection instanceof Map<?, ?>)
												((Map<Object, Object>)collection).remove(dereference(collectionChange.getKey()));
										}
										else if (collectionChange.getType() == 0) {
											if (collection instanceof Set<?>) {
												// This should not happen with a Set !!
												throw new IllegalStateException("Cannot replace an indexed element on a Set, don't use setItemAt on an ArrayCollection representing a Set !");
											}
											else if (collection instanceof List<?>) {
												int index = (Integer)collectionChange.getKey();
												((List<Object>)collection).set(index, dereference(collectionChange.getValue()));
											}
											else if (collection instanceof Map<?, ?>)
												((Map<Object, Object>)collection).put(dereference(collectionChange.getKey()), dereference(collectionChange.getValue()));
										}
									}
								}
								else {
									if (propertyDescriptor.getWriteMethod() != null)
										propertyDescriptor.getWriteMethod().invoke(entity, dereference(me.getValue()));
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