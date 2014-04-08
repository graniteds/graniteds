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
package org.granite.client.test.javafx;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;


@Entity
public class Entity1c implements Serializable {
	
	private static final long serialVersionUID = 1L;

	@Basic
	private String name;
	
	@Basic
	@Column(scale = 2, nullable = false)
	private BigDecimal value;
	
	@Basic
	@Column(scale = 2, nullable = false)
	private BigDecimal value2;

	
	@OneToMany(mappedBy="entity1")
	private Map<String, Entity2c> map = new HashMap<String, Entity2c>();
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public BigDecimal getValue() {
		return value;
	}
	public void setValue(BigDecimal value) {
		this.value = value;
	}
	
	public BigDecimal getValue2() {
		return value2;
	}
	public void setValue2(BigDecimal value) {
		this.value2 = value;
	}
	
	public Map<String, Entity2c> getMap() {
		return map;
	}
	public void setMap(Map<String, Entity2c> map) {
		this.map = map;
	}
}
