/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2015 GRANITE DATA SERVICES S.A.S.
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
package org.granite.client.tide.cdi;

import java.lang.annotation.Annotation;

import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;

import org.granite.client.tide.ViewScope;
import org.granite.client.tide.ViewScope.BeanResetter;
import org.granite.client.tide.ViewScopeHolder;

/**
 * @author William DRAI
 */
public class ViewContext implements Context {
	
	// private static final Logger log = Logger.getLogger(ViewScope.class);
	
	private ViewScope beanCache;
	
	public ViewContext() {
	}
	
	private ViewScope getBeanCache() {
		if (beanCache == null) {
			beanCache = ViewScopeHolder.get();
			if (beanCache == null)
				throw new RuntimeException("View bean cache not set");
		}
		return beanCache;
	}
	
	@Override
	public <T> T get(Contextual<T> bean) {
		return get(bean, null);
	}

	@Override
	public <T> T get(Contextual<T> bean, CreationalContext<T> cc) {
		if (!isActive())
			throw new ContextNotActiveException();
		
		if (bean == null)
			throw new IllegalArgumentException("bean cannot be null");
		
		String id = buildId(bean);
		@SuppressWarnings("unchecked")
		T instance = (T)getBeanCache().get(id);
		if (instance != null)
			return instance;
		
		if (cc == null)
			return null;
		
		instance = bean.create(cc);
		getBeanCache().put(id, instance);
		getBeanCache().addResetter(id, new ViewBeanResetter<T>(bean, cc));
		
		return instance;
	}
	
	private <T> String buildId(Contextual<T> contextual) {
		return contextual.getClass().getName() + "#" + contextual.hashCode();
	}
	
	@Override
	public Class<? extends Annotation> getScope() {
		return ViewScoped.class;
	}
	
	@Override
	public boolean isActive() {
		return getBeanCache() != null;
	}
	
	public static class ViewBeanResetter<T> implements BeanResetter {
		
		private final Contextual<T> bean;
		private final CreationalContext<T> cc;
		
		public ViewBeanResetter(Contextual<T> bean, CreationalContext<T> cc) {
			this.bean = bean;
			this.cc = cc;
		}
		
		@SuppressWarnings("unchecked")
		public void reset(Object instance) {
			bean.destroy((T)instance, cc);
		}
		
	}
	
}

