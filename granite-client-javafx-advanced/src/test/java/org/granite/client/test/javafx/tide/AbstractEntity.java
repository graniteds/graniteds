/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2015 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of the Granite Data Services Platform.
 *
 *                               ***
 *
 *   Community License: GPL 3.0
 *
 *   This file is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published
 *   by the Free Software Foundation, either version 3 of the License,
 *   or (at your option) any later version.
 *
 *   This file is distributed in the hope that it will be useful, but
 *   WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *                               ***
 *
 *   Available Commercial License: GraniteDS SLA 1.0
 *
 *   This is the appropriate option if you are creating proprietary
 *   applications and you are not prepared to distribute and share the
 *   source code of your application under the GPL v3 license.
 *
 *   Please visit http://www.granitedataservices.com/license for more
 *   details.
 */
package org.granite.client.test.javafx.tide;

import java.io.Serializable;
import java.util.UUID;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import org.granite.client.persistence.Entity;
import org.granite.client.persistence.Id;
import org.granite.client.persistence.Uid;
import org.granite.client.persistence.Version;


@Entity
public abstract class AbstractEntity implements Serializable {

	private static final long serialVersionUID = 1L;
    
    @SuppressWarnings("unused")
	private boolean __initialized__ = true;
    @SuppressWarnings("unused")
	private String __detachedState__ = null;

    @Id
    private final ReadOnlyObjectWrapper<Long> id = new ReadOnlyObjectWrapper<Long>(this, "id", null);
    @Version
    private final ReadOnlyObjectWrapper<Long> version = new ReadOnlyObjectWrapper<Long>(this, "version", null);
    @Uid
    private final StringProperty uid = new SimpleStringProperty(this, "uid", null);
    
    
    public AbstractEntity() {        
        this.uid.set(UUID.randomUUID().toString().toUpperCase());
    }
    
    public AbstractEntity(Long id, boolean initialized, String detachedState) {
        if (initialized) {
            this.uid.set(UUID.randomUUID().toString().toUpperCase());
        }
        else {
            this.id.set(id);
            __initialized__ = false;
            __detachedState__ = detachedState;
        }
    }
    
    public AbstractEntity(Long id, Long version, String uid) {
        this.id.set(id);
        this.version.set(version);
        this.uid.set(uid);
    }
    
    public void resetId(Long id, Long version) {
    	this.id.set(id);
    	this.version.set(version);
    }
    
    public ReadOnlyObjectProperty<Long> idProperty() {
        return id.getReadOnlyProperty();
    }        
    public Long getId() {
        return id.get();
    }
    
    public ReadOnlyObjectProperty<Long> versionProperty() {
        return version.getReadOnlyProperty();
    }        
    public Long getVersion() {
        return version.get();
    }
    
    public StringProperty uidProperty() {
        return uid;
    }    
    public String getUid() {
        return uid.get();
    }
    public void setUid(String uid) {
        this.uid.set(uid);
    }
    
    @Override
    public boolean equals(Object o) {
        return (o == this || (o instanceof AbstractEntity && uid.get().equals(((AbstractEntity)o).getUid())));
    }
    
    @Override
    public int hashCode() {
        return uid.get().hashCode();
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + ":" + getId() + ":" + getVersion() + ":" + getUid();
    }
}
