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
package org.granite.test.tide.data;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;

/**
 * @author Franck WOLFF
 */
@Entity
public class Person4 extends LegalEntity {

    private static final long serialVersionUID = 1L;

    public enum Salutation {
        Mr,
        Ms,
        Dr
    }
    
    public Person4() {
    }
    
    public Person4(Long id, Long version, String uid) {
    	super(id, version, uid);
    	contacts = new HashSet<Contact4>();
    }
    
    @Enumerated(EnumType.ORDINAL)
    private Salutation salutation;

    @Basic
    private String firstName;

    @Basic
    private String lastName;

    @OneToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY, mappedBy="person", orphanRemoval=true)
    private Set<Contact4> contacts = new HashSet<Contact4>();

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

    public Set<Contact4> getContacts() {
        return contacts;
    }
    public void setContacts(Set<Contact4> contacts) {
        this.contacts = contacts;
    }
}
