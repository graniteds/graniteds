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
package org.granite.client.test.javafx.jmf;

import java.io.Serializable;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import org.granite.client.persistence.Entity;
import org.granite.client.persistence.Id;
import org.granite.client.persistence.Uid;
import org.granite.client.persistence.Version;
import org.granite.messaging.annotations.Serialized;

@Entity
@Serialized(propertiesOrder={ "__initialized__", "__detachedState__", "id", "uid", "version" })
public abstract class ClientBaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unused")
    private boolean __initialized__ = true;
    @SuppressWarnings("unused")
	private String __detachedState__ = null;
    
    @Id
	private ObjectProperty<Long> id = new SimpleObjectProperty<Long>(this, "id");
    @Uid
	private StringProperty uid = new SimpleStringProperty(this, "uid");
    @Version
	private ObjectProperty<Integer> version = new SimpleObjectProperty<Integer>(this, "version");
	
	public ObjectProperty<Long> idProperty() {
		return id;
	}
    public void setId(Long value) {
        id.set(value);
    }
    public Long getId() {
        return id.get();
    }

	public StringProperty uidProperty() {
		return uid;
	}
    public void setUid(String value) {
    	uid.set(value);
    }
    public String getUid() {
        return uid.get();
    }
    
	public ObjectProperty<Integer> versionProperty() {
		return version;
	}
    public void setVersion(Integer value) {
        version.set(value);
    }
    public Integer getVersion() {
        return version.get();
    }
}
