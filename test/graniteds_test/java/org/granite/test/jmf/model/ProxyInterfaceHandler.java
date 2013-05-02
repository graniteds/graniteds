package org.granite.test.jmf.model;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class ProxyInterfaceHandler implements InvocationHandler, Serializable {

	private static final long serialVersionUID = 1L;

	public ProxyInterfaceHandler() {
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		return null;
	}
}
