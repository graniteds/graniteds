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

package org.granite.tide.cdi;

import java.util.ArrayList;
import java.util.List;

import org.granite.tide.invocation.ContextEvent;
import org.granite.tide.invocation.ContextUpdate;


/**
 * @author William DRAI
 */
public class TideInvocation {
    
    private static ThreadLocal<TideInvocation> invocation = new ThreadLocal<TideInvocation>();
    
    public static TideInvocation get() {
        return invocation.get();
    }
    
    public static TideInvocation init() {
    	TideInvocation ti = new TideInvocation();
    	invocation.set(ti);
    	return ti;
    }
    
    public static void remove() {
        invocation.remove();
    }
    
    private boolean locked = false;
    private boolean enabled = false;
    private boolean updated = false;
    private boolean evaluated = false;
    private final List<ContextUpdate> updates = new ArrayList<ContextUpdate>();
    private final List<ContextUpdate> results = new ArrayList<ContextUpdate>();
    private final List<ContextEvent> events = new ArrayList<ContextEvent>();

    
    public List<ContextUpdate> getUpdates() {
        return updates;
    }
    
    public List<ContextUpdate> getResults() {
        return results;
    }
    
    public List<ContextEvent> getEvents() {
        return events;
    }
    
    public void update(List<ContextUpdate> updates) {
        this.enabled = true;
        this.updated = false;
        this.updates.clear();
        if (updates != null)
            this.updates.addAll(updates);
    }
    public void updated() {
        this.updated = true;
        this.updates.clear();
    }
    public boolean isUpdated() {
        return this.updated;
    }
    
    public void evaluate() {
        this.evaluated = false;
        this.results.clear();
    }
    public void evaluated(List<ContextUpdate> results) {
        this.evaluated = true;
//        this.results.clear();
        this.results.addAll(results);
        this.updated = false;
        this.updates.clear();
    }
    public boolean isEvaluated() {
        return this.evaluated;
    }
    
    public void addEvent(ContextEvent event) {
        events.add(event);
    }
    
    public void lock() {
        this.locked = true;
    }
    public void unlock() {
        this.locked = false;
    }
    public boolean isLocked() {
        return this.locked;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
}
