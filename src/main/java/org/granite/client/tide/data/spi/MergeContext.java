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

import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.granite.client.tide.data.Conflicts;
import org.granite.client.tide.data.EntityManager;
import org.granite.client.tide.server.ServerSession;

/**
 * @author William DRAI
 */
public class MergeContext {
    
    private static ThreadLocal<Map<EntityManager, MergeContext>> mergeContext = new ThreadLocal<Map<EntityManager, MergeContext>>() {
        @Override
        protected Map<EntityManager, MergeContext> initialValue() {
            return new IdentityHashMap<EntityManager, MergeContext>();
        }
    };
    
    private final EntityManager entityManager;
	private final DirtyCheckContext dirtyCheckContext;
    private IdentityHashMap<Object, Object> entityCache = null;
    private LinkedList<Object> mergeStack = new LinkedList<Object>();
    
    private String externalDataSessionId = null;
    private EntityManager sourceEntityManager = null;
    private ServerSession serverSession = null;
    private boolean mergeUpdate = false;
    private boolean merging = false;
    private Set<Object> versionChangeCache = null;
    private boolean resolvingConflict = false;
    private boolean skipDirtyCheck = false;
    private Conflicts mergeConflicts = null;
    private boolean uninitializing = false;
    
    
    public static MergeContext get(EntityManager entityManager) {
        return mergeContext.get().get(entityManager);
    }
    
    public static void destroy(EntityManager entityManager) {
        mergeContext.get().remove(entityManager);
    }
    
    
    public MergeContext(EntityManager entityManager, DirtyCheckContext dirtyCheckContext, ServerSession serverSession) {
        this.entityManager = entityManager;
        this.dirtyCheckContext = dirtyCheckContext;
        this.serverSession = serverSession;
        mergeContext.get().put(entityManager, this);
    }

    
    public void initMerge() {
        if (this.entityCache == null) {
            this.entityCache = new IdentityHashMap<Object, Object>();
            this.mergeUpdate = true;
        }
    }
    
    public void clear() {
		this.entityCache = null;
        this.mergeConflicts = null;
        this.versionChangeCache = null;
        this.resolvingConflict = false;
		this.uninitializing = false;
        this.merging = false;
        this.mergeUpdate = false;
    }

    public void addConflict(Object localEntity, Object receivedEntity, List<String> properties) {
        if (this.mergeConflicts == null)
            this.mergeConflicts = new Conflicts(this.entityManager);

        this.mergeConflicts.addConflict(localEntity, receivedEntity, properties);
    }

    public void initMergeConflicts() {
    	this.entityCache = null;
        this.versionChangeCache = null;
        this.resolvingConflict = false;
    }

    public void checkConflictsResolved() {
        if (this.mergeConflicts != null && this.mergeConflicts.isAllResolved())
            this.mergeConflicts = null;
    }
    
    public boolean isResolvingConflict() {
        return this.resolvingConflict;
    }
    
    public void setResolvingConflict(boolean resolvingConflict) {
        this.resolvingConflict = resolvingConflict;
    }

    public Conflicts getMergeConflicts() {
        return this.mergeConflicts;
    }
	
	public Map<?, ?> getEntityCache() {
		return this.entityCache;
	}
	
	public IdentityHashMap<Object, Object> saveEntityCache() {
		IdentityHashMap<Object, Object> entityCache = this.entityCache;
		this.entityCache = new IdentityHashMap<Object, Object>();
		return entityCache;
	}
	public void restoreEntityCache(IdentityHashMap<Object, Object> entityCache) {
		this.entityCache = entityCache;
	}	

    public String getExternalDataSessionId() {
        return this.externalDataSessionId;
    }

    public void setExternalDataSessionId(String externalDataSessionId) {
        this.externalDataSessionId = externalDataSessionId;
    }
    
    public void setServerSession(ServerSession serverSession) {
    	this.serverSession = serverSession;
    }
    
    public ServerSession getServerSession() {
    	return serverSession;
    }

    public void setSourceEntityManager(EntityManager sourceEntityManager) {
        this.sourceEntityManager = sourceEntityManager;
    }
    
    public EntityManager getSourceEntityManager() {
        return this.sourceEntityManager;
    }
    
    public boolean isMergeUpdate() {
        return this.mergeUpdate;
    }
    
    public void setMergeUpdate(boolean mergeUpdate) {
        this.mergeUpdate = mergeUpdate;
    }

    public boolean isMerging() {
        return this.merging;
    }
    
    public void setMerging(boolean merging) {
        this.merging = merging;
    }

    public boolean isSkipDirtyCheck() {
        return this.skipDirtyCheck;
    }
    
    public void setSkipDirtyCheck(boolean skipDirtyCheck) {
        this.skipDirtyCheck = skipDirtyCheck;
    }
    
    public Object getFromCache(Object obj) {
        if (this.entityCache == null)
            return null;
        return this.entityCache.get(obj);
    }
    
    public void pushMerge(Object obj, Object dest) {
    	pushMerge(obj, dest, true);
    }
    public void pushMerge(Object obj, Object dest, boolean push) {
        if (this.entityCache != null)
            this.entityCache.put(obj, dest);
        if (push)
        	this.mergeStack.push(dest);
    }
	public Object getCachedMerge(Object obj) {
		return this.entityCache.get(obj);
	}
	public Object popMerge() {
		return this.mergeStack.pop();
	}
	public Object getCurrentMerge() {
		return this.mergeStack.peek();
	}
	public void setCurrentMerge(Object merge) {
		this.mergeStack.set(0, merge);
	}
	public int getMergeStackSize() {
		return this.mergeStack.size();
	}
	
	public Object getSavedProperties(Object object) {
		return dirtyCheckContext.getSavedProperties(object);
	}
	
	public Object getCachedObject(Object object) {
		return entityManager.getCachedObject(object, true);
	}
	
	public Object[] getOwnerEntity(Object entity) {
		return entityManager.getOwnerEntity(entity);
	}
	
	public boolean isUnsaved(Object object) {
		return dirtyCheckContext.isUnsaved(object);
	}
    
    public void clearCache() {
        this.entityCache = null;
    }
    
    private Set<Object> getVersionChangeCache() {
        if (this.versionChangeCache == null)
            this.versionChangeCache = new HashSet<Object>();
        return this.versionChangeCache;
    }
    
    public void markVersionChanged(Object obj) {
        getVersionChangeCache().add(obj);
    }
    
    public boolean hasVersionChanged(Object obj) {
        return this.versionChangeCache != null ? this.versionChangeCache.contains(obj) : false;
    }

    public void setUninitializing(boolean uninitializing) {
        this.uninitializing = uninitializing;
    }

    public boolean isUninitializing() {
        return this.uninitializing;
    }

    public boolean isUninitializeAllowed() {
        return this.entityManager.isUninitializeAllowed();
    }
}
