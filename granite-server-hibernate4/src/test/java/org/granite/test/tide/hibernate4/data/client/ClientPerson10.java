/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2014 GRANITE DATA SERVICES S.A.S.
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
package org.granite.test.tide.hibernate4.data.client;

import java.util.ArrayList;
import java.util.List;

import org.granite.client.messaging.RemoteAlias;
import org.granite.client.persistence.Entity;

@Entity
@RemoteAlias("org.granite.test.tide.hibernate4.data.Person10")
public class ClientPerson10 extends AbstractClientEntity {
    
    private static final long serialVersionUID = 1L;

    public ClientPerson10() {
    }
    
    public ClientPerson10(Long id, Long version, String uid) {
    	super(id, version, uid);
    	parameters = new ArrayList<ClientKeyValue>();
    }
    
    private String firstName;
    
    private String lastName;
    
	private List<ClientKeyValue> parameters = new ArrayList<ClientKeyValue>();

    
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
    
	public List<ClientKeyValue> getParameters() {
		return parameters;
	}
	public void setParameters(List<ClientKeyValue> parameters) {
		this.parameters = parameters;
	}
    
}
