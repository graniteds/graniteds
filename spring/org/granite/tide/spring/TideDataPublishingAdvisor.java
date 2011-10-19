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

package org.granite.tide.spring;

import java.io.Serializable;
import java.lang.reflect.Method;

import org.aopalliance.aop.Advice;
import org.granite.tide.data.DataEnabled;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractPointcutAdvisor;
import org.springframework.aop.support.StaticMethodMatcherPointcut;
import org.springframework.aop.support.annotation.AnnotationClassFilter;


public class TideDataPublishingAdvisor extends AbstractPointcutAdvisor {
	
	private static final long serialVersionUID = 1L;
	

	private TideDataPublishingInterceptor dataPublishingInterceptor;

	private final TideDataPublishingPointcut pointcut = new TideDataPublishingPointcut();


	public TideDataPublishingAdvisor() {
		pointcut.setClassFilter(new AnnotationClassFilter(DataEnabled.class));
	}

	public TideDataPublishingAdvisor(TideDataPublishingInterceptor interceptor) {
		setDataPublishingInterceptor(interceptor);
	}

	public void setDataPublishingInterceptor(TideDataPublishingInterceptor interceptor) {
		this.dataPublishingInterceptor = interceptor;
	}


	public Advice getAdvice() {
		return this.dataPublishingInterceptor;
	}

	public Pointcut getPointcut() {
		return this.pointcut;
	}


	private class TideDataPublishingPointcut extends StaticMethodMatcherPointcut implements Serializable {

		private static final long serialVersionUID = 1L;

		public boolean matches(Method method, Class<?> targetClass) {
			return targetClass.isAnnotationPresent(DataEnabled.class) && targetClass.getAnnotation(DataEnabled.class).useInterceptor();
		}
	}

}