package org.granite.test.builder.services;

import junit.framework.Assert;

import org.granite.generator.as3.JavaAs3GroovyConfiguration;
import org.granite.generator.as3.reflect.JavaImport;
import org.granite.generator.as3.reflect.JavaMethod;
import org.granite.generator.as3.reflect.JavaRemoteDestination;
import org.granite.generator.javafx.JavaFXGroovyTransformer;
import org.granite.test.builder.MockJavaFXGroovyConfiguration;
import org.granite.test.builder.Util;
import org.granite.test.builder.entities.Entity1;
import org.granite.test.builder.entities.Entity2;
import org.junit.Test;

public class TestGenRepositoryGDS1076 {

	@Test
	public void testGenRepository() {
		JavaAs3GroovyConfiguration config = new MockJavaFXGroovyConfiguration();
		JavaFXGroovyTransformer provider = new JavaFXGroovyTransformer(config, null);
		
		JavaRemoteDestination jrd = new JavaRemoteDestination(provider, SecureEntity1Repository.class, null);
		JavaMethod jm = Util.findMethod(jrd, "save", Entity1.class);
		Assert.assertEquals("Method return type", "org.granite.test.builder.entities.Entity1", jm.getClientReturnType().getQualifiedName());
	}

	@Test
	public void testGenRepository2() {
		JavaAs3GroovyConfiguration config = new MockJavaFXGroovyConfiguration();
		JavaFXGroovyTransformer provider = new JavaFXGroovyTransformer(config, null);
		
		JavaRemoteDestination jrd = new JavaRemoteDestination(provider, SecureEntity2Repository.class, null);
		JavaMethod jm = Util.findMethod(jrd, "save", Entity1.class);
		Assert.assertEquals("Method return type", "org.granite.test.builder.entities.Entity1", jm.getClientReturnType().getQualifiedName());
		
		JavaRemoteDestination jrd2 = new JavaRemoteDestination(provider, SecureEntity3Repository.class, null);
		JavaMethod jm2 = Util.findMethod(jrd2, "save", Entity2.class);
		Assert.assertEquals("Method return type", "org.granite.test.builder.entities.Entity2", jm2.getClientReturnType().getQualifiedName());
	}

	@Test
	public void testGenRepository2b() {
		JavaAs3GroovyConfiguration config = new MockJavaFXGroovyConfiguration();
		JavaFXGroovyTransformer provider = new JavaFXGroovyTransformer(config, null);
		
		JavaRemoteDestination jrd = new JavaRemoteDestination(provider, CustomRepository1.class, null);
		JavaMethod jm = Util.findMethod(jrd, "save", Entity1.class);
		Assert.assertEquals("Method return type", "org.granite.test.builder.entities.Entity1", jm.getClientReturnType().getQualifiedName());
		
		JavaRemoteDestination jrd2 = new JavaRemoteDestination(provider, CustomRepository2.class, null);
		JavaMethod jm2 = Util.findMethod(jrd2, "save", Entity2.class);
		Assert.assertEquals("Method return type", "org.granite.test.builder.entities.Entity2", jm2.getClientReturnType().getQualifiedName());
	}
	
	@Test
	public void testGenRepository3() {
		MockJavaFXGroovyConfiguration config = new MockJavaFXGroovyConfiguration();
		config.addFileSetClasses(Entity1.class);
		config.addTranslator(Entity1.class.getPackage().getName(), Entity1.class.getPackage().getName() + ".client");
		JavaFXGroovyTransformer provider = new JavaFXGroovyTransformer(config, null);
		
		JavaRemoteDestination jrd = new JavaRemoteDestination(provider, Entity1Repository.class, null);
		JavaMethod jm = Util.findMethod(jrd, "getEntities", int.class);
		Assert.assertEquals("Method return type", "java.util.List<Entity1>", jm.getClientReturnType().getQualifiedName());
		
		JavaImport ji = Util.findImport(jrd, Entity1.class.getName().replace("Entity1", "client.Entity1"));
		Assert.assertNotNull(ji);
	}
	
	@Test
	public void testGenRepository4() {
		MockJavaFXGroovyConfiguration config = new MockJavaFXGroovyConfiguration();
		config.addFileSetClasses(Entity2.class);
		JavaFXGroovyTransformer provider = new JavaFXGroovyTransformer(config, null);
		
		JavaRemoteDestination jrd = new JavaRemoteDestination(provider, Entity2Repository.class, null);
		JavaMethod jm = Util.findMethod(jrd, "deleteEntities", Iterable.class);
		Assert.assertEquals("Method param type", "java.lang.Iterable<Entity2>", jm.getClientParameterTypes()[0].getQualifiedName());
		
		JavaImport ji = Util.findImport(jrd, Entity2.class.getName());
		Assert.assertNotNull(ji);
	}
	
	@Test
	public void testGenRepository5() {
		MockJavaFXGroovyConfiguration config = new MockJavaFXGroovyConfiguration();
		config.addFileSetClasses(Entity2.class);
		JavaFXGroovyTransformer provider = new JavaFXGroovyTransformer(config, null);
		
		JavaRemoteDestination jrd = new JavaRemoteDestination(provider, SimpleRepository.class, null);
		JavaMethod jm = Util.findMethod(jrd, "doSomething", int.class);
		Assert.assertEquals("Method return type", "java.util.Map<String, String>", jm.getClientReturnType().getQualifiedName());
		
		Assert.assertEquals("No import", 1, jrd.getImports().size());
	}
	
}
