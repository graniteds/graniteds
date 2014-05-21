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
package org.granite.client.scan;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

import org.granite.client.configuration.ClassScanner;
import org.granite.client.messaging.RemoteAlias;

import net.sf.extcos.ComponentQuery;
import net.sf.extcos.ComponentScanner;
import net.sf.extcos.spi.ResourceAccessor;
import net.sf.extcos.spi.ResourceType;

/**
 * @author William DRAI
 */
public class ExtCosClassScanner implements ClassScanner {
	
	public Set<Class<?>> scan(final Set<String> packageNames, Class<? extends Annotation> annotationClass) {
		ComponentScanner scanner = new ComponentScanner();
		
		final String[] basePackageNames = packageNames.toArray(new String[packageNames.size()]);
		
		Set<Class<?>> classes = scanner.getClasses(new ComponentQuery() {
			@Override
			protected void query() {
				select(JavaClassResourceType.javaClasses()).from(basePackageNames).returning(allAnnotatedWith(RemoteAlias.class));
			}
		});
		
		Set<Class<?>> annotatedClasses = new HashSet<Class<?>>();
		for (Class<?> cls : classes) {
		    if (cls.isAnnotationPresent(annotationClass))
		        annotatedClasses.add(cls);
		}
		
		return annotatedClasses;
	}	
	
	public static class JavaClassResourceType implements ResourceType {
		
		private static final String JAVA_CLASS_SUFFIX = "class";
		private static JavaClassResourceType instance;

		/**
		 * Always instantiate via the {@link #javaClasses()} method.
		 */
		private JavaClassResourceType() {
		}

		@Override
		public String getFileSuffix() {
			return JAVA_CLASS_SUFFIX;
		}

		/**
		 * EDSL method
		 */
		public static JavaClassResourceType javaClasses() {
			if (instance == null)
				instance = new JavaClassResourceType();

			return instance;
		}

		@Override
		public ResourceAccessor getResourceAccessor() {
			return new JavaResourceAccessor();
		}
	}
}
