/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of the Granite Data Services Platform.
 *
 *   Granite Data Services is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Lesser General Public
 *   License as published by the Free Software Foundation; either
 *   version 2.1 of the License, or (at your option) any later version.
 *
 *   Granite Data Services is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 *   General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public
 *   License along with this library; if not, write to the Free Software
 *   Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301,
 *   USA, or see <http://www.gnu.org/licenses/>.
 */
package org.granite.test.externalizers;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
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
    
    @OneToMany(mappedBy="entity", cascade=CascadeType.ALL)
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