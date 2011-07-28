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

package org.granite.example.crud.guice.services;

import java.util.Collection;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.granite.example.crud.entity.Car;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.wideplay.warp.persist.Transactional;


/**
 * @author Matt GIACOMI
 */
public class Cars {

    @Inject
    private Provider<EntityManager> em;

    public Collection<?> getCars() {
        Query q = em.get().createQuery("select x from Car x");
        return q.getResultList();
    }

    @Transactional
    public void addCar(Car car) {
        em.get().persist(car);
    }

    @Transactional
    public void removeCar(Car car) {
        Query q = em.get().createQuery("select x from Car x where x.id = :id");
        q.setParameter("id", Integer.valueOf(car.getId()));
        Car carClone = (Car)q.getSingleResult();
        em.get().remove(carClone);
    }

    @Transactional
    public void updateCar(Car car) {
        em.get().merge(car);
    }
}
