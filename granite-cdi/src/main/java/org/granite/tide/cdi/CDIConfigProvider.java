/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
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
package org.granite.tide.cdi;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.servlet.ServletContext;

import org.granite.cdi.CDIUtils;
import org.granite.config.ConfigProvider;
import org.granite.messaging.service.ServiceFactory;


public class CDIConfigProvider implements ConfigProvider {
	
	protected BeanManager beanManager;

	public CDIConfigProvider(ServletContext servletContext) {
		beanManager = CDIUtils.lookupBeanManager(servletContext);
	}

	public Boolean useTide() {
		return true;
	}

	public String getType() {
		return "server";
	}

	public Class<? extends ServiceFactory> getFactoryClass() {
		return CDIServiceFactory.class;
	}

	@SuppressWarnings("unchecked")
	public <T> T findInstance(Class<T> type) {
		Set<Bean<?>> beans = beanManager.getBeans(type);
		if (beans.size() == 1) {
			Bean<?> bean = beans.iterator().next();
			CreationalContext<?> cc = beanManager.createCreationalContext(bean);
			return (T)beanManager.getReference(bean, type, cc);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public <T> Set<T> findInstances(Class<T> type) {
		Set<Bean<?>> beans = beanManager.getBeans(type);
		Set<T> instances = new HashSet<T>(beans.size());
		for (Bean<?> bean : beans) {
			CreationalContext<?> cc = beanManager.createCreationalContext(bean);
			instances.add((T)beanManager.getReference(bean, type, cc));
		}
		return instances;
	}
	
	public Class<?>[] getTideInterfaces() {
		return new Class<?>[] { Identity.class };
	}

	@SuppressWarnings("unchecked")
	public Class<? extends Annotation>[] getTideAnnotations() {
		return new Class[0];
	}

}
