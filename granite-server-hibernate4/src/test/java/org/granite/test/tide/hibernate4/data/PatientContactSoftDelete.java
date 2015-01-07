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
package org.granite.test.tide.hibernate4.data;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.SQLDelete;


/**
 * @author Franck WOLFF
 */
@Entity
@Table(name="patientcontactsoftdelete")
@Filter(name="filterSoftDelete", condition="deleted = 0")
@SQLDelete(sql="update patientcontactsoftdelete set deleted = 1 where id = ? and version = ?")
public class PatientContactSoftDelete extends AbstractEntitySoftDelete {

    private static final long serialVersionUID = 1L;


    public PatientContactSoftDelete() {
    }
    
    public PatientContactSoftDelete(Long id, Long version, String uid) {
    	super(id, version, uid);
    }
    
    @Fetch(FetchMode.JOIN)
    @ManyToOne(optional = false)
    private PatientSoftDelete patient;

    @Fetch(FetchMode.SELECT)
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval=true)
    @JoinColumn(name="comment_list_id")
    private CommentListSoftDelete commentList;
    
    @Fetch(FetchMode.JOIN)
    @ManyToOne(optional=false, cascade=CascadeType.ALL)
    @JoinColumn(name="person_id", nullable=false)
    private SimplePersonSoftDelete person;


    public PatientSoftDelete getPatient() {
        return patient;
    }
    public void setPatient(PatientSoftDelete patient) {
        this.patient = patient;
    }
    
    public SimplePersonSoftDelete getPerson() {
        return person;
    }
    public void setPerson(SimplePersonSoftDelete person) {
        this.person = person;
    }

    public CommentListSoftDelete getCommentList() {
    	return commentList;
    }
    public void setCommentList(CommentListSoftDelete commentList) {
    	this.commentList = commentList;
    }
}
