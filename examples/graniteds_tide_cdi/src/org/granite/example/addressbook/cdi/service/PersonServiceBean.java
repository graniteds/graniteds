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

package org.granite.example.addressbook.cdi.service;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.granite.context.GraniteContext;
import org.granite.example.addressbook.entity.Person;
import org.granite.messaging.webapp.HttpGraniteContext;
import org.granite.tide.data.DataEnabled;
import org.granite.tide.data.DataEnabled.PublishMode;



@Transactional
@DataEnabled(topic="addressBookTopic", publish=PublishMode.ON_COMMIT, useInterceptor=true)
public class PersonServiceBean implements PersonService {

	@Inject
    protected EntityManager entityManager;

    
    public Person createPerson(Person person) {
    	person.setCreatedBy(((HttpGraniteContext)GraniteContext.getCurrentInstance()).getRequest().getUserPrincipal().getName());
		entityManager.persist(person);
		return person;
    }

    public Person modifyPerson(Person person) {
		return (Person)entityManager.merge(person);
    }

    public void deletePerson(Integer personId) {
        Person person = (Person)entityManager.find(Person.class, personId);
        entityManager.remove(person);
    }
}
