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

import java.util.Map.Entry;

import org.granite.client.tide.ContextAware;
import org.granite.client.tide.EventBus;
import org.granite.client.tide.NameAware;
import org.granite.client.tide.Application;
import org.granite.client.tide.ApplicationConfigurable;
import org.granite.client.tide.data.Conflicts;
import org.granite.client.tide.data.DataConflictListener;
import org.granite.client.tide.data.EntityManager;
import org.granite.client.tide.data.EntityManager.UpdateKind;
import org.granite.client.tide.impl.SimpleContextManager;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * @author William DRAI
 */
public class SpringContextManager extends SimpleContextManager implements ApplicationContextAware, BeanPostProcessor, BeanFactoryPostProcessor {
	
	private ApplicationContext applicationContext;
	
	public SpringContextManager(Application application) {
		super(application, new SpringEventBus());
	}

	public SpringContextManager(Application application, EventBus eventBus) {
		super(application, eventBus);
	}
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
		setInstanceStoreFactory(new SpringInstanceStoreFactory(applicationContext));
        if (eventBus instanceof SpringEventBus)
            ((SpringEventBus)eventBus).setApplicationContext(applicationContext);
	}
	
	@Override
	public Object postProcessBeforeInitialization(Object instance, String name) throws BeansException {
    	if (name != null && instance instanceof NameAware)
    		((NameAware)instance).setName(name);
    	if (instance instanceof ContextAware)
    		((ContextAware)instance).setContext(getContext());
    	if (instance.getClass().isAnnotationPresent(ApplicationConfigurable.class))
    		application.configure(instance);
    	return instance;
	}
	
	@Override
	public Object postProcessAfterInitialization(Object instance, String name) throws BeansException {
		return instance;
	}
	
	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		beanFactory.registerSingleton("context", getContext());
		EntityManager entityManager = getContext().getEntityManager();
		entityManager.addListener(new SpringDataConflictListener());
		beanFactory.registerSingleton("entityManager", entityManager);
        beanFactory.registerSingleton("eventBus", eventBus);
		beanFactory.registerSingleton("dataManager", getContext().getDataManager());
		for (Entry<String, Object> entry : getContext().getInitialBeans().entrySet())
		    beanFactory.registerSingleton(entry.getKey(), entry.getValue());
		beanFactory.registerScope("view", new ViewScope());
	}


    private final class SpringDataConflictListener implements DataConflictListener {
		@Override
		public void onConflict(EntityManager entityManager, Conflicts conflicts) {
			TideApplicationEvent event = new TideApplicationEvent(getContext(null), UpdateKind.CONFLICT.eventName(), conflicts);
			applicationContext.publishEvent(event);
		}
	}
}
