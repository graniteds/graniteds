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

package org.granite.tide.spring.data;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.Metamodel;

import org.springframework.data.jpa.domain.Specification;

public class FilterExampleSpecification<T> implements Specification<T> {
	
	private Metamodel metamodel;
	private Object filter;
	
	private FilterExampleSpecification(Metamodel metamodel, Object filter) {
		this.metamodel = metamodel;
		this.filter = filter;
	}
	
	public static <T> FilterExampleSpecification<T> byExample(Metamodel metamodel, Object filter) {
		return new FilterExampleSpecification<T>(metamodel, filter);
	}
	
	public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
		List<Predicate> predicates = new ArrayList<Predicate>();
		
		applyAttributes(predicates, root, builder, metamodel.entity(filter.getClass()), filter);
		
		if (predicates.size() > 0)
			return builder.and(predicates.toArray(new Predicate[predicates.size()]));
		
		return null;
	}
	
	private void applyAttributes(List<Predicate> predicates, Path<?> root, CriteriaBuilder builder, ManagedType<?> filterType, Object filter) {
		// Query by example : the filter is of the same type as the entity
		PropertyDescriptor[] pds = null;
		try {
			// Query by bean filter
			BeanInfo info = Introspector.getBeanInfo(filter.getClass());
			pds = info.getPropertyDescriptors();
		}
		catch (Exception e) {
			throw new RuntimeException("Could not introspect filter bean", e);
		}
		
		for (PropertyDescriptor pd : pds) {
			Attribute<?, ?> attribute = filterType.getAttribute(pd.getName());
			if (attribute == null)
				continue;
			
			if (attribute.getPersistentAttributeType() == Attribute.PersistentAttributeType.EMBEDDED) {
				// Visit embedded elements recursively
				try {
					Object embedded = pd.getReadMethod().invoke(filter);
					ManagedType<?> embeddedType = metamodel.embeddable(attribute.getJavaType());
					applyAttributes(predicates, root.get(pd.getName()), builder, embeddedType, embedded);
				}
				catch (Exception e) {
					throw new RuntimeException("Could not get filter property " + pd.getName(), e);
				}
				
				continue;
			}
			
			if (attribute.getPersistentAttributeType() != Attribute.PersistentAttributeType.BASIC)
				continue;
			
			Object value = null;
			try {
				value = pd.getReadMethod().invoke(filter);
			}
			catch (Exception e) {
				throw new RuntimeException("Could not get filter property " + pd.getName(), e);
			}
			
			Predicate predicate = FilterSpecUtil.buildPredicate(root, builder, pd.getReadMethod().getReturnType(), pd.getName(), value);
			if (predicate != null)
				predicates.add(predicate);
		}
	}
}