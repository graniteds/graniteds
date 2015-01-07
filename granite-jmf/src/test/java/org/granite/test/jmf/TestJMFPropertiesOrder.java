/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2015 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of the Granite Data Services Platform.
 *
 *   Granite Data Services is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Lesser General Public
 *   License as published by the Free Software Foundation; either
 *   version 2.1 of the License, or (at your option) any later version.
 *
 *   Granite Data Services is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 *   General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public
 *   License along with this library; if not, write to the Free Software
 *   Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301,
 *   USA, or see <http://www.gnu.org/licenses/>.
 */
package org.granite.test.jmf;

import java.util.List;

import org.granite.messaging.annotations.Serialized;
import org.granite.messaging.reflect.Property;
import org.granite.messaging.reflect.Reflection;
import org.granite.messaging.reflect.ReflectionException;
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
