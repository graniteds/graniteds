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

import javax.inject.Inject;
import javax.inject.Named;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import org.granite.messaging.amf.io.util.externalizer.DefaultExternalizer;
import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedBean;


@TideBean @ExternalizedBean(type=DefaultExternalizer.class) @Interceptor
public class TideBeanInterceptor {

	@Inject
	CDIServiceContext tideContext;
	
	@AroundInvoke
	public Object doAround(InvocationContext context) throws Exception {
		Object result = context.proceed();
		
		String componentName = null;
		if (context.getTarget().getClass().isAnnotationPresent(Named.class))
			componentName = context.getTarget().getClass().getAnnotation(Named.class).value();
		
        tideContext.addResultEval(new ScopedContextResult(componentName, null, context.getTarget()));
		
		return result;
	}
}
