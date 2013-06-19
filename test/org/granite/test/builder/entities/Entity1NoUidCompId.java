package org.granite.test.builder.entities;

import javax.persistence.Basic;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Version;


@Entity
public class Entity1NoUidCompId {
	
	@EmbeddedId
	private Entity1Id id;
	
	@Version
	private Integer version;

	@Basic
	private String name;
	
	public Entity1Id getId() {
		return id;
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
}
