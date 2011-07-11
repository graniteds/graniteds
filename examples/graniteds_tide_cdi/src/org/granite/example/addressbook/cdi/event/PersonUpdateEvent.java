package org.granite.example.addressbook.cdi.event;

import org.granite.example.addressbook.entity.Person;
import org.granite.tide.annotations.TideEvent;



@TideEvent
public class PersonUpdateEvent {
	
	private Person person;
	
	public PersonUpdateEvent(Person person) {
		this.person = person;
	}

	public Person getPerson() {
		return person;
	}
}
