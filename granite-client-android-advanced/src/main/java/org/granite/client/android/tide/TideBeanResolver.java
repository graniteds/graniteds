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
package org.granite.client.android.tide;

import org.granite.binding.android.BeanSetter;
import org.granite.binding.android.Binder.BeanResolver;
import org.granite.client.tide.Context;
import org.granite.client.tide.data.impl.JavaBeanDataManager;
import org.granite.util.TypeUtil;


public class TideBeanResolver implements BeanResolver {
	
	private final Context context;
	private final JavaBeanDataManager dataManager;
	
	private final BeanSetter<Object> beanSetter = new DataManagerBeanSetter();
	
	public TideBeanResolver(Context context) {
		this.context = context;
		this.dataManager = (JavaBeanDataManager)context.getEntityManager().getDataManager();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T resolveBean(Object ref) {
		if (ref instanceof String) {
			String[] path = ((String)ref).split("\\.");
			Object bean = context.byName(path[0]);
			if (path.length > 0) {
				for (int i = 1; i < path.length; i++)
					bean = TypeUtil.getProperty(bean, path[i]);
			}
			return (T)bean;
		}
		else if (ref instanceof Class<?>)
			return context.byType((Class<T>)ref);
		return (T)ref;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> BeanSetter<T> getBeanSetter(T bean, String propertyName) {
		return (BeanSetter<T>)beanSetter;
	}
	
	
	private class DataManagerBeanSetter implements BeanSetter<Object> {		
		@Override
		public void setValue(Object instance, String propertyName, Object value) throws Exception {
			dataManager.setPropertyValue(instance, propertyName, value);			
		}		
	}
}
