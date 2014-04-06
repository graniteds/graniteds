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
package org.granite.client.test.javafx.tide;

import javafx.beans.property.ReadOnlyMapProperty;
import javafx.beans.property.ReadOnlyMapWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableMap;

import org.granite.client.javafx.persistence.collection.FXPersistentCollections;
import org.granite.client.messaging.RemoteAlias;
import org.granite.client.persistence.Entity;
import org.granite.client.persistence.Lazy;


@Entity
@RemoteAlias("org.granite.client.test.Person11")
public class Person11 extends AbstractEntity {

    private static final long serialVersionUID = 1L;
    
    private StringProperty firstName = new SimpleStringProperty(this, "firstName");
    private StringProperty lastName = new SimpleStringProperty(this, "lastName");
    @Lazy
    private ReadOnlyMapWrapper<SimpleEntity, SimpleEntity2> map = FXPersistentCollections.readOnlyObservablePersistentMap(this, "map");
    
    
    public Person11() {
        super();
    }
    
    public Person11(Long id, Long version, String uid, String firstName, String lastName) {
        super(id, version, uid);
        this.firstName.set(firstName);
        this.lastName.set(lastName);
    }
    
    public Person11(Long id, boolean initialized, String detachedState) {
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
    
    public ReadOnlyMapProperty<SimpleEntity, SimpleEntity2> mapProperty() {
        return map.getReadOnlyProperty();
    }
    public ObservableMap<SimpleEntity, SimpleEntity2> getMap() {
    	return map.get();
    }
}
