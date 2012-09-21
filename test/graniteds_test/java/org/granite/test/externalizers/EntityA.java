package org.granite.test.externalizers;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;

@Entity
public class EntityA {
	
    @Id
    private long id;
    
    @Basic
    private long uid;
    
    @Embedded
    private EmbeddedEntity embedded = new EmbeddedEntity();
    
    @OneToMany(mappedBy="entity")
    private Set<EntityB> entities = new HashSet<EntityB>();

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

    public EmbeddedEntity getEmbedded() {
        return embedded;
    }
    public void setEmbedded(EmbeddedEntity embedded) {
        this.embedded = embedded;
    }

    public Set<EntityB> getEntities() {
        return entities;
    }
    public void setEntities(Set<EntityB> entities) {
        this.entities = entities;
    }
}