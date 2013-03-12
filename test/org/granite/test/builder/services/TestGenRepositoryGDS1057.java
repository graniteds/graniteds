package org.granite.test.builder.services;

import junit.framework.Assert;

import org.granite.generator.as3.JavaAs3GroovyConfiguration;
import org.granite.generator.as3.reflect.JavaMethod;
import org.granite.generator.as3.reflect.JavaRemoteDestination;
import org.granite.generator.javafx.JavaFXGroovyTransformer;
import org.granite.test.builder.MockJavaFXGroovyConfiguration;
import org.junit.Test;

public class TestGenRepositoryGDS1057 {

	@Test
	public void testGenRepository() {
		JavaAs3GroovyConfiguration config = new MockJavaFXGroovyConfiguration();
		JavaFXGroovyTransformer provider = new JavaFXGroovyTransformer(config, null);
		
		JavaRemoteDestination jrd1 = new JavaRemoteDestination(provider, Repository1.class, null);
		Assert.assertEquals("Methods 1", 2, jrd1.getMethods().size());
		JavaMethod jm1 = jrd1.getMethods().iterator().next();
		Assert.assertEquals("Method 1 return type", "java.lang.Iterable<Entity1>", jm1.getClientReturnType().getQualifiedName());
		
		JavaRemoteDestination jrd2 = new JavaRemoteDestination(provider, Repository2.class, null);
		Assert.assertEquals("Methods 2", 2, jrd2.getMethods().size());
		JavaMethod jm2 = jrd2.getMethods().iterator().next();
		Assert.assertEquals("Method 2 return type", "java.lang.Iterable<Entity2>", jm2.getClientReturnType().getQualifiedName());
	}
	
}
