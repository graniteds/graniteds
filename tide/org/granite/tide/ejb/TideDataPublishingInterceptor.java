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

package org.granite.tide.ejb;

import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import javax.naming.InitialContext;
import javax.transaction.Synchronization;
import javax.transaction.TransactionSynchronizationRegistry;

import org.granite.tide.data.DataContext;
import org.granite.tide.data.DataEnabled;
import org.granite.tide.data.DataEnabled.PublishMode;
import org.granite.tide.data.JMSDataDispatcher;


/**
 * EJB interceptor to handle publishing of data changes instead of relying on the default behaviour
 * This can be used outside of a HTTP Granite context and inside the security/transaction context
 * @author William DRAI
 *
 */
@Interceptor
public class TideDataPublishingInterceptor {
	
    @AroundInvoke
    public Object processPublishData(InvocationContext invocationContext) throws Exception {
    	if (invocationContext.getMethod() == null) {
    		// Lifecycle method
    		return invocationContext.proceed();
    	}
    	
    	DataEnabled dataEnabled = invocationContext.getTarget().getClass().getAnnotation(DataEnabled.class);
    	if (dataEnabled == null || !dataEnabled.useInterceptor())
    		return invocationContext.proceed();
    	
    	boolean shouldRemoveContextAtEnd = DataContext.get() == null;
    	boolean shouldInitContext = shouldRemoveContextAtEnd || DataContext.isNull();
    	boolean onCommit = false;
    	
    	if (shouldInitContext)
    		DataContext.init(new JMSDataDispatcher(dataEnabled.topic(), onCommit, dataEnabled.params()), dataEnabled.publish());    	
    	
        DataContext.observe();
        try {
        	if (dataEnabled.publish().equals(PublishMode.ON_COMMIT)) {
        		InitialContext ic = new InitialContext();
        		TransactionSynchronizationRegistry tsr = (TransactionSynchronizationRegistry)ic.lookup("java:comp/TransactionSynchronizationRegistry");
        		tsr.registerInterposedSynchronization(new DataPublishingSynchronization(shouldRemoveContextAtEnd));
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
    
    private static class DataPublishingSynchronization implements Synchronization {
    	
    	private final boolean removeContext;
    	
    	public DataPublishingSynchronization(boolean removeContext) {
    		this.removeContext = removeContext;
    	}

		public void beforeCompletion() {
			DataContext.publish(PublishMode.ON_COMMIT);
			if (removeContext)
				DataContext.remove();
		}

		public void afterCompletion(int status) {
		}
    	
    }
}
