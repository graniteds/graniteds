package org.granite.test.builder.entities;

import javax.persistence.Basic;
import javax.persistence.Entity;


@Entity
public class Entity3 extends AbstractEntity1 {
	
	@Basic
	private String name;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
