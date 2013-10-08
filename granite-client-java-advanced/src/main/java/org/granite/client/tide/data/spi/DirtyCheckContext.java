/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of the Granite Data Services Platform.
 *
 *                               ***
 *
 *   Community License: GPL 3.0
 *
 *   This file is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published
 *   by the Free Software Foundation, either version 3 of the License,
 *   or (at your option) any later version.
 *
 *   This file is distributed in the hope that it will be useful, but
 *   WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *                               ***
 *
 *   Available Commercial License: GraniteDS SLA 1.0
 *
 *   This is the appropriate option if you are creating proprietary
 *   applications and you are not prepared to distribute and share the
 *   source code of your application under the GPL v3 license.
 *
 *   Please visit http://www.granitedataservices.com/license for more
 *   details.
 */
package org.granite.client.tide.data.spi;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.granite.client.tide.data.spi.DataManager.ChangeKind;

/**
 * @author William DRAI
 */
public interface DirtyCheckContext {

    public void clear(boolean notify);

    public void markNotDirty(Object object, Object entity);
    
    public boolean checkAndMarkNotDirty(MergeContext mergeContext, Object local, Object received, Object parent);
    
	public void fixRemovalsAndPersists(MergeContext mergeContext, List<Object> removals, List<Object> persists);
	
	public boolean isUnsaved(Object object);
	
    public boolean isEntityChanged(Object entity);

    public boolean isEntityDeepChanged(Object entity);

    public Map<String, Object> getSavedProperties(Object localEntity);
    
    public void addUnsaved(Object entity);
    
    public void resetEntity(MergeContext mergeContext, Object entity, Object parent, Set<Object> cache);

    public void resetAllEntities(MergeContext mergeContext, Set<Object> cache);

    public void entityPropertyChangeHandler(Object owner, Object target, String property, Object oldValue, Object newValue);

    public void entityCollectionChangeHandler(Object owner, String property, Collection<?> coll, ChangeKind kind, Integer location, Object[] items);
    
    public void entityMapChangeHandler(Object owner, String property, Map<?, ?> map, ChangeKind kind, Object[] items);

}
