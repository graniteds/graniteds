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
package org.granite.client.test.javafx;

import java.io.Serializable;

import javafx.beans.property.ReadOnlySetProperty;
import javafx.beans.property.ReadOnlySetWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableSet;

import org.granite.client.javafx.persistence.collection.FXPersistentCollections;
import org.granite.client.messaging.RemoteAlias;
import org.granite.client.persistence.Entity;
import org.granite.messaging.annotations.Serialized;


@Entity
@Serialized
@RemoteAlias("org.granite.client.test.javafx.Entity1b")
public class FXEntity1b implements Serializable {

	private static final long serialVersionUID = 1L;
	
    @SuppressWarnings("unused")
    private boolean __initialized__ = true;
    @SuppressWarnings("unused")
	private String __detachedState__ = null;
    
	private StringProperty name = new SimpleStringProperty(this, "name", null);	
	private ReadOnlySetWrapper<FXEntity2b> list = FXPersistentCollections.readOnlyObservablePersistentSet(this, "list");
	
	public StringProperty nameProperty() {
		return name;
	}
	public String getName() {
		return this.name.get();
	}
	public void setName(String name) {
		this.name.set(name);
	}
	
	public ReadOnlySetProperty<FXEntity2b> listProperty() {
		return list.getReadOnlyProperty();
	}
	public ObservableSet<FXEntity2b> getList() {
		return list.get();
	}
}
