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

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;

import org.granite.client.javafx.persistence.collection.FXPersistentCollections;
import org.granite.client.persistence.Entity;
import org.granite.client.persistence.Lazy;


@Entity
public class PersonUniDir extends AbstractEntity {

    private static final long serialVersionUID = 1L;
    
    private final ObjectProperty<Salutation> salutation = new SimpleObjectProperty<Salutation>(this, "salutation");
    private final StringProperty firstName = new SimpleStringProperty(this, "firstName");
    private final StringProperty lastName = new SimpleStringProperty(this, "lastName");
    @Lazy
    private final ReadOnlyListWrapper<Contact2> contacts = FXPersistentCollections.readOnlyObservablePersistentList(this, "contacts");
    
    
    public PersonUniDir() {
        super();
    }
    
    public PersonUniDir(Long id, Long version, String uid, String firstName, String lastName) {
        super(id, version, uid);
        this.firstName.set(firstName);
        this.lastName.set(lastName);
    }
    
    public PersonUniDir(Long id, boolean initialized, String detachedState) {
        super(id, initialized, detachedState);
    }
    
    public ObjectProperty<Salutation> salutationProperty() {
        return salutation;
    }    
    public Salutation getSalutation() {
        return salutation.get();
    }    
    public void setSalutation(Salutation salutation) {
        this.salutation.set(salutation);
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
    
    public ReadOnlyListProperty<Contact2> contactsProperty() {
        return contacts.getReadOnlyProperty();
    }
    public ObservableList<Contact2> getContacts() {
    	return contacts.get();
    }
}
