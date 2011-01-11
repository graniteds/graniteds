package org.granite.tide.test.home;

import javax.persistence.Entity;
import javax.persistence.Id;


@Entity
public class BaseEntity {

	@Id
	private Long id;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
}
