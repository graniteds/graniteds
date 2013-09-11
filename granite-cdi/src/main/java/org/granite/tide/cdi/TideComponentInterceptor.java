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
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import javax.enterprise.inject.spi.Bean;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import org.granite.logging.Logger;
import org.granite.tide.invocation.ContextResult;
import org.granite.tide.invocation.ContextUpdate;
import org.granite.util.Reflections;


@TideComponent @Interceptor
public class TideComponentInterceptor implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private static final Logger log = Logger.getLogger(TideComponentInterceptor.class);
	
    private boolean reentrant;  // CDI components are single threaded
	
	@Inject
	private CDIServiceContext tideContext;
	
	@Inject
	private TideInstrumentedBeans instrumentedBeans;
	

	@SuppressWarnings("unchecked")
	@AroundInvoke
	public Object doAround(InvocationContext invocation) throws Exception {
		
        if (reentrant)
        	return invocation.proceed();
        
        TideInvocation tideInvocation = TideInvocation.get();
        
        if (tideContext == null || tideInvocation == null || tideInvocation.isLocked())
            return invocation.proceed();
        
        try {
        	reentrant = true;
        	
	        boolean evaluate = false;
	        
	        if (tideInvocation.isEnabled() && !tideInvocation.isUpdated()) {
	            List<ContextUpdate> updates = new ArrayList<ContextUpdate>(tideInvocation.getUpdates());
	            tideInvocation.updated();
	            tideContext.restoreContext(updates, invocation.getTarget());
	            evaluate = true;
	        }
	        
			List<Object[]> crs = new ArrayList<Object[]>();		
	        Bean<?> bean = instrumentedBeans.getBean(invocation.getTarget().getClass());
	        if (bean == null)
	        	log.warn("Instrumented Bean not found for class " + invocation.getTarget().getClass());
	        else {
				String componentName = bean.getName();
				
				for (Entry<ContextResult, Boolean> me : tideContext.getResultsEval().entrySet()) {
					ContextResult cr = me.getKey();
					if (cr.getExpression() == null 
							|| !((cr.getComponentName() != null && cr.getComponentName().equals(componentName))
							|| (cr.getComponentClassName() != null && bean.getTypes().contains(cr.getComponentClass()))))
						continue;
					int idx = cr.getExpression().indexOf('.');
					String propName = idx >= 0 ? cr.getExpression().substring(0, idx) : cr.getExpression();
					Method getter = null;
					try {
						getter = Reflections.getGetterMethod(invocation.getTarget().getClass(), propName);
					}
					catch (Exception e) {
					}
					if (getter != null)
						crs.add(new Object[] { me, getter, getter.invoke(invocation.getTarget()) });
				}
	        }
			
			Object result = invocation.proceed();
			
			for (Object[] cr : crs) {
				Method getter = (Method)cr[1];
				Object newValue = getter.invoke(invocation.getTarget());
				if ((newValue == null && cr[2] != null)
					|| (newValue != null && !newValue.equals(cr[2])))
						((Entry<ContextResult, Boolean>)cr[0]).setValue(true);
			}
			
			for (Entry<ContextResult, Boolean> me : tideContext.getResultsEval().entrySet()) {
				ContextResult cr = me.getKey();
				if (cr.getExpression() != null)
					continue;
				
				if (cr.getComponentClassName() != null && instrumentedBeans.isProducedBy(cr.getComponentClass(), invocation.getTarget().getClass()))
					me.setValue(true);
				
				if (cr.getComponentName() != null && instrumentedBeans.isProducedBy(cr.getComponentName(), invocation.getTarget().getClass()))
					me.setValue(true);
			}
			
	        if (evaluate)
	            tideInvocation.evaluated(tideContext.evaluateResults(invocation.getTarget(), false));
	
			return result;
        }
        finally {
        	reentrant = false;
        }
	}
}
