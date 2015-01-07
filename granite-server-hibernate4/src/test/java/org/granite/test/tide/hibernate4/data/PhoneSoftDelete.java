/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2015 GRANITE DATA SERVICES S.A.S.
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

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.SQLDelete;


/**
 * @author Franck WOLFF
 */
@Entity
@Table(name="phonesoftdelete")
@Filter(name="filterSoftDelete", condition="deleted = 0")
@SQLDelete(sql="update phonesoftdelete set deleted = 1 where id = ? and version = ?")
public class PhoneSoftDelete extends AbstractEntitySoftDelete {

    private static final long serialVersionUID = 1L;


    public PhoneSoftDelete() {
    }
    
    public PhoneSoftDelete(Long id, Long version, String uid) {
    	super(id, version, uid);
    }
    
    @ManyToOne(optional=false)
    private ContactSoftDelete contact;

    @Basic
    private String phone;


    public ContactSoftDelete getContact() {
        return contact;
    }
    public void setContact(ContactSoftDelete contact) {
        this.contact = contact;
    }

    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }
}
