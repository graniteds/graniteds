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

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * @author William Drai
 */
@Entity
@Table(name="TORDER")
public class Order3 extends AbstractEntity0 {

    private static final long serialVersionUID = 1L;

    public Order3() {
    }
    
    public Order3(Long id, Long version, String uid) {
    	super(id, version, uid);
    	lineItems = new HashSet<LineItem>();
    }

    @Basic
    private String description;

    @OneToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY, mappedBy="order", orphanRemoval=true)
    private Set<LineItem> lineItems = new HashSet<LineItem>();

	public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public Set<LineItem> getLineItems() {
        return lineItems;
    }
    public void setLineItems(Set<LineItem> lineItems) {
        this.lineItems = lineItems;
    }
}
