/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2014 GRANITE DATA SERVICES S.A.S.
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
package org.granite.test.generator.services;

import java.io.File;

import org.granite.generator.Generator;
import org.granite.generator.as3.JavaAs3Input;
import org.granite.generator.as3.JavaAs3Output;
import org.granite.generator.gsp.GroovyTemplate;
import org.granite.generator.java.JavaGroovyTransformer;
import org.granite.test.generator.MockJavaFXGroovyConfiguration;
import org.granite.test.generator.MockListener;
import org.granite.test.generator.Util;
import org.granite.test.generator.entities.Entity1;
import org.junit.Assert;
import org.junit.Test;

public class TestGenRemoteDestinationJavaFX {

	@Test
	public void testTideTemplateRemoteDestinationInterface() throws Exception {
		MockJavaFXGroovyConfiguration config = new MockJavaFXGroovyConfiguration();
		config.setTide(true);
		config.addFileSetClasses(Service.class);
		JavaGroovyTransformer provider = new JavaGroovyTransformer(config, new MockListener()) {
			@Override
			public boolean isOutdated(JavaAs3Input input, GroovyTemplate template, File outputFile, boolean hasBaseTemplate) {
				return true;
			}
		};
		Generator generator = new Generator(config);
		generator.add(provider);
		JavaAs3Input input = new JavaAs3Input(Service.class, 
				new File(Service.class.getClassLoader().getResource(Service.class.getName().replace('.', '/') + ".class").toURI()));
		JavaAs3Output[] outputs = (JavaAs3Output[])generator.generate(input);
		
		Assert.assertEquals("Output", 2, outputs.length);
		
		String base = Util.readFile(outputs[0].getFile());
		Assert.assertTrue("Base contains doSomething", base.indexOf("public Future<Void> doSomething(TideResponder<Void> tideResponder)") >= 0);
		Assert.assertTrue("Base contains doSomethingElse", base.indexOf("public Future<String> doSomethingElse(String arg0, TideResponder<String> tideResponder)") >= 0);
	}
	
	@Test
	public void testTideTemplateRemoteDestinationClass() throws Exception {
		MockJavaFXGroovyConfiguration config = new MockJavaFXGroovyConfiguration();
		config.setTide(true);
		config.addFileSetClasses(ServiceImpl.class);
		JavaGroovyTransformer provider = new JavaGroovyTransformer(config, new MockListener()) {
			@Override
			public boolean isOutdated(JavaAs3Input input, GroovyTemplate template, File outputFile, boolean hasBaseTemplate) {
				return true;
			}
		};
		Generator generator = new Generator(config);
		generator.add(provider);
		JavaAs3Input input = new JavaAs3Input(ServiceImpl.class, 
				new File(ServiceImpl.class.getClassLoader().getResource(ServiceImpl.class.getName().replace('.', '/') + ".class").toURI()));
		JavaAs3Output[] outputs = (JavaAs3Output[])generator.generate(input);
		
		Assert.assertEquals("Output", 2, outputs.length);
		
		String base = Util.readFile(outputs[0].getFile());
		Assert.assertTrue("Base contains doSomething", base.indexOf("public Future<Void> doSomething(TideResponder<Void> tideResponder)") >= 0);
		Assert.assertTrue("Base contains doSomethingElse", base.indexOf("public Future<String> doSomethingElse(String arg0, TideResponder<String> tideResponder)") >= 0);
		Assert.assertFalse("Base contains private", base.indexOf("testInternal") >= 0);
	}
	
	@Test
	public void testTideTemplateLazyParameter() throws Exception {
		MockJavaFXGroovyConfiguration config = new MockJavaFXGroovyConfiguration();
		config.setTide(true);
		config.addFileSetClasses(EntityService.class, Entity1.class);
		JavaGroovyTransformer provider = new JavaGroovyTransformer(config, new MockListener()) {
			@Override
			public boolean isOutdated(JavaAs3Input input, GroovyTemplate template, File outputFile, boolean hasBaseTemplate) {
				return true;
			}
		};
		Generator generator = new Generator(config);
		generator.add(provider);
		JavaAs3Input input = new JavaAs3Input(EntityService.class, 
				new File(EntityService.class.getClassLoader().getResource(EntityService.class.getName().replace('.', '/') + ".class").toURI()));
		JavaAs3Output[] outputs = (JavaAs3Output[])generator.generate(input);
		
		Assert.assertEquals("Output", 2, outputs.length);
		
		String base = Util.readFile(outputs[0].getFile());
		Assert.assertTrue("Base contains saveLazy", base.indexOf("public Future<Entity1> saveLazy(@Lazy Entity1 arg0, TideResponder<Entity1> tideResponder)") >= 0);
		Assert.assertTrue("Base contains saveLazy2", base.indexOf("public Future<Entity1> saveLazy2(String arg0, @Lazy Entity1 entity, TideResponder<Entity1> tideResponder)") >= 0);
		Assert.assertTrue("Base contains import", base.indexOf("import org.granite.client.persistence.Lazy") >= 0);
	}
	
	@Test
	public void testTideServiceGDS1316() throws Exception {
		MockJavaFXGroovyConfiguration config = new MockJavaFXGroovyConfiguration();
		config.setTide(true);
		config.addFileSetClasses(MapService.class, Entity1.class);
		JavaGroovyTransformer provider = new JavaGroovyTransformer(config, new MockListener()) {
			@Override
			public boolean isOutdated(JavaAs3Input input, GroovyTemplate template, File outputFile, boolean hasBaseTemplate) {
				return true;
			}
		};
		Generator generator = new Generator(config);
		generator.add(provider);
		JavaAs3Input input = new JavaAs3Input(MapService.class, 
				new File(MapService.class.getClassLoader().getResource(EntityService.class.getName().replace('.', '/') + ".class").toURI()));
		JavaAs3Output[] outputs = (JavaAs3Output[])generator.generate(input);
		
		Assert.assertEquals("Output", 2, outputs.length);
		
		String base = Util.readFile(outputs[0].getFile());
		Assert.assertTrue("Base contains findMap", base.indexOf("public Future<Map<String, Object>> findMap(String arg0, TideResponder<Map<String, Object>> tideResponder)") >= 0);
		Assert.assertTrue("Base contains findHashMap", base.indexOf("public Future<HashMap<String, Object>> findHashMap(String arg0, TideResponder<HashMap<String, Object>> tideResponder)") >= 0);
		Assert.assertTrue("Base contains findListHashMap", base.indexOf("public Future<List<HashMap<String, Object>>> findListHashMap(String arg0, TideResponder<List<HashMap<String, Object>>> tideResponder)") >= 0);
		Assert.assertTrue("Base contains findListMapEntity", base.indexOf("public Future<List<Map<String, Entity1>>> findListMapEntity(String arg0, TideResponder<List<Map<String, Entity1>>> tideResponder)") >= 0);
		Assert.assertTrue("Base contains findListMapListEntity", base.indexOf("public Future<List<Map<String, List<Entity1>>>> findListMapListEntity(String arg0, TideResponder<List<Map<String, List<Entity1>>>> tideResponder)") >= 0);
	}
	
}
