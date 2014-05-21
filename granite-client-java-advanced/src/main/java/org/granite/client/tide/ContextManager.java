/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2014 GRANITE DATA SERVICES S.A.S.
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
package org.granite.client.tide;

import java.util.List;

/**
 * Main interface for Tide context management
 * The context manager is meant to be a singleton in the application and should be defined as a singleton in a DI container
 *
 * <pre>
 * {@code
 * ContextManager contextManager = new SimpleContextManager();
 * Context context = contextManager.getContext();
 * ...
 * }
 * </pre>
 *
 * @author William DRAI
 */
public interface ContextManager {

    /**
     * Get the global context
     * @return global context
     */
    public Context getContext();

    /**
     * Get the context from its id
     * @param contextId context id
     * @return context
     */
    public Context getContext(String contextId);

    /**
     * Get a context from its id with the specified parent id, and create it if it does not exist
     * @param contextId context id
     * @param parentContextId parent context id
     * @param create if true, create a context if not exist
     * @return the context
     */
    public Context getContext(String contextId, String parentContextId, boolean create);

    /**
     * Create a context with the specified id and parent id if it does not exist
     * @param contextId context id
     * @param parentContextId parent context id
     * @return the created context
     */
    public Context newContext(String contextId, String parentContextId);
    
    /**
     * True if context is global context
     * @param context context
     * @return true if global context
     */
    public boolean isGlobal(Context context);

    /**
     * Get or create the context for the specified context id and server conversation flags
     * @param sourceContext source context
     * @param contextId conversation context id
     * @param wasConversationCreated true if the conversation was just created by the last request on the server
     * @param wasConversationEnded true if the conversation was just ended by the last request on the server
     * @return the matching context
     */
    public Context retrieveContext(Context sourceContext, String contextId, boolean wasConversationCreated, boolean wasConversationEnded);

    /**
     * Update the context id for an existing context
     *
     * @param previousContextId previous context id
     * @param context context to update
     */
    public void updateContextId(String previousContextId, Context context);

    /**
     * Destroy a context
     *
     * @param contextId context id
     */
    public void destroyContext(String contextId);

    /**
     * Get a list of all conversation contexts
     *
     * @return list of conversation contexts
     */
    public List<Context> getAllContexts();
    
    // function forEachChildContext(parentContext:Context, callback:Function, token:Object = null):void;

    /**
     *  Destroy all contexts
     */
    public void destroyContexts();

    /**
     *  Destroy finished contexts and reset current pending contexts
     */
    public void destroyFinishedContexts();

    /**
     * Schedule a context for destruction after the next remote call
     * @param contextId context id
     */
    public void addToContextsToDestroy(String contextId);

    /**
     * Deschedule destruction of context
     * @param contextId context id
     */
    public void removeFromContextsToDestroy(String contextId);
}
