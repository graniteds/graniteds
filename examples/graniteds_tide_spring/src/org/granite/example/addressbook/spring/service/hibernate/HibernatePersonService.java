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

package org.granite.example.addressbook.spring.service.hibernate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
//import java.util.Set;

//import org.granite.tide.data.ChangeSet;
//import org.granite.tide.data.ChangeSetApplier;
import org.granite.example.addressbook.entity.Person;
import org.granite.example.addressbook.spring.service.ObserveAllPublishAll;
import org.granite.example.addressbook.spring.service.PersonService;
import org.granite.tide.data.DataEnabled;
import org.granite.tide.data.DataEnabled.PublishMode;
// import org.granite.tide.hibernate.HibernatePersistenceAdapter;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
@DataEnabled(topic="addressBookTopic", params=ObserveAllPublishAll.class, publish=PublishMode.ON_SUCCESS, auto=false)
public class HibernatePersonService implements PersonService {

	@Autowired
    protected SessionFactory sessionFactory;
    
    
    protected Session getSession() {
        return sessionFactory.getCurrentSession();
    }

    
    /* (non-Javadoc)
	 * @see test.granite.spring.service.hibernate.PersonService#createPerson(test.granite.spring.entity.Person)
	 */
	@Transactional
    public Person createPerson(Person person) {
        getSession().persist(person);
        getSession().flush();
        return person;
    }
    
    /* (non-Javadoc)
	 * @see test.granite.spring.service.hibernate.PersonService#create100Persons()
	 */
	@Transactional
    public void create100Persons() {
    	for (int i = 0; i < 100; i++) {
    		Person p = new Person();
    		p.setFirstName("FirstName" + i);
    		p.setLastName("LastName" + i);
            getSession().save(p);
    	}
        getSession().flush();
    }

    /* (non-Javadoc)
	 * @see test.granite.spring.service.hibernate.PersonService#modifyPerson(test.granite.spring.entity.Person)
	 */
	@Transactional
    public Person modifyPerson(Person person) {
        Person p = (Person)getSession().merge(person);
        getSession().flush();
        return p;
    }
    

    /* (non-Javadoc)
	 * @see test.granite.spring.service.hibernate.PersonService#deletePerson(java.lang.Integer)
	 */
	@Transactional
    public void deletePerson(Integer personId) {
        Person person = (Person)getSession().load(Person.class, personId);
        getSession().delete(person);
        getSession().flush();
    }
    
    /* (non-Javadoc)
	 * @see test.granite.spring.service.hibernate.PersonService#findPersons(test.granite.spring.entity.Person, int, int, java.lang.String[], boolean[])
	 */
	@Transactional(readOnly=true)
    public Map<String, Object> findPersons(Person filter, 
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
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("resultCount", resultCount);
        result.put("resultList", resultList);
        return result;
    }
}
