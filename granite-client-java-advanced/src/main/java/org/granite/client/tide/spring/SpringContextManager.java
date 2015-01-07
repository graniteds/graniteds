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
package org.granite.client.tide.spring;

import java.util.Map.Entry;

import org.granite.client.tide.Application;
import org.granite.client.tide.ApplicationConfigurable;
import org.granite.client.tide.ContextAware;
import org.granite.client.tide.EventBus;
import org.granite.client.tide.NameAware;
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
import org.springframework.context.ConfigurableApplicationContext;

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
	
	public SpringContextManager(ConfigurableApplicationContext applicationContext, Application application) {
		super(application, new SpringEventBus());
		setApplicationContext(applicationContext);
	}

	public SpringContextManager(ConfigurableApplicationContext applicationContext, Application application, EventBus eventBus) {
		super(application, eventBus);
		setApplicationContext(applicationContext);
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
		if (!beanFactory.containsSingleton("context"))
			beanFactory.registerSingleton("context", getContext());
		if (!beanFactory.containsSingleton("entityManager")) {
			EntityManager entityManager = getContext().getEntityManager();
			entityManager.addListener(new SpringDataConflictListener());
			beanFactory.registerSingleton("entityManager", entityManager);
		}
		if (!beanFactory.containsSingleton("eventBus"))
			beanFactory.registerSingleton("eventBus", eventBus);
		if (!beanFactory.containsSingleton("dataManager"))
			beanFactory.registerSingleton("dataManager", getContext().getDataManager());
		for (Entry<String, Object> entry : getContext().getInitialBeans().entrySet()) {
			if (!beanFactory.containsSingleton(entry.getKey()))
				beanFactory.registerSingleton(entry.getKey(), entry.getValue());
		}
		if (beanFactory.getRegisteredScope("view") == null)
			beanFactory.registerScope("view", new ViewScope());
	}
	
	public static SpringContextManager configure(ConfigurableApplicationContext applicationContext, Application application) {
		final SpringContextManager contextManager = new SpringContextManager(applicationContext, application);
		
		applicationContext.addBeanFactoryPostProcessor(new BeanFactoryPostProcessor() {
			@Override
			public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
				if (!beanFactory.containsSingleton(SpringContextManager.class.getName()))
					beanFactory.registerSingleton(SpringContextManager.class.getName(), contextManager);
			}
		});
		return contextManager;
	}
	
	
    private final class SpringDataConflictListener implements DataConflictListener {
		@Override
		public void onConflict(EntityManager entityManager, Conflicts conflicts) {
			TideApplicationEvent event = new TideApplicationEvent(getContext(null), UpdateKind.CONFLICT.eventName(), conflicts);
			applicationContext.publishEvent(event);
		}
	}
}
