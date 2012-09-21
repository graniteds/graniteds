package org.granite.test.externalizers;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Embeddable;
import javax.persistence.OneToMany;

@Embeddable
public class EmbeddedEntity {

	@OneToMany(mappedBy="entity")
	private Set<EntityB> entities = new HashSet<EntityB>();

    public Set<EntityB> getEntities() {
        return entities;
    }
    public void setEntities(Set<EntityB> entities) {
        this.entities = entities;
    }

}
