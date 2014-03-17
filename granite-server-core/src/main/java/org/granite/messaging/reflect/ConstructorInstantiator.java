package org.granite.messaging.reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class ConstructorInstantiator implements Instantiator {

	private final Constructor<?> constructor;
	
	public ConstructorInstantiator(Constructor<?> constructor) {
		this.constructor = constructor;
	}

	@Override
	public Object newInstance() throws InstantiationException, IllegalAccessException, InvocationTargetException {
		return constructor.newInstance();
	}
}
