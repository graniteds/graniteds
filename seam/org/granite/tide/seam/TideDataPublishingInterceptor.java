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

package org.granite.tide.seam;

import org.granite.gravity.Gravity;
import org.granite.tide.data.DataContext;
import org.granite.tide.data.DataEnabled;
import org.granite.tide.data.DataEnabled.PublishMode;
import org.jboss.seam.Component;
import org.jboss.seam.annotations.intercept.AroundInvoke;
import org.jboss.seam.annotations.intercept.Interceptor;
import org.jboss.seam.core.Events;
import org.jboss.seam.intercept.AbstractInterceptor;
import org.jboss.seam.intercept.InvocationContext;
import org.jboss.seam.transaction.TransactionInterceptor;


/**
 * Seam interceptor to handle publishing of data changes instead of relying on the default behaviour
 * This can be used outside of a HTTP Granite context and inside the security/transaction context
 * @author William DRAI
 *
 */
@Interceptor(stateless=true, within={TransactionInterceptor.class})
public class TideDataPublishingInterceptor extends AbstractInterceptor {
	
	private static final long serialVersionUID = 1L;

    @AroundInvoke
    public Object aroundInvoke(InvocationContext invocationContext) throws Exception {
    	DataEnabled dataEnabled = getComponent().getBeanClass().getAnnotation(DataEnabled.class);
    	
    	if (dataEnabled == null || !dataEnabled.useInterceptor())
			return invocationContext.proceed();
    	
		if (SeamUtils.isLifecycleMethod(getComponent(), invocationContext.getMethod()))
			return invocationContext.proceed();
    	
    	boolean shouldRemoveContextAtEnd = DataContext.get() == null;
    	boolean shouldInitContext = shouldRemoveContextAtEnd || DataContext.isNull();
    	boolean onCommit = false;
    	
    	if (shouldInitContext) {
    		Gravity gravity = (Gravity)Component.getInstance("org.granite.seam.gravity");
    		DataContext.init(gravity, dataEnabled.topic(), dataEnabled.params(), dataEnabled.publish());
    	}
    	
        DataContext.observe();
        try {
        	if (dataEnabled.publish().equals(PublishMode.ON_COMMIT)) {
        		Events.instance().raiseTransactionSuccessEvent("org.granite.tide.seam.data.transactionSuccess", shouldRemoveContextAtEnd);
        		Events.instance().raiseTransactionCompletionEvent("org.granite.tide.seam.data.transactionCompletion", shouldRemoveContextAtEnd);
        		onCommit = true;
        	}
        	
        	Object ret = invocationContext.proceed();
        	
        	DataContext.publish(PublishMode.ON_SUCCESS);
        	return ret;
        }
        finally {
        	if (shouldRemoveContextAtEnd && !onCommit)
        		DataContext.remove();
        }
    }
    
	// Needed for Seam 2.1
    public boolean isInterceptorEnabled() {
        return getComponent().beanClassHasAnnotation(DataEnabled.class) && getComponent().getBeanClass().getAnnotation(DataEnabled.class).useInterceptor();
    }
}
