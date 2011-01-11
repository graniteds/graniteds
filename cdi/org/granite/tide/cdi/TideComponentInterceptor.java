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

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import org.granite.tide.invocation.ContextUpdate;


@TideComponent @Interceptor
public class TideComponentInterceptor {
	
	@Inject
	private CDIServiceContext tideContext;
	

	@AroundInvoke
	public Object doAround(InvocationContext invocation) throws Exception {
		
        TideInvocation tideInvocation = TideInvocation.get();
        
        if (tideContext == null || tideInvocation == null || tideInvocation.isLocked())
            return invocation.proceed();
        
        boolean evaluate = false;
        
        if (tideInvocation.isEnabled() && !tideInvocation.isUpdated()) {
            List<ContextUpdate> updates = new ArrayList<ContextUpdate>(tideInvocation.getUpdates());
            tideInvocation.updated();
            tideContext.restoreContext(updates, invocation.getTarget());
            evaluate = true;
        }
		
		Object result = invocation.proceed();

        if (evaluate)
            tideInvocation.evaluated(tideContext.evaluateResults(invocation.getTarget(), false));

		return result;
	}
}
