/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of Granite Data Services.
 *
 *   Granite Data Services is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU Library General Public License as published by
 *   the Free Software Foundation; either version 2 of the License, or (at your
 *   option) any later version.
 *
 *   Granite Data Services is distributed in the hope that it will be useful, but
 *   WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *   FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
 *   for more details.
 *
 *   You should have received a copy of the GNU Library General Public License
 *   along with this library; if not, see <http://www.gnu.org/licenses/>.
 */
package org.granite.test.tide.seam.action;

import javax.persistence.EntityManager;

import org.granite.test.tide.data.Person;
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
	
    public void create(long id, String lastName) {
        Person person = new Person();
        person.initIdUid(id, null);
        person.setLastName(lastName);
        entityManager.persist(person);
    }
}
