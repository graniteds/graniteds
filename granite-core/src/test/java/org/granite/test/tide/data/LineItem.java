/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of Granite Data Services.
 *
 *   Granite Data Services is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU Library General Public License as published by
 *   the Free Software Foundation; either version 2 of the License, or (at your
 *   option) any later version.
 *
 *   Granite Data Services is distributed in the hope that it will be useful, but
 *   WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *   FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
 *   for more details.
 *
 *   You should have received a copy of the GNU Library General Public License
 *   along with this library; if not, see <http://www.gnu.org/licenses/>.
 */

package org.granite.test.tide.data;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;


/**
 * @author William Drai
 */
@Entity
public class LineItem extends AbstractEntity0 {

    private static final long serialVersionUID = 1L;


    public LineItem() {
    }
    
    public LineItem(Long id, Long version, String uid) {
    	super(id, version, uid);
    }
    
    @ManyToOne
    private Order3 order;
    
    @Basic
    private String description;


    public Order3 getOrder() {
		return order;
	}
	public void setOrder(Order3 order) {
		this.order = order;
	}

	public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
}
