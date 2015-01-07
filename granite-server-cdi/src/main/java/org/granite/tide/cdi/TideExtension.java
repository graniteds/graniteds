/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2015 GRANITE DATA SERVICES S.A.S.
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

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.AnnotatedConstructor;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.ProcessBean;
import javax.enterprise.inject.spi.ProcessProducerField;
import javax.enterprise.inject.spi.ProcessProducerMethod;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;
import javax.inject.Named;

import org.granite.logging.Logger;
import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedBean;
import org.granite.messaging.service.annotations.RemoteDestination;
import org.granite.tide.annotations.TideEnabled;


/**
 * @author William DRAI
 */
public class TideExtension implements Extension {
	
	private static final Logger log = Logger.getLogger(TideExtension.class);
	
	
	@Inject
	BeanManager manager;

	private Bean<?> tideInstrumentedBeans = null;
	private Map<Type, Bean<?>> instrumentedBeans = new HashMap<Type, Bean<?>>();
	private Map<Object, Type> producedBeans = new HashMap<Object, Type>();
	
	
	public <X> void processAnnotatedType(@Observes ProcessAnnotatedType<X> event) {
		AnnotatedType<X> annotatedType = event.getAnnotatedType();
		
		boolean tideComponent = false;
		boolean tideBean = false;
		for (Type type : annotatedType.getTypeClosure()) {
			if (type instanceof ParameterizedType)
				type = ((ParameterizedType)type).getRawType();
			if (type instanceof Class<?> 
				&& (((Class<?>)type).isAnnotationPresent(RemoteDestination.class) 
					|| ((Class<?>)type).isAnnotationPresent(TideEnabled.class)
					|| ((Class<?>)type).isAnnotationPresent(Named.class)
					|| ((Class<?>)type).isAnnotationPresent(RequestScoped.class))) {
				tideComponent = true;
				break;
			}
			if (type instanceof Class<?> 
				&& ((Serializable.class.isAssignableFrom((Class<?>)type) && ((Class<?>)type).isAnnotationPresent(Named.class)) 
					|| ((Class<?>)type).isAnnotationPresent(ExternalizedBean.class))) {
				tideBean = true;
				break;
			}
		}
		
		if (tideComponent || tideBean)
			event.setAnnotatedType(new TideAnnotatedType<X>(annotatedType, tideComponent, tideBean));
	}
	
	public <X> void processBean(@Observes ProcessBean<X> event) {
		if (event.getAnnotated().isAnnotationPresent(TideComponent.class) || event.getAnnotated().isAnnotationPresent(TideBean.class)) {
			instrumentedBeans.put(event.getAnnotated().getBaseType(), event.getBean());
			log.info("Instrumented Tide component %s", event.getBean().toString());
		}
		
		Bean<?> bean = event.getBean();
		if (event instanceof ProcessProducerMethod<?, ?>) {
			Type type = ((ProcessProducerMethod<?, ?>)event).getAnnotatedProducerMethod().getDeclaringType().getBaseType();			
			producedBeans.put(((ProcessProducerMethod<?, ?>)event).getAnnotatedProducerMethod().getBaseType(), type);
			if (bean.getName() != null)
				producedBeans.put(bean.getName(), type);
		}
		else if (event instanceof ProcessProducerField<?, ?>) {
			Type type = ((ProcessProducerField<?, ?>)event).getAnnotatedProducerField().getDeclaringType().getBaseType();
			producedBeans.put(((ProcessProducerField<?, ?>)event).getAnnotatedProducerField().getBaseType(), type);
			if (bean.getName() != null)
				producedBeans.put(bean.getName(), type);
		}
		
		if (event.getBean().getBeanClass().equals(TideInstrumentedBeans.class))
			tideInstrumentedBeans = event.getBean();
	}
	
	public void processAfterBeanDiscovery(@Observes AfterBeanDiscovery event, BeanManager manager) {
	}
	
	public void processAfterDeploymentValidation(@Observes AfterDeploymentValidation event, BeanManager manager) {
        if (tideInstrumentedBeans == null)
            return;
        TideInstrumentedBeans ib = (TideInstrumentedBeans)manager.getReference(tideInstrumentedBeans, TideInstrumentedBeans.class,
                manager.createCreationalContext(tideInstrumentedBeans));
        ib.setBeans(instrumentedBeans);
        ib.setProducedBeans(producedBeans);
	}
	

	
	@SuppressWarnings("serial")
	public static class TideAnnotatedType<T> implements AnnotatedType<T> {
		
		private final AnnotatedType<T> annotatedType;
		private final Annotation componentQualifier = new AnnotationLiteral<TideComponent>() {};
		private final Annotation beanQualifier = new AnnotationLiteral<TideBean>() {};
		private final Set<Annotation> annotations;
		
		
		public TideAnnotatedType(AnnotatedType<T> annotatedType, boolean component, boolean bean) {
			this.annotatedType = annotatedType;
			annotations = new HashSet<Annotation>(annotatedType.getAnnotations());
			if (component)
				annotations.add(componentQualifier);
			if (bean)
				annotations.add(beanQualifier);
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
			if (annotationClass.equals(TideBean.class))
				return (X)beanQualifier;
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
			if (annotationClass.equals(TideComponent.class) && annotations.contains(componentQualifier))
				return true;			
			if (annotationClass.equals(TideBean.class) && annotations.contains(beanQualifier))
				return true;			
			return annotatedType.isAnnotationPresent(annotationClass);
		}
	}
}
