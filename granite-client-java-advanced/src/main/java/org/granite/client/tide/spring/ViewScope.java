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
package org.granite.client.tide.spring;

import org.granite.client.tide.ViewScope.BeanResetter;
import org.granite.client.tide.ViewScopeHolder;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;

/**
 * @author William DRAI
 */
public class ViewScope implements Scope {
	
	// private static final Logger log = Logger.getLogger(ViewScope.class);
	
	private org.granite.client.tide.ViewScope beanCache;
	
	public ViewScope() {
	}
	
	private org.granite.client.tide.ViewScope getBeanCache() {
		if (beanCache == null) {
			beanCache = ViewScopeHolder.get();
			if (beanCache == null)
				throw new RuntimeException("View bean cache not set");
		}
		return beanCache;
	}
	
	@Override
	public String getConversationId() {
		return getBeanCache().getViewId();
	}
	
	@Override
	public Object get(String name, ObjectFactory<?> objectFactory) {
		Object instance = getBeanCache().get(name);
		if (instance == null) {
			instance = objectFactory.getObject();
			getBeanCache().put(name, instance);
		}
		return instance;
	}
	
	@Override
	public Object resolveContextualObject(String name) {
		return getBeanCache().get(name);
	}
	
	@Override
	public void registerDestructionCallback(String name, final Runnable callback) {
		getBeanCache().addResetter(name, new BeanResetter() {
			@Override
			public void reset(Object instance) {
				callback.run();
			}
		});
	}
	
	@Override
	public Object remove(String name) {
		return getBeanCache().remove(name);
	}
	
}

