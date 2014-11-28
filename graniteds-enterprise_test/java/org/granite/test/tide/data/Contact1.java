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

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;


/**
 * @author Franck WOLFF
 */
@Entity
public class Contact1 extends AbstractEntity {

    private static final long serialVersionUID = 1L;


    public Contact1() {
    	setPhones(new HashSet<Phone>());
    }
    
    public Contact1(Long id, Long version, String uid) {
    	super(id, version, uid);
    	setPhones(new HashSet<Phone>());
    }
    
    @ManyToOne(optional=false)
    private Person1 person;

    @Basic
    private String phone;
    @Basic
    private String mobile;
    @Basic
    private String fax;
    @Basic
    private String email;

    @ManyToOne(cascade=CascadeType.ALL, optional=false)
    private Address address;
    
    @OneToMany(cascade=CascadeType.ALL, orphanRemoval=true)
    private Set<Phone> phones;


    public Person1 getPerson() {
        return person;
    }
    public void setPerson(Person1 person) {
        this.person = person;
    }

    public Address getAddress() {
        return address;
    }
    public void setAddress(Address address) {
        this.address = address;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public String getFax() {
        return fax;
    }
    public void setFax(String fax) {
        this.fax = fax;
    }

    public String getMobile() {
        return mobile;
    }
    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Set<Phone> getPhones() {
        return phones;
    }
    public void setPhones(Set<Phone> phones) {
        this.phones = phones;
    }
}
