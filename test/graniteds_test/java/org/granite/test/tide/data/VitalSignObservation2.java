/*
  GRANITE DATA SERVICES
  Copyright (C) 2011 GRANITE DATA SERVICES S.A.S.

  This file is part of Granite Data Services.

  Granite Data Services is free software; you can redistribute it and/or modify
  it under the terms of the GNU Library General Public License as published by
  the Free Software Foundation; either version 2 of the License, or (at your
  option) any later version.

  Granite Data Services is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
  for more details.

  You should have received a copy of the GNU Library General Public License
  along with this library; if not, see <http://www.gnu.org/licenses/>.
*/

package org.granite.test.tide.data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

/**
 * @author Franck WOLFF
 */
@Entity
@Table(name = "vitalsign_observation")
public class VitalSignObservation2 extends AbstractEntity {

    private static final long serialVersionUID = 1L;

    @Fetch(FetchMode.SELECT)
    @ManyToOne
    @JoinColumn(name = "test_id")
    private VitalSignTest2 vitalSignTest;

    @Column(name = "obs_name")
    private String name;

    public VitalSignTest2 getVitalSignTest() {
        return this.vitalSignTest;
    }

    public void setVitalSignTest(VitalSignTest2 vitalSignTest) {
        this.vitalSignTest = vitalSignTest;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
