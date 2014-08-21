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

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.granite.client.tide.Context;
import org.granite.client.tide.InstanceStore;
import org.granite.client.tide.InstanceStoreFactory;
import org.granite.client.tide.impl.InstanceFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * @author William DRAI
 */
public class SpringInstanceStoreFactory implements InstanceStoreFactory {
	
	private final ApplicationContext applicationContext;
	
	public SpringInstanceStoreFactory(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	@Override
	public InstanceStore createStore(Context context, InstanceFactory instanceFactory) {
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
		
		public void init() {
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
		public void remove(Object instance) {
			// Nothing, managed by Spring
		}
		
		@Override
		public boolean exists(String name) {
			if (isInactive())
				return false;
			
			return applicationContext.containsBean(name);
		}
		
		@Override
		public void inject(Object instance, String name, Map<String, Object> properties) {
			// Nothing, managed by Spring
		}
		
		@Override
		public void clear() {
			// Nothing, managed by Spring
		}

		@Override
		public List<String> allNames() {
			if (isInactive())
				return new ArrayList<String>();
			
			return Arrays.asList(applicationContext.getBeanDefinitionNames());
		}

        @SuppressWarnings("unchecked")
        @Override
        public <T> T getNoProxy(String name, Context context) {
			if (isInactive())
				return null;
			
            return (T)applicationContext.getBean(name);
        }

        @SuppressWarnings("unchecked")
		@Override
		public <T> T byName(String name, Context context) {
            try {
				if (isInactive())
					return null;
				
    			return (T)applicationContext.getBean(name);
            }
            catch (NoSuchBeanDefinitionException e) {
            }
            catch (IllegalStateException e) {
            	// ApplicationContext stopped, callback may have happened during app shutdown
            }
            return null;
		}

		@Override
		public <T> T byType(Class<T> type, Context context) {
            try {
				if (isInactive())
					return null;
				
			    return applicationContext.getBean(type);
            }
            catch (NoSuchBeanDefinitionException e) {
            }
            catch (IllegalStateException e) {
            	// ApplicationContext stopped, callback may have happened during app shutdown
            }
            return null;
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public <T> T[] allByType(Class<T> type, Context context, boolean create) {
			try {
				if (isInactive())
					return (T[])Array.newInstance(type, 0);
				
				Map<String, T> instancesMap = applicationContext.getBeansOfType(type, true, create);
				T[] all = (T[])Array.newInstance(type, instancesMap.size());
				return instancesMap.values().toArray(all);
			}
            catch (IllegalStateException e) {
            	// ApplicationContext stopped, callback may have happened during app shutdown
            }
			return null;
		}
		
		@Override
		public Map<String, Object> allByAnnotatedWith(Class<? extends Annotation> annotationClass, Context context) {
			try {
				if (isInactive())
					return new HashMap<String, Object>();
				
				return applicationContext.getBeansWithAnnotation(annotationClass);
			}
            catch (IllegalStateException e) {
            	// ApplicationContext stopped, callback may have happened during app shutdown
            }
			return Collections.emptyMap();
		}
		
		private boolean isInactive() {
			return (
				applicationContext instanceof ConfigurableApplicationContext &&
				!((ConfigurableApplicationContext)applicationContext).isActive()
			);
		}
	}
}
