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

import java.io.Serializable;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleObjectProperty;

import org.granite.client.persistence.Entity;
import org.granite.client.persistence.Id;


@Entity
public class Group implements Serializable {

	private static final long serialVersionUID = 1L;
	
	
	@SuppressWarnings("unused")
	private boolean __initialized__ = true;
    @SuppressWarnings("unused")
    private String __detachedState__ = null;

    @Id
    private final ReadOnlyStringWrapper name = new ReadOnlyStringWrapper(this, "name");
    private final ObjectProperty<User> user = new SimpleObjectProperty<User>(this, "user");
    

    public Group() {        
    }
    
    public Group(String name, boolean initialized, String detachedState) {
        if (!initialized) {
            __initialized__ = false;
            __detachedState__ = detachedState;
        }
        else
        	this.name.set(name);
    }
    
    public Group(String name) {
        this.name.set(name);
    }
    
    public ReadOnlyStringProperty nameProperty() {
        return name.getReadOnlyProperty();
    }    
    public String getName() {
        return name.get();
    }
    
    public ObjectProperty<User> userProperty() {
        return user;
    }    
    public User getUser() {
        return user.get();
    }    
    public void setUser(User user) {
        this.user.set(user);
    }
}
