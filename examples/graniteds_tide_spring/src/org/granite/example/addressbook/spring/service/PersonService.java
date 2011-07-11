package org.granite.example.addressbook.spring.service;

import java.util.Map;

import org.granite.example.addressbook.entity.Person;
import org.granite.messaging.service.annotations.RemoteDestination;



@RemoteDestination
public interface PersonService {

	public Person createPerson(Person person);

	public void create100Persons();

	public Person modifyPerson(Person person);
	
	public void deletePerson(Integer personId);

	public Map<String, Object> findPersons(Person filter, int first,
			int max, String[] order, boolean[] desc);

}