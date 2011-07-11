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

package org.granite.example.addressbook.spring.service.jpa;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.granite.example.addressbook.entity.Contact;
import org.granite.example.addressbook.entity.Person;
import org.granite.example.addressbook.spring.service.ObserveAllPublishAll;
import org.granite.example.addressbook.spring.service.PersonService;
import org.granite.tide.data.DataEnabled;
import org.granite.tide.data.DataEnabled.PublishMode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@DataEnabled(topic="addressBookTopic", params=ObserveAllPublishAll.class, publish=PublishMode.ON_SUCCESS, auto=false)
public class JPAPersonService implements PersonService {

	@PersistenceContext
    protected EntityManager entityManager;
    
    
    @Transactional
    public Person createPerson(Person person) {
    	entityManager.persist(person);
    	entityManager.flush();
    	return person;
    }
    
    @Transactional
    public void create100Persons() {
    	for (int i = 0; i < 100; i++) {
    		Person p = new Person();
    		p.setFirstName("FirstName" + i);
    		p.setLastName("LastName" + i);
    		entityManager.persist(p);
    	}
    	entityManager.flush();
    }

    @Transactional
    public Person modifyPerson(Person person) {
        Person p = (Person)entityManager.merge(person);
        for (Contact c : p.getContacts())
        	c.setPerson(p);
        entityManager.flush();
        return p;
    }

    @Transactional
    public void deletePerson(Integer personId) {
        Person person = (Person)entityManager.find(Person.class, personId);
        entityManager.remove(person);
        entityManager.flush();
    }
    
    @Transactional(readOnly=true)
    public Map<String, Object> findPersons(Person examplePerson, 
    		int first, int max, String[] order, boolean[] desc) {
        String from = "from Person e ";
        String where = "where lower(e.lastName) like :lastName ";
        String orderBy = "";
        if (order != null && order.length > 0) {
        	orderBy = "order by ";
        	for (int idx = 0; idx < order.length; idx++) {
        		if (idx > 0)
        			orderBy += ", ";
        		orderBy += "e." + order[idx] + (desc[idx] ? " desc" : "");
        	}
        }
        String lastName = examplePerson.getLastName() != null ? examplePerson.getLastName() : "";
        Query qc = entityManager.createQuery("select count(e) " + from + where);
        qc.setParameter("lastName", "%" + lastName.toLowerCase() + "%");
        long resultCount = (Long)qc.getSingleResult();
        if (max == 0)
            max = 36;
        Query ql = entityManager.createQuery("select e " + from + where + orderBy);
        ql.setFirstResult(first);
        ql.setMaxResults(max);
        ql.setParameter("lastName", "%" + lastName.toLowerCase() + "%");
        List<?> resultList = ql.getResultList();
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("resultCount", resultCount);
        result.put("resultList", resultList);
        entityManager.flush();
        return result;
    }
}
