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
package org.granite.test.tide.hibernate4.data;

import org.granite.test.tide.data.AbstractEntity;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;

/**
 * @author Franck WOLFF
 */
@Entity
public class Patient2 extends AbstractEntity {

    private static final long serialVersionUID = 1L;


    public Patient2() {
    }
    
    public Patient2(Long id, Long version, String uid) {
    	super(id, version, uid);
    	tests = new HashSet<Test2>();
    }
    
    @Basic
    private String name;

	@OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<Visit2> visits = new HashSet<Visit2>();

	@OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<Test2> tests = new HashSet<Test2>();


	public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    
    public Set<Visit2> getVisits() {
        return visits;
    }
    public void setVisits(Set<Visit2> visits) {
        this.visits = visits;
    }
    
    public Set<Test2> getTests() {
        return tests;
    }
    public void setTests(Set<Test2> tests) {
        this.tests = tests;
    }
}
