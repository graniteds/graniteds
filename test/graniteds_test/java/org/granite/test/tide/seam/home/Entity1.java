package org.granite.test.tide.seam.home;

import javax.persistence.Entity;


@Entity
public class Entity1 extends BaseEntity {
	
	private String someObject;

	public String getSomeObject() {
		return someObject;
	}

	public void setSomeObject(String someObject) {
		this.someObject = someObject;
	}
}
