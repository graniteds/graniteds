/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2014 GRANITE DATA SERVICES S.A.S.
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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;

/**
 * @author William Drai
 */
@Entity
@Table(name="TORDER")
public class Order extends AbstractEntity {

    private static final long serialVersionUID = 1L;

    public Order() {
    }
    
    public Order(Long id, Long version, String uid) {
    	super(id, version, uid);
    	lineItemsList = new ArrayList<LineItemList>();
    	lineItemsBag = new ArrayList<LineItemBag>();
    }

    @Basic
    private String description;

    @OneToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY, mappedBy="order", orphanRemoval=true)
    @OrderColumn(name="idx")
    private List<LineItemList> lineItemsList = new ArrayList<LineItemList>();

    @OneToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY, mappedBy="order", orphanRemoval=true)
    private List<LineItemBag> lineItemsBag = new ArrayList<LineItemBag>();

	public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public List<LineItemList> getLineItemsList() {
        return lineItemsList;
    }
    public void setLineItemsList(List<LineItemList> lineItemsList) {
        this.lineItemsList = lineItemsList;
    }

    public List<LineItemBag> getLineItemsBag() {
        return lineItemsBag;
    }
    public void setLineItemsBag(List<LineItemBag> lineItemsBag) {
        this.lineItemsBag = lineItemsBag;
    }
}
