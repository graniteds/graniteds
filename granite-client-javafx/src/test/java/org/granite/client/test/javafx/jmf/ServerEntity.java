/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of the Granite Data Services Platform.
 *
 *                               ***
 *
 *   Community License: GPL 3.0
 *
 *   This file is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published
 *   by the Free Software Foundation, either version 3 of the License,
 *   or (at your option) any later version.
 *
 *   This file is distributed in the hope that it will be useful, but
 *   WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *                               ***
 *
 *   Available Commercial License: GraniteDS SLA 1.0
 *
 *   This is the appropriate option if you are creating proprietary
 *   applications and you are not prepared to distribute and share the
 *   source code of your application under the GPL v3 license.
 *
 *   Please visit http://www.granitedataservices.com/license for more
 *   details.
 */
package org.granite.client.test.javafx.jmf;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Version;

@Entity
public class ServerEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id @GeneratedValue
	private Integer id;
	
    @Column(name="ENTITY_UID", unique=true, nullable=false, updatable=false, length=36)
	private String uid;

	@Version
    private Integer version;
	
	@Basic
	private String name;
	
	@OneToMany(mappedBy="entity")
	private List<ServerCollectionEntity> list = new ArrayList<ServerCollectionEntity>();

	public ServerEntity() {
	}

	public ServerEntity(Integer id, Integer version) {
		this.id = id;
		this.version = version;
	}

	public Integer getId() {
		return id;
	}

	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}

	public Integer getVersion() {
		return version;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public List<ServerCollectionEntity> getList() {
		return list;
	}
	public void setList(List<ServerCollectionEntity> list) {
		this.list = list;
	}
}
