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

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;


/**
 * @author Franck WOLFF
 */
@Entity
public class Phone4 extends AbstractEntity {

    private static final long serialVersionUID = 1L;


    public Phone4() {
    }
    
    public Phone4(Long id, Long version, String uid) {
    	super(id, version, uid);
    }
    
    @ManyToOne(optional=false)
    private LegalEntity legalEntity;

    @Basic
    private String phone;


    public LegalEntity getLegalEntity() {
        return legalEntity;
    }
    public void setLegalEntity(LegalEntity legalEntity) {
        this.legalEntity = legalEntity;
    }
    
    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }
}
