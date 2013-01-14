package org.granite.test.builder.entities;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Version;

import org.granite.test.builder.entities.Entity1;


@Entity
public class Entity2 {
	
	@Id
	private Long id;
	
	@Version
	private Integer version;
	
	@Basic
	private String uid;

	@Basic
	private String name;
	
	@ManyToOne
	private Entity1 entity;
	
	public Long getId() {
		return id;
	}
	
	public Integer getVersion() {
		return version;
	}
	
	public String getUid() {
		return uid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Entity1 getEntity() {
		return entity;
	}

	public void setEntity(Entity1 entity) {
		this.entity = entity;
	}
}
