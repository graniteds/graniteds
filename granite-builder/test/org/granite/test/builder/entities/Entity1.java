package org.granite.test.builder.entities;

import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Version;

import org.granite.test.builder.entities.Entity2;


@Entity
public class Entity1 {
	
	@Id
	private Long id;
	
	@Version
	private Integer version;
	
	@Basic
	private String uid;

	@Basic
	private String name;
	
	@OneToMany(fetch=FetchType.LAZY, mappedBy="entity")
	private Set<Entity2> entities;
	
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

	public Set<Entity2> getEntities() {
		return entities;
	}

	public void setEntities(Set<Entity2> entities) {
		this.entities = entities;
	}
}
