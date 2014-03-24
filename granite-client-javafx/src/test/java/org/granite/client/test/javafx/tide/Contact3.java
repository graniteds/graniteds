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

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import org.granite.client.messaging.RemoteAlias;
import org.granite.client.persistence.Entity;
import org.granite.client.persistence.Lazy;


@Entity
@RemoteAlias("org.granite.client.test.Contact3")
public class Contact3 extends AbstractEntity {

    private static final long serialVersionUID = 1L;
    
    @Lazy
    private ObjectProperty<Person9> person = new SimpleObjectProperty<Person9>(this, "person");
    private StringProperty email = new SimpleStringProperty(this, "email");
    
    
    public Contact3() {
        super();
    }
    
    public Contact3(Long id, Long version, String uid, String email) {
        super(id, version, uid);
        this.email.set(email);
    }    
    public Contact3(Long id, Long version, String uid, Person9 person, String email) {
        super(id, version, uid);
        this.person.set(person);
        this.email.set(email);
    }    
    
    public ObjectProperty<Person9> personProperty() {
        return person;
    }
    
    public Person9 getPerson() {
        return person.get();
    }
    
    public void setPerson(Person9 person) {
        this.person.set(person);
    }
    
    public StringProperty emailProperty() {
        return email;
    }
    
    public String getEmail() {
        return email.get();
    }
    
    public void setEmail(String email) {
        this.email.set(email);
    }
}
