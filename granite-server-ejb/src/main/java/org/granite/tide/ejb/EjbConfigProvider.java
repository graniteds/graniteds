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
package org.granite.tide.ejb;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Set;

import javax.servlet.ServletContext;

import org.granite.config.ConfigProvider;
import org.granite.messaging.service.ServiceFactory;


public class EjbConfigProvider implements ConfigProvider {
    
    public EjbConfigProvider(ServletContext servletContext) {        
    }
	
	public Boolean useTide() {
		return true;
	}

	public String getType() {
		return "server";
	}

	public Class<? extends ServiceFactory> getFactoryClass() {
		return EjbServiceFactory.class;
	}

	public <T> T findInstance(Class<T> type) {
		return null;
	}

	public <T> Set<T> findInstances(Class<T> type) {
		return Collections.emptySet();
	}
	
	public Class<?>[] getTideInterfaces() {
		return new Class<?>[] { EjbIdentity.class };
	}

	@SuppressWarnings("unchecked")
	public Class<? extends Annotation>[] getTideAnnotations() {
		return new Class[0];
	}

}
