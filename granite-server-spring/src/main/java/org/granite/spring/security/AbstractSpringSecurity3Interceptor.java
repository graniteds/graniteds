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
