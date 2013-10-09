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
package org.granite.test.tide.hibernate.data;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;

import org.granite.test.tide.data.AbstractEntity;
import org.granite.test.tide.data.Order;
import org.hibernate.annotations.Cascade;

/**
 * @author William Drai
 */
@Entity
public class OrderRepo extends AbstractEntity {

    private static final long serialVersionUID = 1L;

    public OrderRepo() {
    }
    
    public OrderRepo(Long id, Long version, String uid) {
    	super(id, version, uid);
    	orders = new HashMap<String, Order>();
    }

    @Basic
    private String description;

    @SuppressWarnings("deprecation")
	@ManyToMany(cascade=CascadeType.ALL)
    @Cascade({org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN})
    private Map<String, Order> orders = null;

	public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, Order> getOrders() {
        return orders;
    }
    public void setOrders(Map<String, Order> orders) {
        this.orders = orders;
    }
}
