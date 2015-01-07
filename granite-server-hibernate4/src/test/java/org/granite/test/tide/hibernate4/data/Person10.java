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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.granite.test.tide.data.AbstractEntity;

@Entity
@Table(name="PERSON")
public class Person10 extends AbstractEntity {
    
    private static final long serialVersionUID = 1L;

    public Person10() {
    }
    
    public Person10(Long id, Long version, String uid) {
    	super(id, version, uid);
    	parameters = new ArrayList<KeyValue>();
    }
    
    @Basic
    private String firstName;
    
    @Basic
    private String lastName;
    
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL,orphanRemoval=true)	
	private List<KeyValue> parameters = new ArrayList<KeyValue>();

    
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
    
	public List<KeyValue> getParameters() {
		return parameters;
	}
	public void setParameters(List<KeyValue> parameters) {
		this.parameters = parameters;
	}
    
}
