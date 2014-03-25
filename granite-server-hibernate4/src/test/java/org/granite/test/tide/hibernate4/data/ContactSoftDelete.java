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
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.SQLDelete;


/**
 * @author Franck WOLFF
 */
@Entity
@Table(name="contactsoftdelete")
@Filter(name="filterSoftDelete", condition="deleted = 0")
@SQLDelete(sql="update contactsoftdelete set deleted = 1 where id = ? and version = ?")
public class ContactSoftDelete extends AbstractEntitySoftDelete {

    private static final long serialVersionUID = 1L;


    public ContactSoftDelete() {
    }
    
    public ContactSoftDelete(Long id, Long version, String uid) {
    	super(id, version, uid);
    	phones = new HashSet<PhoneSoftDelete>();
    }
    
    @ManyToOne(optional=false)
    private PersonSoftDelete person;

    @Basic
    private String phone;
    @Basic
    private String mobile;
    @Basic
    private String fax;
    @Basic
    private String email;

    @ManyToOne(cascade=CascadeType.ALL, optional=false)
    private AddressSoftDelete address;
    
    @SuppressWarnings("deprecation")
	@Cascade(org.hibernate.annotations.CascadeType.DELETE_ORPHAN)
    @OneToMany(cascade=CascadeType.ALL)
    @Filter(name="filterSoftDelete", condition="deleted = 0")
    private Set<PhoneSoftDelete> phones;


    public PersonSoftDelete getPerson() {
        return person;
    }
    public void setPerson(PersonSoftDelete person) {
        this.person = person;
    }

    public AddressSoftDelete getAddress() {
        return address;
    }
    public void setAddress(AddressSoftDelete address) {
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

    public Set<PhoneSoftDelete> getPhones() {
        return phones;
    }
    public void setPhones(Set<PhoneSoftDelete> phones) {
        this.phones = phones;
    }
}
