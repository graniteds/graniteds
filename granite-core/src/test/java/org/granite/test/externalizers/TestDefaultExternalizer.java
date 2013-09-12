/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of Granite Data Services.
 *
 *   Granite Data Services is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU Library General Public License as published by
 *   the Free Software Foundation; either version 2 of the License, or (at your
 *   option) any later version.
 *
 *   Granite Data Services is distributed in the hope that it will be useful, but
 *   WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *   FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
 *   for more details.
 *
 *   You should have received a copy of the GNU Library General Public License
 *   along with this library; if not, see <http://www.gnu.org/licenses/>.
 */
package org.granite.test.externalizers;

import java.util.List;

import org.granite.messaging.amf.io.util.Property;
import org.granite.messaging.amf.io.util.externalizer.DefaultExternalizer;
import org.junit.Assert;
import org.junit.Test;


public class TestDefaultExternalizer extends AbstractTestExternalizer {

	@Test
	public void testFindOrderedFieldsInClassHierarchy() throws Exception {
		DefaultExternalizer ext = new DefaultExternalizer();

		List<Property> orderedFields = ext.findOrderedFields(AbstractBean.class, false);
		int index = 0;
		Assert.assertEquals("prop1", orderedFields.get(index++).getName());
		Assert.assertEquals("prop2", orderedFields.get(index++).getName());
		Assert.assertEquals("prop3", orderedFields.get(index++).getName());

		orderedFields = ext.findOrderedFields(ConcreteBean.class, false);
		index = 0;
		Assert.assertEquals("prop1", orderedFields.get(index++).getName());
		Assert.assertEquals("prop2", orderedFields.get(index++).getName());
		Assert.assertEquals("prop3", orderedFields.get(index++).getName());
		Assert.assertEquals("prop4", orderedFields.get(index++).getName());
	}
}
