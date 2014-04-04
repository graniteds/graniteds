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

package org.granite.client.test.javafx.tide;

import org.granite.client.persistence.Entity;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;


@Entity
public class PersonByteArray extends AbstractEntity {

    private static final long serialVersionUID = 1L;
    
    private StringProperty firstName = new SimpleStringProperty(this, "firstName");
    private StringProperty lastName = new SimpleStringProperty(this, "lastName");
    private ObjectProperty<byte[]> picture = new SimpleObjectProperty<byte[]>(this, "picture");
    
    
    public PersonByteArray() {
        super();
    }
    
    public PersonByteArray(Long id, Long version, String uid, String firstName, String lastName) {
        super(id, version, uid);
        this.firstName.set(firstName);
        this.lastName.set(lastName);
    }
    
    public PersonByteArray(Long id, boolean initialized, String detachedState) {
        super(id, initialized, detachedState);
    }
    
    public StringProperty firstNameProperty() {
        return firstName;
    }
    
    public String getFirstName() {
        return firstName.get();
    }
    
    public void setFirstName(String firstName) {
        this.firstName.set(firstName);
    }
    
    public StringProperty lastNameProperty() {
        return lastName;
    }
    
    public String getLastName() {
        return lastName.get();
    }
    
    public void setLastName(String lastName) {
        this.lastName.set(lastName);
    }
    
    public ObjectProperty<byte[]> pictureProperty() {
        return picture;
    }
    
    public byte[] getPicture() {
        return picture.get();
    }
    
    public void setPicture(byte[] picture) {
        this.picture.set(picture);
    }
}
