package org.granite.test.builder.services;

import java.util.Iterator;

import junit.framework.Assert;

import org.granite.generator.as3.JavaAs3GroovyConfiguration;
import org.granite.generator.as3.reflect.JavaMethod;
import org.granite.generator.as3.reflect.JavaRemoteDestination;
import org.granite.generator.javafx.JavaFXGroovyTransformer;
import org.granite.test.builder.MockJavaFXGroovyConfiguration;
import org.junit.Test;

public class TestGenRepositoryGDS1076 {

	@Test
	public void testGenRepository() {
		JavaAs3GroovyConfiguration config = new MockJavaFXGroovyConfiguration();
		JavaFXGroovyTransformer provider = new JavaFXGroovyTransformer(config, null);
		
		JavaRemoteDestination jrd = new JavaRemoteDestination(provider, SecureEntity1Repository.class, null);
		Assert.assertEquals("Methods 2", 3, jrd.getMethods().size());
		JavaMethod jm = null;		
		Iterator<JavaMethod> i = jrd.getMethods().iterator();
		while (i.hasNext()) {
			JavaMethod m = i.next();
			if (m.getName().equals("saveAndFlush")) {
				jm = m;
				break;
			}
		}
		if (jm == null)
			Assert.fail("Method saveAndFlush not found");
		else
			Assert.assertEquals("Method return type", "org.granite.test.builder.entities.Entity1", jm.getClientReturnType().getQualifiedName());
	}

	@Test
	public void testGenRepository2() {
		JavaAs3GroovyConfiguration config = new MockJavaFXGroovyConfiguration();
		JavaFXGroovyTransformer provider = new JavaFXGroovyTransformer(config, null);
		
		JavaRemoteDestination jrd = new JavaRemoteDestination(provider, SecureEntity2Repository.class, null);
		Assert.assertEquals("Methods 2", 3, jrd.getMethods().size());

		JavaMethod jm = null;		
		Iterator<JavaMethod> i = jrd.getMethods().iterator();
		while (i.hasNext()) {
			JavaMethod m = i.next();
			if (m.getName().equals("save")) {
				jm = m;
				break;
			}
		}
		if (jm == null)
			Assert.fail("Method save not found");
		else
			Assert.assertEquals("Method return type", "org.granite.test.builder.entities.Entity1", jm.getClientReturnType().getQualifiedName());
	}
	
}
