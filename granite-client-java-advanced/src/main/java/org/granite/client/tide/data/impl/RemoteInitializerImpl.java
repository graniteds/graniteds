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
package org.granite.client.tide.data.impl;

import java.util.ArrayList;
import java.util.HashMap;
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
import org.granite.client.tide.impl.FaultHandler;
import org.granite.client.tide.impl.ResultHandler;
import org.granite.client.tide.server.ServerSession;
import org.granite.logging.Logger;
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
		
		entityManager.addReference(entity, null, null);
		
		synchronized (objectsInitializing) {
			objectsInitializing.add(new Object[] { context, entity, propertyName });
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
						propertyNames = new ArrayList<String>();
						propertyNames.add((String)objectsInitializing.get(i)[2]);
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
                        new ResultHandler<Object>(serverSession, null, null).handleResult(context, (InvocationResult) event.getResult(), ((InvocationResult) event.getResult()).getResult(), null);
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

                    new FaultHandler<Object>(serverSession, null, null).handleFault(context, event.getMessage());
				}
			});
		}   	

		@Override
		public void onIssue(final IssueEvent event) {
			context.callLater(new Runnable() {
				public void run() {
					log.error("Fault initializing collection " + ObjectUtil.toString(entity) + " " + event.toString());

                    new FaultHandler<Object>(serverSession, null, null).handleFault(context, null);
				}
			});
		}   	
    }
}