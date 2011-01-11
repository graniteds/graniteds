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

package test.granite.spring.service.jpa;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.granite.tide.data.DataEnabled;
import org.granite.tide.data.DataEnabled.PublishMode;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import test.granite.spring.entity.Contact;
import test.granite.spring.entity.Person;
import test.granite.spring.service.ObserveAllPublishAll;

@Controller("personController")
@DataEnabled(topic="addressBookTopic", params=ObserveAllPublishAll.class, publish=PublishMode.ON_SUCCESS)
public class PersonController {

	@PersistenceContext
    protected EntityManager entityManager;
    
    
    @Transactional
    @RequestMapping("/person/create")
    public void createPerson(@RequestParam("person") Person person) {
    	entityManager.persist(person);
    	entityManager.flush();
    }
    
    @Transactional
    @RequestMapping("/person/create100")
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
    @RequestMapping("/person/update")
    public void modifyPerson(@RequestParam("person") Person person) {
        Person p = (Person)entityManager.merge(person);
        // Workaround for possible DataNucleus bug, not necessary for other JPA providers !!!
        for (Contact c : p.getContacts()) {
        	if (c.getPerson() != p)
        		c.setPerson(p);
        }
        // End of workaround for DataNucleus
        entityManager.flush();
    }

    @Transactional
    @RequestMapping("/person/remove")
    public void deletePerson(@RequestParam("personId") Integer personId) {
        Person person = (Person)entityManager.find(Person.class, personId);
        entityManager.remove(person);
        entityManager.flush();
    }
    
    @Transactional(readOnly=true)
    @RequestMapping("/person/find")
    public ModelMap findPersons(@RequestParam("filter") Person examplePerson, 
    		@RequestParam("first") int first, 
    		@RequestParam("max") int max, 
    		@RequestParam("order") String[] order, 
    		@RequestParam("desc") boolean[] desc) {
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
        ModelMap result = new ModelMap();
        result.addAttribute("resultCount", resultCount);
        result.addAttribute("resultList", resultList);
        entityManager.flush();
        return result;
    }
}
