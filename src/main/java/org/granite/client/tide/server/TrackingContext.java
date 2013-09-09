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

package org.granite.client.tide.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.granite.client.tide.ScopeType;
import org.granite.client.tide.SyncMode;
import org.granite.logging.Logger;
import org.granite.tide.invocation.ContextResult;
import org.granite.tide.invocation.ContextUpdate;

/**
 * @author William DRAI
 */
public class TrackingContext {
    
    private static final Logger log = Logger.getLogger(TrackingContext.class);
    
    private boolean enabled = true;
    private List<ContextUpdate> updates = new ArrayList<ContextUpdate>();
    private List<ContextUpdate> pendingUpdates = new ArrayList<ContextUpdate>();
    private List<ContextResult> results = new ArrayList<ContextResult>();
    private List<String> lastResults = new ArrayList<String>();

    /**
     *  Enable/disable tracking on the context
     * 
     *  @param enabled enable or disable tracking on the current context
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    /**
     *  Return tracking mode
     * 
     *  @return true if tracking enabled
     */
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     *  @return current list of updates that will be sent to the server
     */
    public List<ContextUpdate> getUpdates() {
        return updates;
    }
    
    /**
     *  @return current list of results that will be requested from the server
     */
    public List<ContextResult> getResults() {
        return results;
    }       
    
    /**
     *  Reset the context
     */
    public void clear() {
        updates.clear();
        pendingUpdates.clear();
        results.clear();
        lastResults.clear();
    }
    
    /**
     *  Reset the current updates and optionally saves them as pending
     * 
     *  @param savePending moves existing updates to pending before clearing
     */
    public void clearUpdates(boolean savePending) {
        if (savePending)
            pendingUpdates = new ArrayList<ContextUpdate>(updates);
        updates.clear();
    }
    
    /**
     *  Reset the current pending updates
     */
    public void clearPendingUpdates() {
        pendingUpdates.clear();
        lastResults.clear();
    }
    
    /**
     *  Filter the current updates with a function
     */
    public void filterUpdates(UpdateFilter filter) {           
        // Keep only updates for identity component
        for (int i = 0; i < updates.size(); i++) {
            ContextUpdate u = updates.get(i);
            if (!filter.accept(u)) {
                updates.remove(i);
                i--;
            }
        }
    }
        
    public static interface UpdateFilter {        
        public boolean accept(ContextUpdate u);        
    }
    
    
    /**
     *  Add update to current context 
     *  Note: always move the update in last position in case the value can depend on previous updates
     * 
     *  @param componentName name of the component/context variable or null if typed component reference
     *  @param componentClassName class name of the component/context variable or null if untyped component name
     *  @param expr EL expression to evaluate
     *  @param value value to send to server
     */
    public void addUpdate(String componentName, String componentClassName, String expr, Object value) {
        internalAddUpdate(componentName, componentClassName, expr, value, ScopeType.EVENT, SyncMode.NONE, componentName == null);             
    }
    
    /**
     *  Add update to current context 
     *  Note: always move the update in last position in case the value can depend on previous updates
     * 
     *  @param componentName name of the component/context variable or null if typed component reference
     *  @param componentClassName class name of the component/context variable or null if untyped component name
     *  @param expr EL expression to evaluate
     *  @param value value to send to server
     *  @param scope scope of the result
     *  @param sync remote sync mode of the result
     *  @param typed component name represents a typed component instance  
     */
    protected void internalAddUpdate(String componentName, String componentClassName, String expr, Object value, ScopeType scope, SyncMode sync, boolean typed) {
        if (!enabled)
            return;
        
        boolean found = false;
        for (int i = 0; i < updates.size(); i++) {
            ContextUpdate u = updates.get(i);
            if (u.getComponentName() == componentName && u.getComponentClassName() == componentClassName && u.getExpression() == expr) {
                u.setValue(value);
                if (i < updates.size()-1) {
                    found = false;
                    updates.remove(i);    // Remove here to add it in last position
                    i--;
                }
                else
                    found = true;
            }
            else if (u.getComponentName() == componentName && u.getComponentClassName() == componentClassName && u.getExpression() != null && (expr == null || u.getExpression().indexOf(expr + ".") == 0)) {
                updates.remove(i);
                i--;
            }
            else if (u.getComponentName() == componentName && u.getComponentClassName() == componentClassName && expr != null && (u.getExpression() == null || expr.indexOf(u.getExpression() + ".") == 0))
                found = true;
        }
        
        if (!found) {
            log.debug("add new update {0}", (componentName != null ? componentName : "") + (componentClassName != null ? "(" + componentClassName + ")" : "") + (expr != null ? "." + expr : ""));
            ContextUpdate cu = new ContextUpdate(componentName, expr, value, scope.ordinal(), false);
            cu.setComponentClassName(componentClassName);
            updates.add(cu);
        }
    }
    
    /**
     *  Add result evaluator in current context
     * 
     *  @param componentName name of the component/context variable
     *  @param componentClassName class name of the component/context variable
     *  @param expr EL expression to evaluate
     *  @param instance current instance of the component
     * 
     *  @return true if the result was not already present in the current context 
     */
    public boolean addResult(String componentName, String componentClassName, String expr, Object instance) {
        return internalAddResult(componentName, componentClassName, expr, instance, ScopeType.EVENT, SyncMode.NONE);
    }
    
    /**
     *  Add result evaluator in current context
     * 
     *  @param componentName name of the component/context variable
     *  @param componentClassName class name of the component/context variable
     *  @param expr EL expression to evaluate
     *  @param instance current instance of the component
     *  @param scope scope of the update
     *  @param sync remote sync mode of the update
     * 
     *  @return true if the result was not already present in the current context 
     */
    protected boolean internalAddResult(String componentName, String componentClassName, String expr, Object instance, ScopeType scope, SyncMode sync) {
        if (!enabled || sync == SyncMode.NONE || (instance == null && expr == null))
            return false;
        
        // Check in existing results
        for (ContextResult r : results) {
            if (r.getComponentName() == componentName && r.getComponentClassName() == componentClassName && r.getExpression() == expr)
                return false;
        }
        
        // Check in last received results
        String e = componentName + (componentClassName != null ? "(" + componentClassName + ")" : "") + (expr != null ? "." + expr : "");
        if (lastResults.indexOf(e) >= 0)
            return false;
        
        log.debug("add new result {0}", e);
        // TODO: should store somewhere if the client componentName is the same as the server bean name
        ContextResult cr = new ContextResult(componentName, expr);
        cr.setComponentClassName(componentClassName);
        results.add(cr);
        return true;
    }
    
    
    public void addLastResult(String res) {
        lastResults.add(res);
    }
    
    
    public void removeResults(List<ContextUpdate> rmap) {
        // Remove all received results from current results list
        List<ContextResult> newResults = new ArrayList<ContextResult>();
        for (ContextResult cr : this.results) {
            boolean found = false;
            for (ContextUpdate u : rmap) {
                if (cr.matches(u.getComponentName(), u.getComponentClassName(), u.getExpression())) {
                    found = true;
                    break;
                }
            }
            if (!found)
                newResults.add(cr);
        }
        this.results = newResults;
    }
    
    
    /**
     *  Trace the current tracking context
     */
    public void traceContext() {
        log.debug("updates: %s", updates.toString());
        log.debug("results: %s", results.toString());
    }
    
    
    /**
     *  Reset current tracking context and returns saved context
     * 
     *  @return saved tracking context
     */
    public Map<String, Object> saveAndResetContext() {
        Map<String, Object> savedTrackingContext = new HashMap<String, Object>();
        savedTrackingContext.put("updates", new ArrayList<ContextUpdate>(updates));
        savedTrackingContext.put("results", new ArrayList<ContextResult>(results));
        savedTrackingContext.put("pendingUpdates", new ArrayList<ContextUpdate>(pendingUpdates));
        savedTrackingContext.put("lastResults", new ArrayList<String>(lastResults));
        
        updates.clear();
        pendingUpdates.clear();
        results.clear();
        lastResults.clear();
        
        return savedTrackingContext;
    }
    
    /**
     *  Restore tracking context
     * 
     *  @param trackingContext object containing the current call context
     */ 
    @SuppressWarnings("unchecked")
    public void restoreContext(Map<String, Object> trackingContext) {
        updates = (List<ContextUpdate>)trackingContext.get("updates");
        results = (List<ContextResult>)trackingContext.get("results");
        pendingUpdates = (List<ContextUpdate>)trackingContext.get("pendingUpdates");
        lastResults = (List<String>)trackingContext.get("lastResults");
    }
}
