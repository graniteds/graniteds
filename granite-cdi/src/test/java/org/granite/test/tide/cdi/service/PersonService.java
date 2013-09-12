/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of Granite Data Services.
 *
 *   Granite Data Services is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU Library General Public License as published by
 *   the Free Software Foundation; either version 2 of the License, or (at your
 *   option) any later version.
 *
 *   Granite Data Services is distributed in the hope that it will be useful, but
 *   WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *   FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
 *   for more details.
 *
 *   You should have received a copy of the GNU Library General Public License
 *   along with this library; if not, see <http://www.gnu.org/licenses/>.
 */
package org.granite.test.tide.cdi.service;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;

import org.granite.test.tide.data.Person;
import org.granite.tide.data.DataEnabled;


@DataEnabled(topic="testTopic")
public class PersonService {
		
	@Inject
	private EntityManagerFactory entityManagerFactory;

	
    public void create(Long id, String lastName) {
    	EntityManager entityManager = entityManagerFactory.createEntityManager();
    	EntityTransaction et = entityManager.getTransaction();
    	et.begin();
        Person person = new Person();
        person.initIdUid(id, null);
        person.setLastName(lastName);
        entityManager.persist(person);
        entityManager.flush();
        et.commit();
    }
}
