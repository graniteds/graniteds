/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of Granite Data Services.
 *
 *   Granite Data Services is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU Library General Public License as published by
 *   the Free Software Foundation; either version 2 of the License, or (at your
 *   option) any later version.
 *
 *   Granite Data Services is distributed in the hope that it will be useful, but
 *   WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *   FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
 *   for more details.
 *
 *   You should have received a copy of the GNU Library General Public License
 *   along with this library; if not, see <http://www.gnu.org/licenses/>.
 */

package org.granite.tide.cdi;

import java.io.Serializable;

import javax.enterprise.inject.spi.Bean;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import org.granite.logging.Logger;
import org.granite.messaging.amf.io.util.externalizer.DefaultExternalizer;
import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedBean;


@TideBean @ExternalizedBean(type=DefaultExternalizer.class) @Interceptor
public class TideBeanInterceptor implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private static final Logger log = Logger.getLogger(TideBeanInterceptor.class);

	@Inject
	private CDIServiceContext tideContext;
	
	@Inject
	private TideInstrumentedBeans instrumentedBeans;
	
	
	@AroundInvoke
	public Object doAround(InvocationContext invocation) throws Exception {
		Object result = invocation.proceed();
		
        Bean<?> bean = instrumentedBeans.getBean(invocation.getTarget().getClass());
        if (bean == null)
        	log.warn("Instrumented Bean not found for class " + invocation.getTarget().getClass());
        else {
    		String componentName = bean.getName();
    		
    		ScopedContextResult scr = new ScopedContextResult(componentName, null, invocation.getTarget());
    		scr.setComponentClassName(bean.getBeanClass().getName());
    		tideContext.addResultEval(scr);
        }
		
		return result;
	}
}
