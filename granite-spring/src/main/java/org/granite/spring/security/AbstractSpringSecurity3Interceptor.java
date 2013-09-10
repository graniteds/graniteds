package org.granite.spring.security;

import org.granite.messaging.service.security.AbstractSecurityContext;
import org.springframework.security.access.SecurityMetadataSource;
import org.springframework.security.access.intercept.AbstractSecurityInterceptor;
import org.springframework.security.access.intercept.InterceptorStatusToken;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;


public abstract class AbstractSpringSecurity3Interceptor extends AbstractSecurityInterceptor {
	
	private FilterInvocationSecurityMetadataSource securityMetadataSource;

	@Override
	public Class<? extends Object> getSecureObjectClass() {
		return FilterInvocation.class;
	}

	@Override
	public SecurityMetadataSource obtainSecurityMetadataSource() {
		return securityMetadataSource;
	}
	
	public void setSecurityMetadataSource(FilterInvocationSecurityMetadataSource newSource) {
		this.securityMetadataSource = newSource;
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
