/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2014 GRANITE DATA SERVICES S.A.S.
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
