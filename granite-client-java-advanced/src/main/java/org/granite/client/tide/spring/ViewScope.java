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

