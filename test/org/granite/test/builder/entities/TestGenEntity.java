package org.granite.test.builder.entities;

import java.io.File;

import junit.framework.Assert;

import org.granite.generator.Generator;
import org.granite.generator.as3.JavaAs3GroovyTransformer;
import org.granite.generator.as3.JavaAs3Input;
import org.granite.generator.as3.JavaAs3Output;
import org.granite.generator.gsp.GroovyTemplate;
import org.granite.test.builder.MockJavaAs3GroovyConfiguration;
import org.granite.test.builder.MockListener;
import org.granite.test.builder.Util;
import org.granite.test.builder.entities.Entity1;
import org.junit.Test;

public class TestGenEntity {

	@Test
	public void testTideTemplateEntity() throws Exception {
		MockJavaAs3GroovyConfiguration config = new MockJavaAs3GroovyConfiguration();
		config.setTide(true);
		config.addFileSetClasses(Entity1.class);
		JavaAs3GroovyTransformer provider = new JavaAs3GroovyTransformer(config, new MockListener()) {
			@Override
			public boolean isOutdated(JavaAs3Input input, GroovyTemplate template, File outputFile, boolean hasBaseTemplate) {
				return true;
			}
		};
		Generator generator = new Generator(config);
		generator.add(provider);
		JavaAs3Input input = new JavaAs3Input(Entity1.class, 
				new File(Entity1.class.getClassLoader().getResource(Entity1.class.getName().replace('.', '/') + ".class").toURI()));
		JavaAs3Output[] outputs = (JavaAs3Output[])generator.generate(input);
		
		Assert.assertEquals("Output", 2, outputs.length);
		
		String base = Util.readFile(outputs[0].getFile());
		Assert.assertTrue("Base contains [Lazy]", base.indexOf("[Lazy]\n        public function get entities():ListCollectionView") >= 0);
	}
	
	
	@Test
	public void testTideTemplateAbstractEntityUid() throws Exception {
		MockJavaAs3GroovyConfiguration config = new MockJavaAs3GroovyConfiguration();
		config.setTide(true);
		config.addFileSetClasses(AbstractEntity1.class, Entity3.class);
		JavaAs3GroovyTransformer provider = new JavaAs3GroovyTransformer(config, new MockListener()) {
			@Override
			public boolean isOutdated(JavaAs3Input input, GroovyTemplate template, File outputFile, boolean hasBaseTemplate) {
				return true;
			}
		};
		Generator generator = new Generator(config);
		generator.add(provider);
		JavaAs3Input input = new JavaAs3Input(AbstractEntity1.class, 
				new File(AbstractEntity1.class.getClassLoader().getResource(AbstractEntity1.class.getName().replace('.', '/') + ".class").toURI()));
		JavaAs3Output[] outputs = (JavaAs3Output[])generator.generate(input);
		
		Assert.assertEquals("Output", 2, outputs.length);
		
		String base = Util.readFile(outputs[0].getFile());
		Assert.assertTrue("AbstractEntityBase1 contains [Id]", base.indexOf("[Id]") >= 0);
		Assert.assertTrue("AbstractEntityBase1 contains _uid", base.indexOf("private var _uid:String") >= 0);
		Assert.assertTrue("AbstractEntityBase1 contains get uid", base.indexOf("public function get uid():String") >= 0);
	}
}
