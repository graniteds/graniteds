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
package org.granite.test.tide.hibernate4.data.client;

import org.granite.client.messaging.RemoteAlias;
import org.granite.client.persistence.Entity;


/**
 * @author Franck WOLFF
 */
@Entity
@RemoteAlias("org.granite.test.tide.hibernate4.data.KeyValue")
public class ClientKeyValue extends AbstractClientEntity {

	private static final long serialVersionUID = 1L;

	
    public ClientKeyValue() {
    }

    public ClientKeyValue(Long id, Long version, String uid, String key, String value) {
    	super(id, version, uid);
    	this.key = key;
    	this.value = value;
    }

    private String key;
    private String value;

    
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
}
