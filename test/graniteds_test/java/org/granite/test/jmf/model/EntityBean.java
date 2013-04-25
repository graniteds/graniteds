package org.granite.test.jmf.model;

import java.io.Serializable;
import java.util.UUID;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Version;

@Entity
public class EntityBean implements Serializable {

	private static final long serialVersionUID = 0L;
	
	@Id @GeneratedValue
	private Integer id;
	
	@Column(name="ENTITY_UID", unique=true, nullable=false, updatable=false, length=36)
	private String uid;
	
    @Version
    private Integer version;
    
    @Basic
    private boolean booleanValue;

    
    public Integer getId() {
        return id;
    }

    public Integer getVersion() {
        return version;
    }

    public boolean isBooleanValue() {
		return booleanValue;
	}

	public void setBooleanValue(boolean booleanValue) {
		this.booleanValue = booleanValue;
	}

	@Override
    public boolean equals(Object o) {
        return (o == this || (o instanceof EntityBean && uid().equals(((EntityBean)o).uid())));
    }

    @Override
    public int hashCode() {
        return uid().hashCode();
    }

    public static class AbstractEntityListener {

        @PrePersist
        public void onPrePersist(EntityBean entity) {
        	entity.uid();
        }
    }

    private String uid() {
        if (uid == null)
            uid = UUID.randomUUID().toString();
        return uid;
    }
    
    public void junitInit() {
    	id = 2;
    	uid();
    	version = 1;
    }
    
    public boolean junitEquals(Object o) {
    	if (!(o instanceof EntityBean))
    		return false;
    	
    	EntityBean eb = (EntityBean)o;
    	return (
    		(id == null ? eb.id == null : id.equals(eb.id)) &&
    		(uid == null ? eb.uid == null : uid.equals(eb.uid)) &&
    		(version == null ? eb.version == null : version.equals(eb.version)) &&
    		(booleanValue == eb.booleanValue)
    	);
    }
}
