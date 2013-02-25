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

	
    public void create(String lastName) {
    	EntityManager entityManager = entityManagerFactory.createEntityManager();
    	EntityTransaction et = entityManager.getTransaction();
    	et.begin();
        Person person = new Person();
        person.initIdUid(12, null);
        person.setLastName(lastName);
        entityManager.persist(person);
        entityManager.flush();
        et.commit();
    }
}
