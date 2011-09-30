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

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.granite.gravity.Gravity;
import org.granite.logging.Logger;
import org.granite.tide.data.DataContext;
import org.granite.tide.data.DataEnabled;
import org.granite.tide.data.DataUpdatePostprocessor;
import org.granite.tide.data.DataEnabled.PublishMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Spring AOP interceptor to handle publishing of data changes instead of relying on the default behaviour
 * This can be used outside of a HTTP Granite context and inside the security/transaction context
 *  
 * @author William DRAI
 */
public class TideDataPublishingInterceptor implements MethodInterceptor {
	
	private static final Logger log = Logger.getLogger(TideDataPublishingInterceptor.class);
	
	private Gravity gravity;
	private DataUpdatePostprocessor dataUpdatePostprocessor;
	
	public void setGravity(Gravity gravity) {
		this.gravity = gravity;
	}
	
	@Autowired(required=false)
	public void setDataUpdatePostprocessor(DataUpdatePostprocessor dataUpdatePostprocessor) {
		this.dataUpdatePostprocessor = dataUpdatePostprocessor;
	}
	
    public Object invoke(final MethodInvocation invocation) throws Throwable {
    	DataEnabled dataEnabled = invocation.getThis().getClass().getAnnotation(DataEnabled.class);
    	if (dataEnabled == null || !dataEnabled.useInterceptor())
    		return invocation.proceed();
    	
    	boolean shouldRemoveContextAtEnd = DataContext.get() == null;
    	boolean shouldInitContext = shouldRemoveContextAtEnd || DataContext.isNull();
    	boolean onCommit = false;
    	
    	if (shouldInitContext) {
    		DataContext.init(gravity, dataEnabled.topic(), dataEnabled.params(), dataEnabled.publish());
    		if (dataUpdatePostprocessor != null)
    			DataContext.get().setDataUpdatePostprocessor(dataUpdatePostprocessor);
    	}
    	
        DataContext.observe();
        try {
        	if (dataEnabled.publish().equals(PublishMode.ON_COMMIT) && !TransactionSynchronizationManager.isCurrentTransactionReadOnly()) {
        		 if (TransactionSynchronizationManager.isSynchronizationActive()) {
        			 TransactionSynchronizationManager.registerSynchronization(new DataPublishingTransactionSynchronization(shouldRemoveContextAtEnd));
        			 onCommit = true;
        		 }
        		 else
        			 log.warn("Could not register synchronization for ON_COMMIT publish mode, check that the Spring PlatformTransactionManager supports it");
        	}
        	
        	Object ret = invocation.proceed();
        	
        	DataContext.publish(PublishMode.ON_SUCCESS);
        	return ret;
        }
        finally {
        	if (shouldRemoveContextAtEnd && !onCommit)
        		DataContext.remove();
        }
    }
    
    private static class DataPublishingTransactionSynchronization extends TransactionSynchronizationAdapter {
    	
    	private boolean removeContext = false;
    	
    	public DataPublishingTransactionSynchronization(boolean removeContext) {
    		this.removeContext = removeContext;
    	}

		@Override
		public void beforeCommit(boolean readOnly) {
			if (!readOnly)
				DataContext.publish(PublishMode.ON_COMMIT);
		}

		@Override
		public void beforeCompletion() {
			if (removeContext)
				DataContext.remove();
		}

    	
    }
}
