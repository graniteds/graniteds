package org.granite.test.tide.spring.data;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.Version;

import org.springframework.data.jpa.domain.AbstractPersistable;

@MappedSuperclass
public abstract class AbstractEntity extends AbstractPersistable<Long> {

	private static final long serialVersionUID = 1L;

    /* "UUID" and "UID" are Oracle reserved keywords -> "ENTITY_UID" */
    @Column(name="ENTITY_UID", unique=true, nullable=false, updatable=false, length=36)
    private String uid;

    @Version
    private Long version;
    
    
    public AbstractEntity() {    	
    }
    
    public AbstractEntity(Long id, Long version, String uid) {
    	super();
    	setId(id);
    	this.version = version;
    	this.uid = uid;
    }
    
    public void initUid() {
    	uid();
    }
    public void initIdUid(Long id, String uid) {
        setId(id);
        if (uid == null)
            uid = "Person:" + id;
        this.uid = uid;
    }

    public String getUid() {
        return this.uid;
    }
    
    public Long getVersion() {
        return version;
    }
    
    @Override
    public boolean equals(Object o) {
        return (o == this || (o instanceof AbstractEntity && uid().equals(((AbstractEntity)o).uid())));
    }
    
    @Override
    public int hashCode() {
        return uid().hashCode();
    }
    
	@PrePersist
    private void onPrePersist() {
        uid();
    }
    
    private String uid() {
        if (uid == null)
            uid = UUID.randomUUID().toString();
        return uid;
    }
}
