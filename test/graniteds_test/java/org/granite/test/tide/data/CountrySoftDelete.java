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

package org.granite.test.tide.data;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.SQLDelete;

/**
 * @author Franck WOLFF
 */
@Entity
@Table(name="countrysoftdelete")
@Filter(name="filterSoftDelete", condition="deleted = 0")
@SQLDelete(sql="update countrysoftdelete set deleted = 1 where id = ? and version = ?")
public class CountrySoftDelete extends AbstractEntitySoftDelete {

    private static final long serialVersionUID = 1L;


    public CountrySoftDelete() {
    }
    
    public CountrySoftDelete(Long id, Long version, String uid) {
    	super(id, version, uid);
    }
    
    @Basic
    private String name;


    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
}
