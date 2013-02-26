package org.granite.test.builder.entities;

import java.io.File;

import junit.framework.Assert;

import org.granite.generator.Generator;
import org.granite.generator.as3.JavaAs3GroovyTransformer;
import org.granite.generator.as3.JavaAs3Input;
import org.granite.generator.as3.JavaAs3Output;
import org.granite.generator.gsp.GroovyTemplate;
import org.granite.generator.javafx.JavaFXGroovyTransformer;
import org.granite.test.builder.MockJavaAs3GroovyConfiguration;
import org.granite.test.builder.MockJavaFXGroovyConfiguration;
import org.granite.test.builder.MockListener;
import org.granite.test.builder.Util;
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
	public void testTideTemplateEntityGDS1046() throws Exception {
		MockJavaAs3GroovyConfiguration config = new MockJavaAs3GroovyConfiguration();
		config.setTide(true);
		config.addFileSetClasses(EntityGDS1046.class);
		JavaAs3GroovyTransformer provider = new JavaAs3GroovyTransformer(config, new MockListener()) {
			@Override
			public boolean isOutdated(JavaAs3Input input, GroovyTemplate template, File outputFile, boolean hasBaseTemplate) {
				return true;
			}
		};
		Generator generator = new Generator(config);
		generator.add(provider);
		JavaAs3Input input = new JavaAs3Input(EntityGDS1046.class, 
				new File(EntityGDS1046.class.getClassLoader().getResource(EntityGDS1046.class.getName().replace('.', '/') + ".class").toURI()));
		JavaAs3Output[] outputs = (JavaAs3Output[])generator.generate(input);
		
		Assert.assertEquals("Output", 2, outputs.length);
		
		String base = Util.readFile(outputs[0].getFile());
		Assert.assertTrue("Base contains getter", base.indexOf("public function get isSomething():Boolean") >= 0);
	}

	@Test
	public void testTideTemplateEntityGDS1049() throws Exception {
		MockJavaAs3GroovyConfiguration config = new MockJavaAs3GroovyConfiguration();
		config.setTide(true);
		config.addFileSetClasses(EntityGDS1049.class, Entity4.class);
		JavaAs3GroovyTransformer provider = new JavaAs3GroovyTransformer(config, new MockListener()) {
			@Override
			public boolean isOutdated(JavaAs3Input input, GroovyTemplate template, File outputFile, boolean hasBaseTemplate) {
				return true;
			}
		};
		Generator generator = new Generator(config);
		generator.add(provider);
		JavaAs3Input input = new JavaAs3Input(EntityGDS1049.class, 
				new File(EntityGDS1049.class.getClassLoader().getResource(EntityGDS1049.class.getName().replace('.', '/') + ".class").toURI()));
		JavaAs3Output[] outputs = (JavaAs3Output[])generator.generate(input);
		
		Assert.assertEquals("Output", 2, outputs.length);
		
		String base = Util.readFile(outputs[0].getFile());
		Assert.assertTrue("Base contains setter", base.indexOf("public function set parent(value:Entity4):void") >= 0);
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
	
	@Test
	public void testTideTemplateBean() throws Exception {
		MockJavaFXGroovyConfiguration config = new MockJavaFXGroovyConfiguration();
		config.setTide(true);
		config.addFileSetClasses(Bean1.class);
		JavaFXGroovyTransformer provider = new JavaFXGroovyTransformer(config, new MockListener()) {
			@Override
			public boolean isOutdated(JavaAs3Input input, GroovyTemplate template, File outputFile, boolean hasBaseTemplate) {
				return true;
			}
		};
		Generator generator = new Generator(config);
		generator.add(provider);
		JavaAs3Input input = new JavaAs3Input(Bean1.class, 
				new File(Bean1.class.getClassLoader().getResource(AbstractEntity1.class.getName().replace('.', '/') + ".class").toURI()));
		JavaAs3Output[] outputs = (JavaAs3Output[])generator.generate(input);
		
		Assert.assertEquals("Output", 2, outputs.length);
		
		String base = Util.readFile(outputs[0].getFile());
		Assert.assertTrue("Bean1Base contains name", base.indexOf("private StringProperty name = new SimpleStringProperty(this, \"name\");") >= 0);
		Assert.assertTrue("Bean1Base contains list", base.indexOf("private ObservableList<String> list = new PersistentList<String>()") >= 0);
	}
}
