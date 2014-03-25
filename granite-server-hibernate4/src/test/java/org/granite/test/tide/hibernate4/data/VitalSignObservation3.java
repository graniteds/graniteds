/**
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
package org.granite.test.tide.hibernate4.data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.granite.test.tide.data.AbstractEntity;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

/**
 * @author Franck WOLFF
 */
@Entity
@Table(name = "vitalsignobservation3")
public class VitalSignObservation3 extends AbstractEntity {

    private static final long serialVersionUID = 1L;

    public VitalSignObservation3() {
    }
    
    public VitalSignObservation3(Long id, Long version, String uid) {
    	super(id, version, uid);
    }
    
    @Column(name = "obs_name")
    private String name;
    
    @Fetch(FetchMode.JOIN)
    @ManyToOne(optional = false)
    @JoinColumn(name = "vitalsigntest_id", insertable = false, updatable = false, nullable = false)
    private VitalSignTest3 vitalSignTest;
    
    
	public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public VitalSignTest3 getVitalSignTest() {
        return this.vitalSignTest;
    }

    public void setVitalSignTest(VitalSignTest3 vitalSignTest) {
        this.vitalSignTest = vitalSignTest;
    }
}
