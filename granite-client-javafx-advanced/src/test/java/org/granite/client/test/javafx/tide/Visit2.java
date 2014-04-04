/*
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
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
import org.granite.client.messaging.RemoteAlias;
import org.granite.client.persistence.Entity;
import org.granite.client.persistence.Lazy;

@Entity
@RemoteAlias("org.granite.client.test.tide.Visit2")
public class Visit2 extends AbstractEntity {

	private static final long serialVersionUID = 1L;
	
	private StringProperty name = new SimpleStringProperty(this, "name");
    private ObjectProperty<Patient4> patient = new SimpleObjectProperty<Patient4>(this, "patient");
    @Lazy
    private ReadOnlyListWrapper<Diagnosis> assessments = FXPersistentCollections.readOnlyObservablePersistentList(this, "assessments");


    public Visit2() {
        super();
    }

    public Visit2(Long id, Long version, String uid, Patient4 patient, String name) {
        super(id, version, uid);
        this.patient.set(patient);
        this.name.set(name);
    }

    public Visit2(Long id, boolean initialized, String detachedState) {
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

    public ObjectProperty<Patient4> patientProperty() {
        return patient;
    }
    public Patient4 getPatient() {
        return patient.get();
    }
    public void setPatient(Patient4 patient) {
        this.patient.set(patient);
    }

    public ReadOnlyListProperty<Diagnosis> assessmentsProperty() {
        return assessments.getReadOnlyProperty();
    }
    public ObservableList<Diagnosis> getAssessments() {
        return assessments.get();
    }
}
