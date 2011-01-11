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

package test.granite.ejb3.service;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import test.granite.ejb3.entity.AbstractEntity;

/**
 * @author Franck WOLFF
 */
public class AbstractEntityService {

    @PersistenceContext
    protected EntityManager manager;

    public AbstractEntityService() {
        super();
    }

    protected <T extends AbstractEntity> T merge(T entity) {
        return manager.merge(entity);
    }

    protected <T extends AbstractEntity> T load(Class<T> entityClass, Integer entityId) {
        return manager.find(entityClass, entityId);
    }

    @SuppressWarnings("unchecked")
    protected <T extends AbstractEntity> List<T> findAll(Class<T> entityClass) {
        Query query = manager.createQuery("select distinct e from " + entityClass.getName() + " e");
        return query.getResultList();
    }
}
