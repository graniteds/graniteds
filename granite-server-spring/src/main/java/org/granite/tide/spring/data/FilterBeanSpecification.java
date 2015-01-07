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
package org.granite.tide.spring.data;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;

public class FilterBeanSpecification<T> implements Specification<T> {
	
	private Object filter;
	
	private FilterBeanSpecification(Object filter) {
		this.filter = filter;
	}
	
	public static <T> FilterBeanSpecification<T> byBean(Object filter) {
		return new FilterBeanSpecification<T>(filter);
	}
	
	public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
		List<Predicate> predicates = new ArrayList<Predicate>();
		
		// Query by bean filter
		PropertyDescriptor[] pds = null;
		try {
			BeanInfo info = Introspector.getBeanInfo(filter.getClass());
			pds = info.getPropertyDescriptors();
		}
		catch (Exception e) {
			throw new RuntimeException("Could not introspect filter bean", e);
		}
		
		for (PropertyDescriptor pd : pds) {
			if (pd.getWriteMethod() == null || !pd.getReadMethod().isAnnotationPresent(FilterMapping.class) || pd.getReadMethod().getAnnotation(FilterMapping.class).mode() == FilterMode.EXCLUDE)
				continue;
			
			FilterMapping mapping = pd.getReadMethod().getAnnotation(FilterMapping.class);
			if (root.get(mapping.value()) == null)
				throw new RuntimeException("Invalid filter mapping, path: " + mapping.value());
					
			Object value = null;
			try {
				value = pd.getReadMethod().invoke(filter);
			}
			catch (Exception e) {
				throw new RuntimeException("Could not get filter property " + pd.getName(), e);
			}
			
			Predicate predicate = FilterSpecUtil.buildPredicate(root, builder, pd.getReadMethod().getReturnType(), mapping.value(), value);
			if (predicate != null)
				predicates.add(predicate);
		}
		
		if (predicates.size() > 0)
			return builder.and(predicates.toArray(new Predicate[predicates.size()]));
		
		return null;
	}
}