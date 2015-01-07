/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2015 GRANITE DATA SERVICES S.A.S.
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
package org.granite.test.tide.data;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

/**
 * @author Franck WOLFF
 */
@Entity
public class Medication extends AbstractEntity {

    private static final long serialVersionUID = 1L;


    public Medication() {
    }
    
    public Medication(Long id, Long version, String uid) {
    	super(id, version, uid);
    	prescriptionList = new HashSet<Prescription>();
    }
    
    @Basic
    private String name;
    
    @ManyToOne
    private Patient patient;

    @OneToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY, mappedBy="medication")
    private Set<Prescription> prescriptionList = new HashSet<Prescription>();


	public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public Patient getPatient() {
		return patient;
	}
	public void setPatient(Patient patient) {
		this.patient = patient;
	}

	public Set<Prescription> getPrescriptionList() {
        return prescriptionList;
    }
    public void setPrescriptionList(Set<Prescription> prescriptionList) {
        this.prescriptionList = prescriptionList;
    }
}
