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

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import org.granite.client.messaging.RemoteAlias;
import org.granite.messaging.annotations.Serialized;


@Serialized
@RemoteAlias("org.granite.client.test.javafx.Entity2")
public class FXEntity2 implements Serializable {

	private static final long serialVersionUID = 1L;
	
    @SuppressWarnings("unused")
    private boolean __initialized__ = true;
    @SuppressWarnings("unused")
	private String __detachedState__ = null;
    
	private StringProperty name = new SimpleStringProperty(this, "name", null);
	private ObjectProperty<FXEntity1> entity1 = new SimpleObjectProperty<FXEntity1>(this, "entity1", null);
	
	public StringProperty nameProperty() {
		return name;
	}
	public String getName() {
		return name.get();
	}
	public void setName(String name) {
		this.name.set(name);
	}
	
	public ObjectProperty<FXEntity1> entity1Property() {
		return entity1;
	}
	public FXEntity1 getEntity1() {
		return entity1.get();
	}
	public void setEntity1(FXEntity1 entity1) {
		this.entity1.set(entity1);
	}
}
