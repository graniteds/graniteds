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
package org.granite.test.tide.hibernate.data;

import org.granite.test.tide.hibernate.data.Test2;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * @author Franck WOLFF
 */
@Entity
@Table(name = "vitalsign_test")
public class VitalSignTest2 extends Test2 {

    private static final long serialVersionUID = 1L;

    public VitalSignTest2() {
    }
    
    public VitalSignTest2(Long id, Long version, String uid) {
    	super(id, version, uid);
    }
    
    @OneToMany(mappedBy = "vitalSignTest", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<VitalSignObservation2> vitalSignObservations = new HashSet<VitalSignObservation2>();

    public Set<VitalSignObservation2> getVitalSignObservations() {
        return this.vitalSignObservations;
    }

    public void setVitalSignObservations(Set<VitalSignObservation2> vitalSignObservations) {
        this.vitalSignObservations = vitalSignObservations;
    }
}
