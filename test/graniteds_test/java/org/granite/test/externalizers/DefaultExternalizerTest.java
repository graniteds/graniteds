package org.granite.test.externalizers;

import java.util.List;

import org.granite.messaging.amf.io.util.Property;
import org.granite.messaging.amf.io.util.externalizer.DefaultExternalizer;
import org.junit.Assert;
import org.junit.Test;


public class DefaultExternalizerTest extends AbstractExternalizerTest {

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
