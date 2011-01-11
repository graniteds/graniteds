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

package test.granite.ejb3.service;

import java.util.List;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.persistence.Query;

import org.jboss.annotation.security.SecurityDomain;

import org.granite.messaging.service.annotations.RemoteDestination;

import test.granite.ejb3.entity.Contact;
import test.granite.ejb3.entity.Country;
import test.granite.ejb3.entity.Person;

/**
 * @author Franck WOLFF
 */
@Stateless
@Local(PersonService.class)
@SecurityDomain("other")
@RemoteDestination(id="person", securityRoles={"user","admin"})
public class PersonServiceBean extends AbstractEntityService implements PersonService {

    public PersonServiceBean() {
        super();
    }

    public List<Person> findAllPersons() {
        return findAll(Person.class);
    }

    @SuppressWarnings("unchecked")
    public List<Person> findAllPersons(String name) {
        Query query = manager.createQuery(
            "select distinct p from Person p " +
            "where upper(p.firstName) like upper('%" + name + "%') or upper(p.lastName) like upper('%" + name + "%')"
        );
        return query.getResultList();
    }

    public List<Country> findAllCountries() {
        return findAll(Country.class);
    }

    @RolesAllowed({"admin"})
    public Person createPerson(Person person) {
        return manager.merge(person);
    }

    @RolesAllowed({"admin"})
    public Person modifyPerson(Person person) {
        return manager.merge(person);
    }

    @RolesAllowed({"admin"})
    public void deleteContact(Contact contact) {
        contact = manager.find(Contact.class, contact.getId());
        contact.getPerson().getContacts().remove(contact);
        if (contact.equals(contact.getPerson().getMainContact())) {
            Set<Contact> contacts = contact.getPerson().getContacts();
            contact.getPerson().setMainContact(contacts.isEmpty() ? null : contacts.iterator().next());
        }
        manager.remove(contact);
    }

    @RolesAllowed({"admin"})
    public void deletePerson(Person person) {
        person = manager.find(Person.class, person.getId());
        manager.remove(person);
    }
}
