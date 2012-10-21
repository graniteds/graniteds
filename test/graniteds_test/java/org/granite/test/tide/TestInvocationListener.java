package org.granite.test.tide;

import org.granite.messaging.service.ServiceInvocationContext;
import org.granite.messaging.service.ServiceInvocationListener;


public class TestInvocationListener implements ServiceInvocationListener {
	
	private boolean failBefore = false;
	
	public void setFailBefore(boolean failBefore) {
		this.failBefore = failBefore;
	}
	
	@Override
	public Object[] beforeMethodSearch(Object invokee, String methodName, Object[] args) {
		return args;
	}

	@Override
	public void beforeInvocation(ServiceInvocationContext context) {
		if (failBefore)
			throw new RuntimeException("ForceFail");
	}

	@Override
	public void afterInvocationError(ServiceInvocationContext context, Throwable t) {
	}

	@Override
	public Object afterInvocation(ServiceInvocationContext context, Object result) {
		return result;
	}

}
