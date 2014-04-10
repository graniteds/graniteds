/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2014 GRANITE DATA SERVICES S.A.S.
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
@RemoteAlias("org.granite.test.tide.VisitTest")
public class VisitTest extends AbstractEntity {

    private static final long serialVersionUID = 1L;
    
	private StringProperty name = new SimpleStringProperty(this, "name");
    private ObjectProperty<Patient3> patient = new SimpleObjectProperty<Patient3>(this, "patient");
    private ObjectProperty<Visit> visit = new SimpleObjectProperty<Visit>(this, "visit");
    @Lazy
    private ReadOnlyListWrapper<VisitObservation> observations = FXPersistentCollections.readOnlyObservablePersistentList(this, "observations");


    public VisitTest() {
        super();
    }

    public VisitTest(Long id, Long version, String uid, Patient3 patient, Visit visit, String name) {
        super(id, version, uid);
        this.patient.set(patient);
        this.visit.set(visit);
        this.name.set(name);
    }

    public VisitTest(Long id, boolean initialized, String detachedState) {
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

    public ObjectProperty<Patient3> patientProperty() {
        return patient;
    }
    public Patient3 getPatient() {
        return patient.get();
    }
    public void setPatient(Patient3 patient) {
        this.patient.set(patient);
    }

    public ReadOnlyListProperty<VisitObservation> observationsProperty() {
        return observations.getReadOnlyProperty();
    }
    public ObservableList<VisitObservation> getObservations() {
        return observations.get();
    }
}
