package org.granite.test.jmf;

import java.util.List;

import org.granite.messaging.annotations.Serialized;
import org.granite.messaging.jmf.reflect.Property;
import org.granite.messaging.jmf.reflect.Reflection;
import org.granite.messaging.jmf.reflect.ReflectionException;
import org.granite.test.jmf.model.EmptyPropertiesOrderBean;
import org.granite.test.jmf.model.IllegalEmptyPropertiesOrderBean;
import org.granite.test.jmf.model.IllegalNamePropertiesOrderBean;
import org.granite.test.jmf.model.IllegalNonEmptyPropertiesOrderBean;
import org.granite.test.jmf.model.PropertiesOrderBean;
import org.junit.Assert;
import org.junit.Test;

public class TestJMFPropertiesOrder {

	@Test
	public void testEmptyOrder() {
		Reflection reflection = new Reflection(null);
		List<Property> properties = reflection.findSerializableProperties(EmptyPropertiesOrderBean.class);
		Assert.assertTrue(properties.size() == 0);
	}

	@Test
	public void testPropertiesOrder() {
		String[] names = PropertiesOrderBean.class.getAnnotation(Serialized.class).propertiesOrder();
		
		Reflection reflection = new Reflection(null);
		List<Property> properties = reflection.findSerializableProperties(PropertiesOrderBean.class);
		
		Assert.assertTrue(properties.size() == names.length);
		for (int i = 0; i < names.length; i++)
			Assert.assertEquals(names[i], properties.get(i).getName());
	}

	@Test
	public void testIllegalEmptyOrder() {
		Reflection reflection = new Reflection(null);
		try {
			reflection.findSerializableProperties(IllegalEmptyPropertiesOrderBean.class);
			Assert.fail("Should fail");
		}
		catch (ReflectionException e) {
			//e.printStackTrace();
		}
	}

	@Test
	public void testIllegalNonEmptyOrder() {
		Reflection reflection = new Reflection(null);
		try {
			reflection.findSerializableProperties(IllegalNonEmptyPropertiesOrderBean.class);
			Assert.fail("Should fail");
		}
		catch (ReflectionException e) {
			//e.printStackTrace();
		}
	}

	@Test
	public void testIllegalNameOrder() {
		Reflection reflection = new Reflection(null);
		try {
			reflection.findSerializableProperties(IllegalNamePropertiesOrderBean.class);
			Assert.fail("Should fail");
		}
		catch (ReflectionException e) {
			//e.printStackTrace();
		}
	}
}
