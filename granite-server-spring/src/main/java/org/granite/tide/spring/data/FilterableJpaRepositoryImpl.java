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
package org.granite.tide.spring.data;

import java.io.Serializable;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.metamodel.ManagedType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.JpaEntityInformationSupport;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;


public class FilterableJpaRepositoryImpl<T, ID extends Serializable> extends SimpleJpaRepository<T, ID> implements FilterableJpaRepository<T, ID> {

	private JpaEntityInformation<T, ?> entityInformation;
	private EntityManager entityManager;

	public FilterableJpaRepositoryImpl(Class<T> domainClass, EntityManager entityManager) {
		super(domainClass, entityManager);
		
		this.entityInformation = JpaEntityInformationSupport.getMetadata(domainClass, entityManager);
	    this.entityManager = entityManager;
	}
	
	@SuppressWarnings("unchecked")
	public Page<T> findByFilter(Object filter, Pageable pageable) {
		if (filter == null)
			return findAll(pageable);
		
		ManagedType<?> filterType = entityManager.getMetamodel().managedType(entityInformation.getJavaType());		
		Specification<T> specification = null;
		
		if (filter.getClass().equals(filterType.getJavaType()))
			specification = FilterExampleSpecification.byExample(entityManager.getMetamodel(), filter);		
		else if (filter instanceof Map<?, ?>)
			specification = FilterMapSpecification.byMap((Map<String, Object>)filter);
		else
			specification = FilterBeanSpecification.byBean(filter);
		
		return findAll(specification, pageable);
	}
}
