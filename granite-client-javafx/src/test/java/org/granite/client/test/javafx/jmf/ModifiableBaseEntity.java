package org.granite.client.test.javafx.jmf;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class ModifiableBaseEntity extends BaseEntity {

	private static final long serialVersionUID = 1L;

	@Column(name = "CREATE_DATE")
	private Date createDate;

	@Column(name = "CREATE_USER")
	private String createUser;

	@Column(name = "MODIFY_DATE")
	private Date modifyDate;

	@Column(name = "MODIFY_USER")
	private String modifyUser;

	
	public Date getCreateDate() { return createDate; }
	public void setCreateDate(Date createDate) { this.createDate = createDate; }
	
	public String getCreateUser() { return createUser; }
	public void setCreateUser(String createUser) { this.createUser = createUser; }

	public Date getModifyDate() { return modifyDate; }
	public void setModifyDate(Date modifyDate) { this.modifyDate = modifyDate; }

	public String getModifyUser() { return modifyUser; }
	public void setModifyUser(String modifyUser) { this.modifyUser = modifyUser; }	
}
