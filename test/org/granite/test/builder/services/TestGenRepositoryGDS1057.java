package org.granite.test.builder.services;

import junit.framework.Assert;

import org.granite.generator.as3.reflect.JavaMethod;
import org.granite.generator.as3.reflect.JavaRemoteDestination;
import org.granite.generator.javafx.JavaFXGroovyTransformer;
import org.granite.test.builder.MockJavaFXGroovyConfiguration;
import org.granite.test.builder.Util;
import org.granite.test.builder.entities.Entity1;
import org.granite.test.builder.entities.Entity2;
import org.junit.Test;

public class TestGenRepositoryGDS1057 {

	@Test
	public void testGenRepository() {
		MockJavaFXGroovyConfiguration config = new MockJavaFXGroovyConfiguration();
		config.addFileSetClasses(Entity1.class, Entity2.class);
		JavaFXGroovyTransformer provider = new JavaFXGroovyTransformer(config, null);
		
		JavaRemoteDestination jrd1 = new JavaRemoteDestination(provider, Repository1.class, null);
		JavaMethod jm = Util.findMethod(jrd1, "findAll", Iterable.class);
		Assert.assertEquals("Method return type", "java.lang.Iterable<Entity1>", jm.getClientReturnType().getQualifiedName());
		jm = Util.findMethod(jrd1, "delete", Iterable.class);
		Assert.assertEquals("Method param type", "java.lang.Iterable<? extends Entity1>", jm.getClientParameterTypes()[0].getQualifiedName());
		jm = Util.findMethod(jrd1, "deleteAllInBatch", Iterable.class);
		Assert.assertEquals("Method param type", "java.lang.Iterable<Entity1>", jm.getClientParameterTypes()[0].getQualifiedName());
		
		JavaRemoteDestination jrd2 = new JavaRemoteDestination(provider, Repository2.class, null);
		jm = Util.findMethod(jrd2, "findAll", Iterable.class);
		Assert.assertEquals("Method 2 return type", "java.lang.Iterable<Entity2>", jm.getClientReturnType().getQualifiedName());
		jm = Util.findMethod(jrd2, "delete", Iterable.class);
		Assert.assertEquals("Method param type", "java.lang.Iterable<? extends Entity2>", jm.getClientParameterTypes()[0].getQualifiedName());
		jm = Util.findMethod(jrd2, "deleteAllInBatch", Iterable.class);
		Assert.assertEquals("Method param type", "java.lang.Iterable<Entity2>", jm.getClientParameterTypes()[0].getQualifiedName());
	}

	@Test
	public void testGenRepositoryB() {
		MockJavaFXGroovyConfiguration config = new MockJavaFXGroovyConfiguration();
		JavaFXGroovyTransformer provider = new JavaFXGroovyTransformer(config, null);
		
		JavaRemoteDestination jrd1 = new JavaRemoteDestination(provider, Repository1.class, null);
		JavaMethod jm = Util.findMethod(jrd1, "findAll", Iterable.class);
		Assert.assertEquals("Method return type", "java.lang.Iterable<Entity1>", jm.getClientReturnType().getQualifiedName());
		jm = Util.findMethod(jrd1, "delete", Iterable.class);
		Assert.assertEquals("Method param type", "java.lang.Iterable<? extends Entity1>", jm.getClientParameterTypes()[0].getQualifiedName());
		jm = Util.findMethod(jrd1, "deleteAllInBatch", Iterable.class);
		Assert.assertEquals("Method param type", "java.lang.Iterable<Entity1>", jm.getClientParameterTypes()[0].getQualifiedName());
		
		JavaRemoteDestination jrd2 = new JavaRemoteDestination(provider, Repository2.class, null);
		jm = Util.findMethod(jrd2, "findAll", Iterable.class);
		Assert.assertEquals("Method 2 return type", "java.lang.Iterable<Entity2>", jm.getClientReturnType().getQualifiedName());
		jm = Util.findMethod(jrd2, "delete", Iterable.class);
		Assert.assertEquals("Method param type", "java.lang.Iterable<? extends Entity2>", jm.getClientParameterTypes()[0].getQualifiedName());
		jm = Util.findMethod(jrd2, "deleteAllInBatch", Iterable.class);
		Assert.assertEquals("Method param type", "java.lang.Iterable<Entity2>", jm.getClientParameterTypes()[0].getQualifiedName());
	}

	@Test
	public void testGenRepository2() {
		MockJavaFXGroovyConfiguration config = new MockJavaFXGroovyConfiguration();
		config.addFileSetClasses(Entity1.class, Entity2.class);
		JavaFXGroovyTransformer provider = new JavaFXGroovyTransformer(config, null);
		
		JavaRemoteDestination jrd1 = new JavaRemoteDestination(provider, SecureEntity2Repository.class, null);
		JavaMethod jm1 = Util.findMethod(jrd1, "findAll");
		Assert.assertEquals("Method return type", "List<Entity1>", jm1.getClientReturnType().getName());
		
		JavaRemoteDestination jrd2 = new JavaRemoteDestination(provider, SecureEntity3Repository.class, null);
		JavaMethod jm2 = Util.findMethod(jrd2, "findAll");
		Assert.assertEquals("Method return type", "List<Entity2>", jm2.getClientReturnType().getName());
	}
	
}
