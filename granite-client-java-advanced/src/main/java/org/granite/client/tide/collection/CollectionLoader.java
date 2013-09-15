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
package org.granite.client.tide.collection;

import org.granite.client.persistence.Loader;
import org.granite.client.persistence.collection.PersistentCollection;
import org.granite.client.persistence.collection.PersistentCollection.InitializationCallback;
import org.granite.client.tide.data.EntityManager;
import org.granite.client.tide.data.PersistenceManager;
import org.granite.client.tide.server.ServerSession;


/**
 *  Internal implementation of persistent collection handling automatic lazy loading.<br/>
 *  Used for wrapping persistent collections received from the server.<br/>
 *  Should not be used directly.
 * 
 *  @author William DRAI
 */
public class CollectionLoader implements Loader<PersistentCollection> {
    
    private final ServerSession serverSession;
    
    private final Object entity;
	private final String propertyName;
    
    private boolean localInitializing = false;
    private boolean initializing = false;
    @SuppressWarnings("unused")
    private InitializationCallback initializationCallback = null;
    
    
	public CollectionLoader(ServerSession serverSession, Object entity, String propertyName) {
    	this.serverSession = serverSession;
        this.entity = entity;
        this.propertyName = propertyName;
    }
    
    public boolean isInitializing() {
        return initializing;
    }
    
    public void onInitializing() {
        localInitializing = true;
    }
    
    public void onInitialize() {
    	localInitializing = false;
    }
    
    public void onUninitialize() {
        initializing = false;
        localInitializing = false;
        initializationCallback = null;
    }
    
    public void load(PersistentCollection collection, InitializationCallback callback) {
        if (localInitializing)
            return;
        
        this.initializationCallback = callback;
        
        EntityManager entityManager = PersistenceManager.getEntityManager(entity);
        if (!initializing && entityManager.initializeObject(serverSession, entity, propertyName, collection))                
            initializing = true;
    }
}