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

package org.granite.example.addressbook.seam.service;

import static org.jboss.seam.ScopeType.SESSION;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.granite.tide.annotations.TideEnabled;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.security.Identity;

@SuppressWarnings("all")

@Name("authenticator")
public class Authenticator {
	
	@In
	private Identity identity;
	
	
	public boolean authenticate() {
		if ("admin".equals(identity.getCredentials().getUsername()) && "admin".equals(identity.getCredentials().getPassword())) {
			identity.addRole("admin");
			return true;
		}
		if ("user".equals(identity.getCredentials().getUsername()) && "user".equals(identity.getCredentials().getPassword())) {
			identity.addRole("user");
			return true;
		}
		return false;
   }

}
