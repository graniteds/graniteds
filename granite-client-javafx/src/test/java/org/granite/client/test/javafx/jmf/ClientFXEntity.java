/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2014 GRANITE DATA SERVICES S.A.S.
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
package org.granite.client.test.javafx.jmf;

import java.io.Serializable;

import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;

import org.granite.client.javafx.persistence.collection.FXPersistentCollections;
import org.granite.client.messaging.RemoteAlias;
import org.granite.client.persistence.Entity;
import org.granite.client.persistence.Id;
import org.granite.client.persistence.Uid;
import org.granite.client.persistence.Version;

@Entity
@RemoteAlias("org.granite.client.test.javafx.jmf.ServerEntity")
public class ClientFXEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unused")
	private boolean __initialized__ = true;
	@SuppressWarnings("unused")
	private String __detachedState__ = null;
	
	@Id
	private ReadOnlyObjectWrapper<Integer> id = new ReadOnlyObjectWrapper<Integer>(this, "id", null);
	
	@Uid
	private StringProperty uid = new SimpleStringProperty(this, "uid", null);
	
	@Version
	private ReadOnlyObjectWrapper<Integer> version = new ReadOnlyObjectWrapper<Integer>(this, "version", null);

	private StringProperty name = new SimpleStringProperty(this, "name", null);
	
	private ReadOnlyListWrapper<ClientFXCollectionEntity> list = FXPersistentCollections.readOnlyObservablePersistentList(this, "list");

	public ClientFXEntity() {
	}

	public ClientFXEntity(Integer id, Integer version) {
		this.id.set(id);
		this.version.set(version);
	}

	public ReadOnlyObjectProperty<Integer> idProperty() {
		return id.getReadOnlyProperty();
	}
	public Integer getId() {
		return id.get();
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

	public ReadOnlyObjectProperty<Integer> versionProperty() {
		return version.getReadOnlyProperty();
	}
	public Integer getVersion() {
		return version.get();
	}

	public StringProperty nameProperty() {
		return uid;
	}
	public String getName() {
		return name.get();
	}
	public void setName(String name) {
		this.name.set(name);
	}

	public ReadOnlyListProperty<ClientFXCollectionEntity> listProperty() {
		return list.getReadOnlyProperty();
	}
	public ObservableList<ClientFXCollectionEntity> getList() {
		return list.get();
	}
}
