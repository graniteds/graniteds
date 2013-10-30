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
package org.granite.client.tide.spring;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.granite.client.tide.Context;
import org.granite.client.tide.InstanceStore;
import org.granite.client.tide.InstanceStoreFactory;
import org.springframework.context.ApplicationContext;

/**
 * @author William DRAI
 */
public class SpringInstanceStoreFactory implements InstanceStoreFactory {
	
	private final ApplicationContext applicationContext;
	
	public SpringInstanceStoreFactory(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	@Override
	public InstanceStore createStore(Context context) {
		return new SpringInstanceStore(context, applicationContext);
	}

	
	public static class SpringInstanceStore implements InstanceStore {
		
		@SuppressWarnings("unused")
		private final Context context;
		private final ApplicationContext applicationContext;
		
		public SpringInstanceStore(Context context, ApplicationContext applicationContext) {
			this.context = context;
			this.applicationContext = applicationContext;
		}
	    
		@Override
		public <T> T set(String name, T instance) {
			// Nothing, managed by Spring
			return instance;
		}

		@Override
		public <T> T set(T instance) {
			// Nothing, managed by Spring
			return instance;
		}

		@Override
		public void remove(String name) {
			// Nothing, managed by Spring
		}
		
		@Override
		public void clear() {
			// Nothing, managed by Spring
		}

		@Override
		public List<String> allNames() {
			return Arrays.asList(applicationContext.getBeanDefinitionNames());
		}

        @SuppressWarnings("unchecked")
        @Override
        public <T> T getNoProxy(String name, Context context) {
            return (T)applicationContext.getBean(name);
        }

        @SuppressWarnings("unchecked")
		@Override
		public <T> T byName(String name, Context context) {
			return (T)applicationContext.getBean(name);
		}

		@Override
		public <T> T byType(Class<T> type, Context context) {
			return applicationContext.getBean(type);
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public <T> T[] allByType(Class<T> type, Context context, boolean create) {
			Map<String, T> instancesMap = applicationContext.getBeansOfType(type, true, create);
			T[] all = (T[])Array.newInstance(type, instancesMap.size());
			return instancesMap.values().toArray(all);
		}
		
		@Override
		public Map<String, Object> allByAnnotatedWith(Class<? extends Annotation> annotationClass, Context context) {
			return applicationContext.getBeansWithAnnotation(annotationClass);
		}
	}
}
