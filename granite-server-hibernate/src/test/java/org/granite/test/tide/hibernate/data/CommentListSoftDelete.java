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
/*
  GRANITE DATA SERVICES
  Copyright (C) 2011 GRANITE DATA SERVICES S.A.S.

  This file is part of Granite Data Services.

  Granite Data Services is free software; you can redistribute it and/or modify
  it under the terms of the GNU Library General Public License as published by
  the Free Software Foundation; either version 2 of the License, or (at your
  option) any later version.

  Granite Data Services is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
  for more details.

  You should have received a copy of the GNU Library General Public License
  along with this library; if not, see <http://www.gnu.org/licenses/>.
*/

package org.granite.test.tide.hibernate.data;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.SQLDelete;


/**
 * @author Franck WOLFF
 */
@Entity
@Table(name="commentlistsoftdelete")
@Filter(name="filterSoftDelete", condition="deleted = 0")
@SQLDelete(sql="update commentlistsoftdelete set deleted = 1 where id = ? and version = ?")
public class CommentListSoftDelete extends AbstractEntitySoftDelete {

    private static final long serialVersionUID = 1L;


    public CommentListSoftDelete() {
    }
    
    public CommentListSoftDelete(Long id, Long version, String uid) {
    	super(id, version, uid);
    }
    
    @OneToMany(mappedBy="commentList", cascade=CascadeType.ALL, orphanRemoval=true)
    @Filter(name="filterSoftDelete")
    private Set<CommentSoftDelete> comments = new HashSet<CommentSoftDelete>();

    public Set<CommentSoftDelete> getComments() {
        return this.comments;
    }

    public void setComments(Set<CommentSoftDelete> comments) {
        this.comments = comments;
    }
}
