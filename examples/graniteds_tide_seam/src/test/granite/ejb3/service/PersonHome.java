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


import org.granite.tide.annotations.TideEnabled;
import org.granite.tide.data.DataEnabled;
import org.granite.tide.data.DataEnabled.PublishMode;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.End;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.framework.EntityHome;

import test.granite.ejb3.entity.Person;


@Name("personHome")
@Restrict("#{identity.loggedIn}")
@TideEnabled
@DataEnabled(topic="addressBookTopic", params=AddressBookParams.class, publish=PublishMode.ON_SUCCESS)
public class PersonHome extends EntityHome<Person> {

    private static final long serialVersionUID = 1L;
    
    
    @Override
    @Begin(join=true)
    public void create() {
        super.create();
    }
    
    @Override
    @Transactional
    @End
    public String persist() {
        return super.persist();
    }
    
    @Override
    @Restrict("#{s:hasRole('admin') or s:hasPermission(personHome.instance, 'update')}")
    @Transactional
    @End
    public String update() {
        return super.update();
    }
    
    @Override
    @Restrict("#{s:hasRole('admin')}")
    @Transactional
    @End
    public String remove() {
        return super.remove();
    }
}
