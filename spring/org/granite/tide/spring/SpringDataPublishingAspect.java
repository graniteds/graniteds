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

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.granite.tide.data.DataContext;
import org.granite.tide.data.DataEnabled;
import org.granite.tide.data.DataEnabled.PublishMode;

/**
 * Spring AOP aspect to handle publishing of data changes instead of relying on the default behaviour
 * This can be used outside of a HTTP Granite context and inside the security context when used with Spring Security 
 * @author William DRAI
 *
 */
@Aspect
public class SpringDataPublishingAspect {
	
    @Around("@within(org.granite.tide.data.DataEnabled) && @within(dataEnabled)")
    public Object processPublishData(ProceedingJoinPoint pjp, DataEnabled dataEnabled) throws Throwable {
    	boolean initContext = DataContext.get() == null;
    	
    	if (initContext)
    		DataContext.init(dataEnabled.topic(), dataEnabled.params(), dataEnabled.publish());
        DataContext.observe();
        try {
        	Object ret = pjp.proceed();
        	DataContext.publish(PublishMode.ON_SUCCESS);
        	return ret;
        }
        finally {
        	if (initContext)
        		DataContext.remove();
        }
    }
}
