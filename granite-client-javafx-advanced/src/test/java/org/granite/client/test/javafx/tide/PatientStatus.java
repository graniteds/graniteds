/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2015 GRANITE DATA SERVICES S.A.S.
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
@RemoteAlias("org.granite.test.tide.PatientStatus")
public class PatientStatus extends AbstractEntity {

	private static final long serialVersionUID = 1L;
	
	private StringProperty name = new SimpleStringProperty(this, "name");
    private ObjectProperty<Patient4> patient = new SimpleObjectProperty<Patient4>(this, "patient");
    @Lazy
    private ReadOnlyListWrapper<Diagnosis> deathCauses = FXPersistentCollections.readOnlyObservablePersistentList(this, "deathCauses");


    public PatientStatus() {
        super();
    }

    public PatientStatus(Long id, Long version, String uid, Patient4 patient, String name) {
        super(id, version, uid);
        this.patient.set(patient);
        this.name.set(name);
    }
    
    public PatientStatus(Long id, boolean initialized, String detachedState) {
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

    public ReadOnlyListProperty<Diagnosis> deathCausesProperty() {
        return deathCauses.getReadOnlyProperty();
    }
    public ObservableList<Diagnosis> getDeathCauses() {
        return deathCauses.get();
    }
}
