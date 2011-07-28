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

package org.granite.example.addressbook.ejb.service;

import java.util.List;

import org.granite.example.addressbook.entity.Contact;
import org.granite.example.addressbook.entity.Country;
import org.granite.example.addressbook.entity.Person;


/**
 * @author Franck WOLFF
 */
public interface PersonService {

    public List<Person> findAllPersons();
    public List<Person> findAllPersons(String name);
    public List<Country> findAllCountries();

    public Person createPerson(Person person);
    public Person modifyPerson(Person person);
    public void deletePerson(Person person);

    public void deleteContact(Contact contact);
}
