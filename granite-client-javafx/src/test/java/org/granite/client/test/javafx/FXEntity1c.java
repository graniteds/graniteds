/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
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
package org.granite.client.test.javafx;

import java.io.Serializable;
import java.math.BigDecimal;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyMapProperty;
import javafx.beans.property.ReadOnlyMapWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableMap;

import org.granite.client.messaging.RemoteAlias;
import org.granite.client.persistence.collection.javafx.FXPersistentCollections;
import org.granite.messaging.annotations.Serialized;


@Serialized
@RemoteAlias("org.granite.client.test.javafx.Entity1c")
public class FXEntity1c implements Serializable {

	private static final long serialVersionUID = 1L;
	
    @SuppressWarnings("unused")
    private boolean __initialized__ = true;
    @SuppressWarnings("unused")
	private String __detachedState__ = null;
    
	private StringProperty name = new SimpleStringProperty(this, "name", null);
	private ObjectProperty<BigDecimal> value = new SimpleObjectProperty<BigDecimal>(this, "value", null);
	private ObjectProperty<BigDecimal> value2 = new SimpleObjectProperty<BigDecimal>(this, "value2", null);
	private ReadOnlyMapWrapper<String, FXEntity2c> map = FXPersistentCollections.readOnlyObservablePersistentMap(this, "map");
	
	public StringProperty nameProperty() {
		return name;
	}
	public String getName() {
		return this.name.get();
	}
	public void setName(String name) {
		this.name.set(name);
	}
	
	public ObjectProperty<BigDecimal> valueProperty() {
		return value;
	}
	public BigDecimal getValue() {
		return this.value.get();
	}
	public void setValue(BigDecimal value) {
		this.value.set(value);
	}
	
	public ObjectProperty<BigDecimal> value2Property() {
		return value2;
	}
	public BigDecimal getValue2() {
		return this.value2.get();
	}
	public void setValue2(BigDecimal value) {
		this.value2.set(value);
	}
	
	public ReadOnlyMapProperty<String, FXEntity2c> mapProperty() {
		return map.getReadOnlyProperty();
	}
	public ObservableMap<String, FXEntity2c> getMap() {
		return map.get();
	}
}
