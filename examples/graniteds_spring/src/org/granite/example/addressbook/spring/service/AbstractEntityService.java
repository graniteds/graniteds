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

package org.granite.example.addressbook.spring.service;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.granite.example.addressbook.entity.AbstractEntity;
import org.springframework.transaction.annotation.Transactional;


public class AbstractEntityService {

	@PersistenceContext
    protected EntityManager entityManager;

	
    @Transactional
    protected <T extends AbstractEntity> T merge(final T entity) {
    	T merged = entityManager.merge(entity);
    	entityManager.flush();
        return merged;
    }

    @Transactional(readOnly=true)
    protected <T extends AbstractEntity> T load(Class<T> entityClass, Integer entityId) {
        return entityManager.find(entityClass, entityId);
    }

    @Transactional(readOnly=true)
    @SuppressWarnings("unchecked")
    protected <T extends AbstractEntity> List<T> findAll(Class<T> entityClass) {
        return entityManager.createQuery("select distinct e from " + entityClass.getName() + " e").getResultList();
    }
}
