package org.granite.test.tide.seam.action;

import javax.persistence.EntityManager;

import org.granite.test.tide.seam.entity.Person;
import org.granite.tide.data.DataEnabled;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;


@Name("personAction")
@Scope(ScopeType.EVENT)
@Transactional
@DataEnabled(topic="")
public class PersonAction {
    
	@In
	private EntityManager entityManager;
	
    public void create(String lastName) {
        Person person = new Person();
        person.initIdUid(12, null);
        person.setLastName(lastName);
        entityManager.persist(person);
    }
}
