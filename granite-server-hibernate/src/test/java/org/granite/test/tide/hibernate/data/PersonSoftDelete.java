/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of the Granite Data Services Platform.
 *
 *   Granite Data Services is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Lesser General Public
 *   License as published by the Free Software Foundation; either
 *   version 2.1 of the License, or (at your option) any later version.
 *
 *   Granite Data Services is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 *   General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public
 *   License along with this library; if not, write to the Free Software
 *   Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301,
 *   USA, or see <http://www.gnu.org/licenses/>.
 */
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

package org.granite.test.tide.hibernate.data;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.granite.messaging.annotations.Include;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.SQLDelete;

/**
 * @author Franck WOLFF
 */
@Entity
@Table(name="personsoftdelete")
@Filter(name="filterSoftDelete", condition="deleted = 0")
@SQLDelete(sql="update personsoftdelete set deleted = 1 where id = ? and version = ?")
public class PersonSoftDelete extends AbstractEntitySoftDelete {

    private static final long serialVersionUID = 1L;

    public enum Salutation {
        Mr,
        Ms,
        Dr
    }
    
    public PersonSoftDelete() {
    }
    
    public PersonSoftDelete(Long id, Long version, String uid) {
    	super(id, version, uid);
    	contacts = new HashSet<ContactSoftDelete>();
    }
    
    @Enumerated(EnumType.ORDINAL)
    private Salutation salutation;

    @Basic
    private String firstName;

    @Basic
    private String lastName;

    @SuppressWarnings("deprecation")
	@OneToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY, mappedBy="person")
    @Cascade({org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN})
    @Filter(name="filterSoftDelete", condition="deleted = 0")
    private Set<ContactSoftDelete> contacts = new HashSet<ContactSoftDelete>();

    @OneToOne(cascade=CascadeType.ALL, fetch=FetchType.EAGER)
    private ContactSoftDelete mainContact;

    public Salutation getSalutation() {
		return salutation;
	}
	public void setSalutation(Salutation salutation) {
		this.salutation = salutation;
	}

	public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Set<ContactSoftDelete> getContacts() {
        return contacts;
    }
    public void setContacts(Set<ContactSoftDelete> contacts) {
        this.contacts = contacts;
    }

    public ContactSoftDelete getMainContact() {
        return mainContact;
    }
    public void setMainContact(ContactSoftDelete mainContact) {
        this.mainContact = mainContact;
    }


    @Include
    public String getFullName() {
        StringBuilder sb = new StringBuilder();
        if (firstName != null && firstName.length() > 0)
            sb.append(firstName);
        if (lastName != null && lastName.length() > 0) {
            if (sb.length() > 0)
                sb.append(' ');
            sb.append(lastName);
        }
        return sb.toString();
    }
    
    public void setFullName(String fullName) {
    }
}
