/*
  GRANITE DATA SERVICES
  Copyright (C) 2011 GRANITE DATA SERVICES S.A.S.

  This file is part of Granite Data Services.

  Granite Data Services is free software; you can redistribute it and/or modify
  it under the terms of the GNU Library General Public License as published by
  the Free Software Foundation; either version 2 of the License, or (at your
  option) any later version.

  Granite Data Services is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
  for more details.

  You should have received a copy of the GNU Library General Public License
  along with this library; if not, see <http://www.gnu.org/licenses/>.
 */

package org.granite.tide.cdi;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AnnotatedConstructor;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;

import org.granite.messaging.service.annotations.RemoteDestination;
import org.granite.tide.annotations.TideEnabled;


/**
 * @author William DRAI
 */
public class TideExtension implements Extension {
	
	@Inject
	BeanManager manager;

	
	public <X> void processAnnotatedType(@Observes ProcessAnnotatedType<X> event) {
		AnnotatedType<X> annotatedType = event.getAnnotatedType();
		
		boolean tideComponent = false;
		for (Type type : annotatedType.getTypeClosure()) {
			if (type instanceof Class<?> 
				&& (((Class<?>)type).isAnnotationPresent(RemoteDestination.class) || ((Class<?>)type).isAnnotationPresent(TideEnabled.class))) {
				tideComponent = true;
				break;
			}
		}
		
		if (tideComponent)
			event.setAnnotatedType(new TideComponentAnnotatedType<X>(annotatedType));
	}
	
	
	@SuppressWarnings("serial")
	public static class TideComponentAnnotatedType<T> implements AnnotatedType<T> {
		
		private final AnnotatedType<T> annotatedType;
		private final Annotation componentQualifier = new AnnotationLiteral<TideComponent>() {};
		private final Set<Annotation> annotations;
		
		
		public TideComponentAnnotatedType(AnnotatedType<T> annotatedType) {
			this.annotatedType = annotatedType;
			annotations = new HashSet<Annotation>(annotatedType.getAnnotations());
			annotations.add(componentQualifier);
		}

		public Set<AnnotatedConstructor<T>> getConstructors() {
			return annotatedType.getConstructors();
		}

		public Set<AnnotatedField<? super T>> getFields() {
			return annotatedType.getFields();
		}

		public Class<T> getJavaClass() {
			return annotatedType.getJavaClass();
		}

		public Set<AnnotatedMethod<? super T>> getMethods() {
			return annotatedType.getMethods();
		}

		@SuppressWarnings("unchecked")
		public <X extends Annotation> X getAnnotation(Class<X> annotationClass) {
			if (annotationClass.equals(TideComponent.class))
				return (X)componentQualifier;			
			return annotatedType.getAnnotation(annotationClass);
		}

		public Set<Annotation> getAnnotations() {
			return annotations;
		}

		public Type getBaseType() {
			return annotatedType.getBaseType();
		}

		public Set<Type> getTypeClosure() {
			return annotatedType.getTypeClosure();
		}

		public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
			if (annotationClass.equals(TideComponent.class))
				return true;			
			return annotatedType.isAnnotationPresent(annotationClass);
		}
	}
}
