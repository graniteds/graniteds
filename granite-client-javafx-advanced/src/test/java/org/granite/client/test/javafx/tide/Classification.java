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

import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.beans.property.ReadOnlySetProperty;
import javafx.beans.property.ReadOnlySetWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;

import org.granite.client.javafx.persistence.collection.FXPersistentCollections;
import org.granite.client.messaging.RemoteAlias;
import org.granite.client.persistence.Entity;
import org.granite.client.persistence.Lazy;


@Entity
@RemoteAlias("org.granite.test.tide.Classification")
public class Classification extends AbstractEntity {

    private static final long serialVersionUID = 1L;
    
    private StringProperty name = new SimpleStringProperty(this, "name");
    @Lazy
    private ReadOnlyListWrapper<Classification> subclasses = FXPersistentCollections.readOnlyObservablePersistentList(this, "subclasses");
    @Lazy
    private ReadOnlySetWrapper<Classification> superclasses = FXPersistentCollections.readOnlyObservablePersistentSet(this, "superclasses");
    
    
    public Classification() {
        super();
    }
    
    public Classification(Long id, Long version, String uid, String name) {
        super(id, version, uid);
        this.name.set(name);
    }
    
    public Classification(Long id, boolean initialized, String detachedState) {
        super(id, initialized, detachedState);
    }
    
    public StringProperty nameProperty() {
        return name;
    }    
    public String getName() {
        return name.get();
    }    
    public void setName(String name) {
        this.name.set(name);
    }
    
    public ReadOnlyListProperty<Classification> subclassesProperty() {
    	return subclasses.getReadOnlyProperty();
    }
    public ObservableList<Classification> getSubclasses() {
        return subclasses.get();
    }
    
    public ReadOnlySetProperty<Classification> superclassesProperty() {
    	return superclasses.getReadOnlyProperty();
    }
    public ObservableSet<Classification> getSuperclasses() {
        return superclasses.get();
    }
}
