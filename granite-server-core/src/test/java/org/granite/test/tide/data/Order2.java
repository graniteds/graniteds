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
package org.granite.test.tide.data;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;

/**
 * @author William Drai
 */
@Entity
@Table(name="TORDER")
public class Order2 extends AbstractEntity {

    private static final long serialVersionUID = 1L;

    public Order2() {
    }
    
    public Order2(Long id, Long version, String uid) {
    	super(id, version, uid);
    	lineItemsList = new ArrayList<LineItemList2>();
    	lineItemsBag = new ArrayList<LineItemBag2>();
    }

    @Basic
    private String description;

    @OneToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY, orphanRemoval=true)
    @JoinTable(name="torder_lineitem", 
    		joinColumns={ @JoinColumn(name="order_id") }, 
    		inverseJoinColumns = { @JoinColumn(name = "lineitem_id") }
    )
    @OrderColumn(name="idx", nullable=false)
    private List<LineItemList2> lineItemsList = new ArrayList<LineItemList2>();

    @OneToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY, orphanRemoval=true)
    private List<LineItemBag2> lineItemsBag = new ArrayList<LineItemBag2>();

	public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public List<LineItemList2> getLineItemsList() {
        return lineItemsList;
    }
    public void setLineItemsList(List<LineItemList2> lineItemsList) {
        this.lineItemsList = lineItemsList;
    }

    public List<LineItemBag2> getLineItemsBag() {
        return lineItemsBag;
    }
    public void setLineItemsBag(List<LineItemBag2> lineItemsBag) {
        this.lineItemsBag = lineItemsBag;
    }
}
