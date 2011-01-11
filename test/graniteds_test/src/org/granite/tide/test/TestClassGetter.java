package org.granite.tide.test;

import java.lang.reflect.Field;

import org.granite.messaging.amf.io.util.DefaultClassGetter;
import org.granite.util.Reflections;


public class TestClassGetter extends DefaultClassGetter {
	
	@Override
	public void initialize(Object owner, String propertyName, Object propertyValue) {
		super.initialize(owner, propertyName, propertyValue);
		
		try {
			if (propertyValue instanceof String && ((String)propertyValue).startsWith("$$Proxy$$")) {
				Field field = owner.getClass().getDeclaredField(propertyName);
				field.setAccessible(true);
				Reflections.set(field, owner, ((String)propertyValue).substring("$$Proxy$$".length()));
			}
		}
		catch (Exception e) {
		}
	}

}
