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

package org.granite.tide.data;

import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;

import org.granite.tide.data.DataContext.EntityUpdateType;


public class DataPublishListener {
    
    @PostPersist
    public void onPostPersist(Object entity) {
    	DataContext.addUpdate(EntityUpdateType.PERSIST, entity);
    }
    
    @PostRemove
    public void onPostRemove(Object entity) {
    	DataContext.addUpdate(EntityUpdateType.REMOVE, entity);
    }
    
    @PostUpdate
    public void onPostUpdate(Object entity) {
    	DataContext.addUpdate(EntityUpdateType.UPDATE, entity);
    }
}
