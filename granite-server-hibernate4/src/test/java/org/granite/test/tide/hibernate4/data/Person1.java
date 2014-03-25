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
package org.granite.test.tide.hibernate4.data;

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

import org.granite.test.tide.data.AbstractEntity;
import org.hibernate.annotations.Cascade;

@Entity
@Table(name="PERSON")
public class Person1 extends AbstractEntity { // , DocumentedEntity {
    
    private static final long serialVersionUID = 1L;

    public enum Salutation {
        Mr,
        Ms,
        Mlle,
        Dr
    }
    
    public Person1() {
    }
    
    public Person1(Long id, Long version, String uid) {
    	super(id, version, uid);
    	contacts = new HashSet<Contact1>();
    }
    
    @Enumerated(EnumType.ORDINAL)
    private Salutation salutation;

    @Basic
    private String firstName;
    
    @Basic
    private String lastName;
    
    @SuppressWarnings("deprecation")
	@OneToMany(fetch=FetchType.LAZY, mappedBy="person", orphanRemoval=true)
    @Cascade({org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN})
    private Set<Contact1> contacts = new HashSet<Contact1>();
    
    @OneToOne(cascade=CascadeType.ALL, fetch=FetchType.EAGER)
    private Contact1 mainContact;

/*    @Embedded
    private Document document;
*/    

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
    
    public Set<Contact1> getContacts() {
        return contacts;
    }
    public void setContacts(Set<Contact1> contacts) {
        this.contacts = contacts;
    }
    
    public Contact1 getMainContact() {
        return mainContact;
    }
    public void setMainContact(Contact1 mainContact) {
        this.mainContact = mainContact;
    }

/*    public Document getDocument() {
		return document;
	}
	public void setDocument(Document document) {
		this.document = document;
	}
*/
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
}
