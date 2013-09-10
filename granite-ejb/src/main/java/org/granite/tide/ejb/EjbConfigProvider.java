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
