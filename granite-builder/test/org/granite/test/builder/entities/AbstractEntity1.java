package org.granite.test.builder.entities;

import javax.persistence.Basic;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;


@MappedSuperclass
public class AbstractEntity1 extends AbstractEntity0 {
	
	private String createdBy; 
	
	@Version
	private Integer version;
	
	@Basic
	private String uid;
	
	
	public String getCreatedBy() {
		return createdBy;
	}
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}
	
	public Integer getVersion() {
		return version;
	}
	
	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
}
