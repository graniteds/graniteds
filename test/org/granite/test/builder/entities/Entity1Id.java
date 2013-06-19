package org.granite.test.builder.entities;

import javax.persistence.Embeddable;

@Embeddable
public class Entity1Id {
	private Long id1;
	private Long id2;
	
	public Entity1Id() {
	}
	
	public Entity1Id(Long id1, Long id2) {
		this.id1 = id1;
		this.id2 = id2;
	}
	
	public Long getId1() {
		return id1;
	}
	
	public Long getId2() {
		return id2;
	}
}