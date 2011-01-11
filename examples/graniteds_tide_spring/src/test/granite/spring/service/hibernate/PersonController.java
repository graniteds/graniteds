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

package test.granite.spring.service.hibernate;

import java.util.List;

import org.granite.tide.data.DataEnabled;
import org.granite.tide.data.DataEnabled.PublishMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import test.granite.spring.entity.Person;
import test.granite.spring.service.ObserveAllPublishAll;

@Controller("personController")
@Transactional
@DataEnabled(topic="addressBookTopic", params=ObserveAllPublishAll.class, publish=PublishMode.ON_SUCCESS)
public class PersonController {

    protected SessionFactory sessionFactory;
    
    
    @Autowired
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
    
    
    protected Session getSession() {
        return sessionFactory.getCurrentSession();
    }

    
    @Transactional
    @RequestMapping("/person/create")
    public ModelMap createPerson(@RequestParam("person") Person person) {
        getSession().persist(person);
        getSession().flush();
        return new ModelMap("person", person);
    }
    
    @Transactional
    @RequestMapping("/person/create100")
    public void create100Persons() {
    	for (int i = 0; i < 100; i++) {
    		Person p = new Person();
    		p.setFirstName("FirstName" + i);
    		p.setLastName("LastName" + i);
            getSession().save(p);
    	}
        getSession().flush();
    }

    @Transactional
    @RequestMapping("/person/update")
    public ModelMap modifyPerson(@RequestParam("person") Person person) {
        Person p = (Person)getSession().merge(person);
        getSession().flush();
        return new ModelMap("person", p);
    }

    @Transactional
    @RequestMapping("/person/remove")
    public void deletePerson(@RequestParam("personId") Integer personId) {
        Person person = (Person)getSession().load(Person.class, personId);
        getSession().delete(person);
        getSession().flush();
    }
    
    @Transactional(readOnly=true)
    @RequestMapping("/person/find")
    public ModelMap findPersons(@RequestParam("filter") Person filter, 
    		@RequestParam("first") int first, 
    		@RequestParam("max") int max, 
    		@RequestParam("order") String[] order, 
    		@RequestParam("desc") boolean[] desc) {
        String from = "from test.granite.spring.entity.Person e ";
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
        String lastName = filter.getLastName() != null ? filter.getLastName() : "";
        Query qc = getSession().createQuery("select count(e) " + from + where);
        qc.setParameter("lastName", "%" + lastName.toLowerCase() + "%");
        long resultCount = (Long)qc.uniqueResult();
        if (max == 0)
            max = 36;
        Query ql = getSession().createQuery("select e " + from + where + orderBy);
        ql.setFirstResult(first);
        ql.setMaxResults(max);
        ql.setParameter("lastName", "%" + lastName.toLowerCase() + "%");
        List<?> resultList = ql.list();
        ModelMap result = new ModelMap();
        result.addAttribute("resultCount", resultCount);
        result.addAttribute("resultList", resultList);
        return result;
    }
}
