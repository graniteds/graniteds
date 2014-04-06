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
package org.granite.client.tide.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.granite.client.tide.Application;
import org.granite.client.tide.Context;
import org.granite.client.tide.ContextManager;
import org.granite.client.tide.EventBus;
import org.granite.client.tide.InstanceStore;
import org.granite.client.tide.InstanceStoreFactory;

/**
 * @author William DRAI
 */
public class SimpleContextManager implements ContextManager {
    
    static final String DEFAULT_CONTEXT = "__DEFAULT__CONTEXT__";
    
    public static final String CONTEXT_CREATE = "org.granite.tide.contextCreate";
    public static final String CONTEXT_DESTROY = "org.granite.tide.contextDestroy";
    
    protected final Application application;
    protected final EventBus eventBus;
    private InstanceStoreFactory instanceStoreFactory = new DefaultInstanceStoreFactory();
    private Map<String, Context> contextsById = new HashMap<String, Context>();
    private List<String> contextsToDestroy = new ArrayList<String>();
    
    
    public SimpleContextManager() {
    	// CDI proxying...
    	this.application = new DefaultApplication();
    	this.eventBus = new SimpleEventBus();
    }

    /**
     * Create a simple context manager using the specified application
     * @param application application
     */
    public SimpleContextManager(Application application) {
    	this.application = application;
    	this.eventBus = new SimpleEventBus();
    }

    /**
     * Create a simple context manager using the specified application and event bus
     * @param application application
     * @param eventBus event bus
     */
    public SimpleContextManager(Application application, EventBus eventBus) {
    	this.application = application;
    	this.eventBus = eventBus;
    }

    /**
     * Set the instance store factory for this context manager
     * @param instanceStoreFactory instance store factory
     */
    public void setInstanceStoreFactory(InstanceStoreFactory instanceStoreFactory) {
    	this.instanceStoreFactory = instanceStoreFactory;
    }
    
    public static class DefaultInstanceStoreFactory implements InstanceStoreFactory {
		@Override
		public InstanceStore createStore(Context context) {
			return new SimpleInstanceStore(context);
		}    	
    }
    
    
    /**
     *  Determine if the specified context is the global one
     *  
     *  @param context
     *  @return true if global
     */
    public boolean isGlobal(Context context) {
        return contextsById.get(DEFAULT_CONTEXT) == context;
    }

    public Context getContext() {
    	return getContext(null, null, true);
    }
    
    public Context getContext(String contextId) {
        return getContext(contextId, null, true);
    }
    
    
    protected Context createContext(Context parentCtx, String contextId) {
        Context ctx = new Context(this, parentCtx, contextId);
        ctx.initContext(application, eventBus, instanceStoreFactory.createStore(ctx));
        return ctx;
    }
    
    public Context getContext(String contextId, String parentContextId, boolean create) {
        Context ctx = contextsById.get(contextId != null ? contextId : DEFAULT_CONTEXT);
        if (ctx == null && create) {
            Context parentCtx = contextsById.get(parentContextId == null ? DEFAULT_CONTEXT : parentContextId);
            if (parentContextId != null && parentCtx == null)
                throw new IllegalStateException("Parent context not found for id " + parentContextId);
            
            ctx = createContext(parentCtx, contextId);
            contextsById.put(contextId != null ? contextId : DEFAULT_CONTEXT, ctx);
            if (contextId != null)
            	ctx.getEventBus().raiseEvent(ctx, CONTEXT_CREATE);
            ctx.postInit();
        }
        return ctx;
    }

    public Context newContext(String contextId, String parentContextId) {
        Context ctx = contextsById.get(contextId != null ? contextId : DEFAULT_CONTEXT);
        if (ctx != null && ctx.isFinished()) {
            ctx.clear();
            contextsById.remove(contextId);
            removeFromContextsToDestroy(contextId);
            ctx = null;
        }
        if (ctx == null) {
            Context parentCtx = contextsById.get(parentContextId != null ? parentContextId : DEFAULT_CONTEXT);
            ctx = createContext(parentCtx, contextId);
            if (contextId != null)
                contextsById.put(contextId, ctx);
            ctx.getEventBus().raiseEvent(ctx, CONTEXT_CREATE);
            ctx.postInit();
        }
        return ctx;
    }
    
    public void destroyContext(String contextId) {
        Context ctx = contextId != null ? contextsById.get(contextId) : null;
        if (ctx != null) {
            // Destroy child contexts
            for (Context c : contextsById.values()) {
                if (c.getParentContext() == ctx)
                    destroyContext(c.getContextId());
            }
            
            removeFromContextsToDestroy(contextId);
            ctx.getEventBus().raiseEvent(ctx, CONTEXT_DESTROY);
            contextsById.get(contextId).clear();
            contextsById.remove(contextId);
        }
    }     
    
    public List<Context> getAllContexts() {
        List<Context> contexts = new ArrayList<Context>();
        for (Entry<String, Context> ectx : contextsById.entrySet()) {
            if (!ectx.getKey().equals(DEFAULT_CONTEXT))
                contexts.add(ectx.getValue());
        }
        return contexts;
    }       
    
//    /**
//     *  Execute a function for each conversation context
//     * 
//     *  @param parentContext parent context
//     *  @param callback callback function
//     *  @param token token passed to the function
//     */
//    public function forEachChildContext(parentContext:Context, callback:Function, token:Object = null):void {
//        for each (var ctx:Context in _ctx) {
//            if (ctx.meta_parentContext === parentContext) {
//                if (token)
//                    callback(ctx, token);
//                else
//                    callback(ctx);
//            }
//        }
//    }       
    
    public void destroyContexts() {
        contextsToDestroy.clear();
        
        Context globalCtx = contextsById.get(DEFAULT_CONTEXT);
        List<String> contextIdsToDestroy = new ArrayList<String>();
        for (Entry<String, Context> ectx : contextsById.entrySet()) {
            if (!ectx.getKey().equals(DEFAULT_CONTEXT) && ectx.getValue().getParentContext() == globalCtx)
                contextIdsToDestroy.add(ectx.getKey());
        }
        for (String contextId : contextIdsToDestroy)
        	destroyContext(contextId);
        
        globalCtx.clear();
    }
    
    public void destroyFinishedContexts() {
        for (String contextId : contextsToDestroy)
            destroyContext(contextId);
        contextsToDestroy.clear();
    }
    
    
    /**
     *  Remove context from the list of contexts to destroy
     *  
     *  @param contextId context id
     */
    public void removeFromContextsToDestroy(String contextId) {
        int idx = contextsToDestroy.indexOf(contextId);
        if (idx >= 0)
            contextsToDestroy.remove(idx);
    }
    
    /**
     *  Add context to the list of contexts to destroy
     *  
     *  @param contextId context id
     */
    public void addToContextsToDestroy(String contextId) {
        if (contextsToDestroy.contains(contextId))
            return;
        contextsToDestroy.add(contextId);
    }
    
    
    public Context retrieveContext(Context sourceContext, String contextId, boolean wasConversationCreated, boolean wasConversationEnded) {
        Context context = null;
        if (!isGlobal(sourceContext) && contextId == null && wasConversationEnded) {
            // The conversation of the source context was ended
            // Get results in the current conversation when finished
            context = sourceContext;
            context.markAsFinished();
        }
        else if (!isGlobal(sourceContext) && contextId == null && !sourceContext.isContextIdFromServer()) {
            // A call to a non conversational component was issued from a conversation context
            // Get results in the current conversation
            context = sourceContext;
        }
        else if (!isGlobal(sourceContext) && contextId != null
            && (sourceContext.getContextId() == null || (!sourceContext.getContextId().equals(contextId) && !wasConversationCreated))) {
            // The conversationId has been updated by the server
            String previousContextId = sourceContext.getContextId();
            context = sourceContext;
            context.setContextId(contextId, true);
            updateContextId(previousContextId, context);
        }
        else {
            context = getContext(contextId);
            if (contextId != null)
                context.setContextId(contextId, true);
        }
        
        return context;
    }
    
    /**
     *  Defines new context for existing id
     * 
     *  @param previousContextId existing id
     *  @param context new context
     */
    public void updateContextId(String previousContextId, Context context) {
        if (previousContextId != null)
            contextsById.remove(previousContextId);
        contextsById.put(context.getContextId(), context);
    }

}
