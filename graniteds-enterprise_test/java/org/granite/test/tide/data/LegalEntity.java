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

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;

/**
 * @author Franck WOLFF
 */
@Entity
@Inheritance(strategy=InheritanceType.JOINED)
public class LegalEntity extends AbstractEntity {

	private static final long serialVersionUID = 1L;
	
    public LegalEntity() {
    }
    
    public LegalEntity(Long id, Long version, String uid) {
    	super(id, version, uid);
    }
    
    @OneToMany(cascade=CascadeType.ALL, orphanRemoval=true)
    private List<Phone4> phones;    
    
    public List<Phone4> getPhones() {
        return phones;
    }
    public void setPhones(List<Phone4> phones) {
        this.phones = phones;
    }
}
