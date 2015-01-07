/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2015 GRANITE DATA SERVICES S.A.S.
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
package org.granite.test.tide.data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinTable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.OrderColumn;


@Entity
public class Classification extends AbstractEntity {

    private static final long serialVersionUID = 1L;

    @ManyToMany(cascade=CascadeType.ALL)
    @JoinTable(name="classification_hierarchy", 
            joinColumns={ @JoinColumn(name="parent_id") }, 
            inverseJoinColumns={ @JoinColumn(name="child_id") }
    )
    @OrderColumn(name="rank", nullable=false)
    private List<Classification> subClassifications = new ArrayList<Classification>();

    @ManyToMany(mappedBy="subClassifications", cascade=CascadeType.ALL)
    private Set<Classification> superClassifications = new HashSet<Classification>();

    @Column(name="classification_code", nullable=false)
    private String code;
    
    
    public Classification() {
    }
    
    public Classification(Long id, Long version, String uid) {
        super(id, version, uid);
    }
    
    public String getCode() {
        return this.code;
    }

    public void setCode(String code) {
        this.code = code;
    }
    
    public List<Classification> getSubClassifications() {
        return this.subClassifications;
    }

    public void setSubClassifications(List<Classification> subClassifications) {
        this.subClassifications = subClassifications;
    }

    public Set<Classification> getSuperClassifications() {
        return this.superClassifications;
    }

    public void setSuperClassifications(Set<Classification> superClassifications) {
        this.superClassifications = superClassifications;
    }

}
