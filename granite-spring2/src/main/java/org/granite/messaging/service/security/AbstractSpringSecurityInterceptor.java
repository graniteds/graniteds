package org.granite.messaging.service.security;

import org.springframework.security.intercept.AbstractSecurityInterceptor;
import org.springframework.security.intercept.InterceptorStatusToken;
import org.springframework.security.intercept.ObjectDefinitionSource;
import org.springframework.security.intercept.web.FilterInvocation;
import org.springframework.security.intercept.web.FilterInvocationDefinitionSource;


public abstract class AbstractSpringSecurityInterceptor extends AbstractSecurityInterceptor {
	
	private FilterInvocationDefinitionSource objectDefinitionSource;

	@Override
	public Class<? extends Object> getSecureObjectClass() {
		return FilterInvocation.class;
	}

	@Override
	public ObjectDefinitionSource obtainObjectDefinitionSource() {
		return objectDefinitionSource;
	}
	
	public void setObjectDefinitionSource(FilterInvocationDefinitionSource newSource) {
		this.objectDefinitionSource = newSource;
	}
	
	public Object invoke(AbstractSecurityContext securityContext) throws Exception {
		FilterInvocation fi = buildFilterInvocation(securityContext);
		InterceptorStatusToken token = beforeInvocation(fi);
		Object returnedObject = null;
		try {
			returnedObject = securityContext.invoke();
		}
		finally {
			returnedObject = afterInvocation(token, returnedObject);
		}
		return returnedObject;
	}
	
	protected abstract FilterInvocation buildFilterInvocation(AbstractSecurityContext securityContext);
		
}
