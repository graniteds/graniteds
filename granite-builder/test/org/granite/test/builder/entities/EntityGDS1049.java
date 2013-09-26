package org.granite.test.builder.entities;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;


@Entity
public class EntityGDS1049 extends AbstractEntity1 {
	
	@ManyToOne
	private Entity4 parent;
	
	public Entity4 getParent() {
		return parent;
	}
	
	public boolean isParent() {
		return parent == null;
	}
	
	public void setParent(Entity4 parent) {
		this.parent = parent;
	}
}
