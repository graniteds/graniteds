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

package org.granite.client.tide.data.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.granite.client.messaging.RemoteService;
import org.granite.client.messaging.ResultFaultIssuesResponseListener;
import org.granite.client.messaging.events.FaultEvent;
import org.granite.client.messaging.events.IssueEvent;
import org.granite.client.messaging.events.ResultEvent;
import org.granite.client.tide.Context;
import org.granite.client.tide.data.EntityManager;
import org.granite.client.tide.data.PersistenceManager;
import org.granite.client.tide.data.RemoteInitializer;
import org.granite.client.tide.server.ServerSession;
import org.granite.logging.Logger;
import org.granite.tide.Expression;
import org.granite.tide.invocation.InvocationCall;
import org.granite.tide.invocation.InvocationResult;

/**
 * @author William DRAI
 */
public class RemoteInitializerImpl implements RemoteInitializer {
	
	private static final Logger log = Logger.getLogger(RemoteInitializerImpl.class);
	
	private final Context context;
	private boolean enabled = true;

	
	public RemoteInitializerImpl(Context context) {
		this.context = context;
	}

	@Override
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}
	
	private List<Object[]> objectsInitializing = new ArrayList<Object[]>();
    
	/**
	 * 	{@inheritDoc}
	 */
    public boolean initializeObject(ServerSession serverSession, Object entity, String propertyName, Object object) {
		if (!enabled || context.isFinished())
			return false;
		
		log.debug("initialize {0}", ObjectUtil.toString(object));
		
		EntityManager entityManager = PersistenceManager.getEntityManager(entity);
		if (entityManager == null)
			return false;
		
		Expression path = null;
		
		if (context.getContextId() != null && context.isContextIdFromServer())
			path = entityManager.getReference(entity, false, new HashSet<Object>());
		
		entityManager.addReference(entity, null, null, null);
		
		synchronized (objectsInitializing) {
			objectsInitializing.add(new Object[] { context, path != null ? path.getPath() : entity, propertyName });
		}
		
		context.callLater(new DoInitializeObjects(serverSession));
		return true;
	}
    
    public class DoInitializeObjects implements Runnable {
    	
    	private final ServerSession serverSession;
    	
    	public DoInitializeObjects(ServerSession serverSession) {
    		this.serverSession = serverSession;
    	}
    	
    	public void run() {
	    	Map<Object, List<String>> initMap = new HashMap<Object, List<String>>();
			
	    	synchronized (objectsInitializing) {
				for (int i = 0; i < objectsInitializing.size(); i++) {
					if (objectsInitializing.get(i)[0] != context)
						continue;
					
					List<String> propertyNames = initMap.get(objectsInitializing.get(i)[1]);
					if (propertyNames == null) {
						propertyNames = Arrays.asList((String)objectsInitializing.get(i)[2]);
						initMap.put(objectsInitializing.get(i)[1], propertyNames);
					}
					else
						propertyNames.add((String)objectsInitializing.get(i)[2]);
					
					objectsInitializing.remove(i--);
				}
	    	}
			
	    	RemoteService rs = serverSession.getRemoteService();
			for (Object entity : initMap.keySet()) {
				rs.newInvocation("initializeObject", entity, initMap.get(entity).toArray(), new InvocationCall())
					.addListener(new InitializerListener(serverSession, entity)).invoke();
				
			}
    	}
	}
	
    
    public class InitializerListener extends ResultFaultIssuesResponseListener {
    	
    	private final ServerSession serverSession;
    	private final Object entity;
    	
    	public InitializerListener(ServerSession serverSession, Object entity) {
    		this.serverSession = serverSession;
    		this.entity = entity;
    	}

		@Override
		public void onResult(final ResultEvent event) {
			context.callLater(new Runnable() {
				public void run() {
					EntityManager entityManager = PersistenceManager.getEntityManager(entity);
					
					boolean saveUninitializeAllowed = entityManager.isUninitializeAllowed();
					try {
						entityManager.setUninitializeAllowed(false);
						
						// Assumes objects is a PersistentCollection or PersistentMap
						serverSession.handleResult(context, null, null, (InvocationResult)event.getResult(), ((InvocationResult)event.getResult()).getResult(), null);
					}
					finally {
						entityManager.setUninitializeAllowed(saveUninitializeAllowed);
					}
				}
			});
		}

		@Override
		public void onFault(final FaultEvent event) {
			context.callLater(new Runnable() {
				public void run() {
					log.error("Fault initializing collection " + ObjectUtil.toString(entity) + " " + event.toString());
					
					serverSession.handleFault(context, null, null, event.getMessage());
				}
			});
		}   	

		@Override
		public void onIssue(final IssueEvent event) {
			context.callLater(new Runnable() {
				public void run() {
					log.error("Fault initializing collection " + ObjectUtil.toString(entity) + " " + event.toString());
					
					serverSession.handleFault(context, null, null, null);
				}
			});
		}   	
    }
}