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

package org.granite.client.tide.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.granite.client.tide.Resettable;
import org.granite.client.tide.ViewScope;

/**
 * @author William DRAI
 */
public class DefaultViewScope implements ViewScope {
	
	private Map<String, Object> instanceCache = new ConcurrentHashMap<String, Object>();
		
	private GlobalResetter resetter = null;
	private Map<String, BeanResetter> resettersMap = new ConcurrentHashMap<String, BeanResetter>();
	
	private String viewId = null;
	
	public DefaultViewScope() {
	}
	
	public String getViewId() {
		return viewId;
	}
	public void setViewId(String viewId) {
		this.viewId = viewId;
	}
	
	public Object get(String name) {
		return instanceCache.get(name);
	}
	
	public void put(String name, Object instance) {
		instanceCache.put(name, instance);
	}
	
	public Object remove(String name) {
		Object instance = instanceCache.remove(name);
		
		if (instance instanceof Resettable)
			((Resettable)instance).reset();
		
		if (resettersMap.containsKey(name)) {
			resettersMap.get(name).reset(instance);
			resettersMap.remove(name);
		}
			
		if (resetter != null)
			resetter.reset(name, instance);
		
		return instance;
	}
	
	public void reset(Class<?> type) {
		List<String> names = new ArrayList<String>();
		
		for (Entry<String, Object> entry : instanceCache.entrySet()) {
			if (type.isInstance(entry.getValue()))
				names.add(entry.getKey());
		}
		
		for (String name : names)
			remove(name);
	}
	
	public void reset() {
		for (Entry<String, Object> entry : instanceCache.entrySet()) {
			if (entry.getValue() instanceof Resettable)
				((Resettable)entry.getValue()).reset();
			
			if (resettersMap.containsKey(entry.getKey()))
				resettersMap.get(entry.getKey()).reset(entry.getValue());
				
			if (resetter != null)
				resetter.reset(entry.getKey(), entry.getValue());
		}
		
		instanceCache.clear();
		resettersMap.clear();
	}
	
	public void setResetter(GlobalResetter resetter) {
		this.resetter = resetter;
	}

	public void addResetter(String name, BeanResetter resetter) {
		this.resettersMap.put(name, resetter);
	}
	
}
