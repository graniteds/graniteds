package org.granite.test.tide.spring.service;

import org.granite.test.tide.data.Person;
import org.granite.tide.data.DataEnabled;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
@DataEnabled
public class HibernatePersonService {
	
	private SessionFactory sessionFactory;
	
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
    
    public void create(String lastName) {
        Person person = new Person();
        person.initIdUid(12, null);
        person.setLastName(lastName);
        sessionFactory.getCurrentSession().persist(person);
    }
}
