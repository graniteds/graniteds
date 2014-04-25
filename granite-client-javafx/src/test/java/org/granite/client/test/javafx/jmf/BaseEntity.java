package org.granite.client.test.javafx.jmf;

import java.io.Serializable;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.SequenceGenerator;
import javax.persistence.Version;

@MappedSuperclass
public abstract class BaseEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ID")
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="CtacIdSeq") 
    @SequenceGenerator(name="CtacIdSeq", sequenceName="CTAC_ID_SEQ") 	
	private Long id;

    /* "UUID" and "UID" are Oracle reserved keywords -> "ENTITY_UID" */
    @Column(name="ENTITY_UID", unique = true, nullable = false, updatable = false, length=36)
    private String uid = UUID.randomUUID().toString();
	
	@Version
	private Integer version = Integer.valueOf(3);
	
	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }
	
    public String getUid() {
		return uid;
	}
	
	public Integer getVersion() { return version; }
	public void setVersion(Integer version) { this.version = version; }	
}
