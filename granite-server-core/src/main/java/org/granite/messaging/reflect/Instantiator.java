package org.granite.messaging.reflect;

import java.lang.reflect.InvocationTargetException;

public interface Instantiator {

	Object newInstance() throws InstantiationException, IllegalAccessException, InvocationTargetException;
}
