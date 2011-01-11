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

import java.util.Set;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.security.Identity;
import org.jboss.seam.security.permission.PermissionResolver;

import test.granite.ejb3.entity.AbstractEntity;


@Name("test.granite.permissionResolver")
public class CreatedByPermissionResolver implements PermissionResolver {

    public void filterSetByAction(Set<Object> set, String action) {
    }

    public boolean hasPermission(Object target, String action) {
        if (target instanceof AbstractEntity && ("update".equals(action) || "delete".equals(action) || ((AbstractEntity)target).isRestricted()))
            return Identity.instance().getCredentials().getUsername().equals(((AbstractEntity)target).getCreatedBy());
        return false;
    }

}
