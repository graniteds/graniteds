package org.granite.test.util;

import org.granite.util.ClassUtil;
import org.junit.Assert;
import org.junit.Test;

public class TestUtil {

	@Test
	public void testMultidimArray() {
		Object[][] array = new Object[10][10];
		boolean npe = false;
		try {
			ClassUtil.findResource(array.getClass());
		}
		catch (NullPointerException e) {
			npe = true;
		}
		Assert.assertFalse(npe);
	}
}
