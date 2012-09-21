package org.granite.test.externalizers;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class EntityB {
	
    @Id
    private long id;
    
    @Basic
    private long uid;
    
    @Basic
    private String name;
    
    @ManyToOne
    private EntityA entity;

    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }

    public long getUid() {
        return uid;
    }
    public void setUid(long uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public EntityA getEntity() {
        return entity;
    }
    public void setEntity(EntityA entity) {
        this.entity = entity;
    }
}
