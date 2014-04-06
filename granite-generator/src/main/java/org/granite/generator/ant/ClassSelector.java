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
package org.granite.generator.ant;

import java.io.File;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.Parameter;
import org.apache.tools.ant.types.selectors.BaseExtendSelector;

/**
 * @author Franck WOLFF
 */
public class ClassSelector extends BaseExtendSelector {

	Class<? extends Annotation>[] annotations = null;
	Class<?>[] superclasses = null;
	Class<?>[] classes = null;
	
	@SuppressWarnings("unchecked")
	private void initializeParameters() throws BuildException {
		log("init...");
		if (annotations == null) {
			List<Class<?>> annotationsList = new ArrayList<Class<?>>();
			List<Class<?>> superclassesList = new ArrayList<Class<?>>();
			List<Class<?>> classesList = new ArrayList<Class<?>>();

			for (Parameter parameter : getParameters()) {
				if ("annotatedwith".equalsIgnoreCase(parameter.getName())) {
					try {
						Class<?> annotation = Thread.currentThread().getContextClassLoader().loadClass(parameter.getValue());
						annotationsList.add(annotation);
					} catch (Exception e) {
						throw new BuildException("Could not load annotation: " + parameter.getValue());
					}
				}
				else if ("instanceof".equalsIgnoreCase(parameter.getName())) {
					try {
						Class<?> superclass = Thread.currentThread().getContextClassLoader().loadClass(parameter.getValue());
						superclassesList.add(superclass);
					} catch (Exception e) {
						throw new BuildException("Could not load superclass: " + parameter.getValue());
					}
				}
				else if ("type".equalsIgnoreCase(parameter.getName())) {
					try {
						Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass(parameter.getValue());
						classesList.add(clazz);
					} catch (Exception e) {
						throw new BuildException("Could not load type: " + parameter.getValue());
					}
				}
				else
					throw new BuildException("Illegal param name: " + parameter.getName());
			}

			annotations = (Class<? extends Annotation>[])annotationsList.toArray(new Class<?>[annotationsList.size()]);
			superclasses = superclassesList.toArray(new Class<?>[superclassesList.size()]);
			classes = classesList.toArray(new Class<?>[classesList.size()]);
		}
	}
	
	@Override
	public boolean isSelected(File basedir, String fileName, File file) throws BuildException {
		initializeParameters();
		log(fileName);
		if (!fileName.endsWith(".class"))
			return false;
		
		String className = fileName.substring(0, fileName.length() - 6).replace(File.separatorChar, '.');
		try {
			Class<?> type = Thread.currentThread().getContextClassLoader().loadClass(className);
			for (Class<?> clazz : classes) {
				if (clazz.equals(type))
					return true;
			}
			for (Class<?> superclass : superclasses) {
				if (superclass.isAssignableFrom(type))
					return true;
			}
			for (Class<? extends Annotation> annotation : annotations) {
				if (type.isAnnotationPresent(annotation))
					return true;
			}
		} catch (Exception e) {
			throw new BuildException("Could not load class: " + className, e);
		}
		
		return false;
	}
}
