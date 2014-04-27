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
package org.granite.client.test.javafx.jmf;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class ModifiableBaseEntity extends BaseEntity {

	private static final long serialVersionUID = 1L;

	@Column(name = "CREATE_DATE")
	private Date createDate;

	@Column(name = "CREATE_USER")
	private String createUser;

	@Column(name = "MODIFY_DATE")
	private Date modifyDate;

	@Column(name = "MODIFY_USER")
	private String modifyUser;

	
	public Date getCreateDate() { return createDate; }
	public void setCreateDate(Date createDate) { this.createDate = createDate; }
	
	public String getCreateUser() { return createUser; }
	public void setCreateUser(String createUser) { this.createUser = createUser; }

	public Date getModifyDate() { return modifyDate; }
	public void setModifyDate(Date modifyDate) { this.modifyDate = modifyDate; }

	public String getModifyUser() { return modifyUser; }
	public void setModifyUser(String modifyUser) { this.modifyUser = modifyUser; }	
}
