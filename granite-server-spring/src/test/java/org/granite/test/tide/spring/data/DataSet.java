package org.granite.test.tide.spring.data;

import javax.persistence.Basic;
import javax.persistence.Entity;


@Entity
public class DataSet extends AbstractEntity {
	
	private static final long serialVersionUID = 1L;
	
	@Basic
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
