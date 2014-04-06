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

    public Map<Object, Map<String, Object>> getSavedProperties();

    public Map<String, Object> getSavedProperties(Object localEntity);
    
    public void addUnsaved(Object entity);
    
    public void resetEntity(MergeContext mergeContext, Object entity, Object parent, Set<Object> cache);

    public void resetAllEntities(MergeContext mergeContext, Set<Object> cache);

    public void entityPropertyChangeHandler(Object owner, Object target, String property, Object oldValue, Object newValue);

    public void entityCollectionChangeHandler(Object owner, String property, Collection<?> coll, ChangeKind kind, Integer location, Object[] items);
    
    public void entityMapChangeHandler(Object owner, String property, Map<?, ?> map, ChangeKind kind, Object[] items);

}
