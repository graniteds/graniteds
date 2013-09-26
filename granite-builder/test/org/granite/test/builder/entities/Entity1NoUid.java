package org.granite.test.builder.entities;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Version;


@Entity
public class Entity1NoUid {
	
	@Id
	private Long id;
	
	@Version
	private Integer version;

	@Basic
	private String name;
	
	public Long getId() {
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
