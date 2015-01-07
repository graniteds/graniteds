/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2015 GRANITE DATA SERVICES S.A.S.
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

import org.junit.Assert;
import org.granite.generator.Generator;
import org.granite.generator.as3.JavaAs3GroovyConfiguration;
import org.granite.generator.as3.JavaAs3GroovyTransformer;
import org.granite.generator.as3.JavaAs3Input;
import org.granite.generator.as3.JavaAs3Output;
import org.granite.generator.as3.reflect.JavaMethodProperty;
import org.granite.generator.as3.reflect.JavaRemoteDestination;
import org.granite.generator.as3.reflect.JavaStatefulDestination;
import org.granite.generator.gsp.GroovyTemplate;
import org.granite.test.generator.MockJavaAs3GroovyConfiguration;
import org.granite.test.generator.MockListener;
import org.granite.test.generator.Util;
import org.granite.test.generator.entities.Entity1;
import org.junit.Test;

public class TestGenRemoteDestination {

	@Test
	public void testReflectRemoteDestination() {
		JavaAs3GroovyConfiguration config = new MockJavaAs3GroovyConfiguration();
		JavaAs3GroovyTransformer provider = new JavaAs3GroovyTransformer(config, null);
		JavaRemoteDestination jrd = new JavaRemoteDestination(provider, Service.class, null);
		
		Assert.assertEquals("Properties", 0, jrd.getProperties().size());
		Assert.assertEquals("Methods", 5, jrd.getMethods().size());
	}
	
	@Test
	public void testReflectStatefulDestination() {
		JavaAs3GroovyConfiguration config = new MockJavaAs3GroovyConfiguration();
		JavaAs3GroovyTransformer provider = new JavaAs3GroovyTransformer(config, null);
		JavaStatefulDestination jrd = new JavaStatefulDestination(provider, Service.class, null);
		
		Assert.assertEquals("Properties", 2, jrd.getProperties().size());
		for (JavaMethodProperty jmp : jrd.getProperties()) {
			if (jmp.getName().equals("prop")) {
				Assert.assertNotNull("Prop 1 get", jmp.getReadMethod());
				Assert.assertNull("Prop 1 set", jmp.getWriteMethod());
			}
			else if (jmp.getName().equals("bla")) {
				Assert.assertNotNull("Prop 2 get", jmp.getReadMethod());
				Assert.assertNotNull("Prop 2 set", jmp.getWriteMethod());
			}
			else
				Assert.fail("Unknown property " + jmp.getName());
		}
	}
	
	@Test
	public void testTideTemplateRemoteDestinationInterface() throws Exception {
		MockJavaAs3GroovyConfiguration config = new MockJavaAs3GroovyConfiguration();
		config.setTide(true);
		config.addFileSetClasses(Service.class);
		JavaAs3GroovyTransformer provider = new JavaAs3GroovyTransformer(config, new MockListener()) {
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
		Assert.assertTrue("Base contains doSomething", base.indexOf("public function doSomething(resultHandler:Object = null, faultHandler:Function = null):AsyncToken") >= 0);
		Assert.assertTrue("Base contains doSomethingElse", base.indexOf("public function doSomethingElse(arg0:String, resultHandler:Object = null, faultHandler:Function = null):AsyncToken") >= 0);
	}
	
	@Test
	public void testTideTemplateRemoteDestinationClass() throws Exception {
		MockJavaAs3GroovyConfiguration config = new MockJavaAs3GroovyConfiguration();
		config.setTide(true);
		config.addFileSetClasses(ServiceImpl.class);
		JavaAs3GroovyTransformer provider = new JavaAs3GroovyTransformer(config, new MockListener()) {
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
		Assert.assertTrue("Base contains doSomething", base.indexOf("public function doSomething(resultHandler:Object = null, faultHandler:Function = null):AsyncToken") >= 0);
		Assert.assertTrue("Base contains doSomethingElse", base.indexOf("public function doSomethingElse(arg0:String, resultHandler:Object = null, faultHandler:Function = null):AsyncToken") >= 0);
		Assert.assertFalse("Base contains private", base.indexOf("testInternal") >= 0);
	}
	
	@Test
	public void testTideTemplateLazyParameter() throws Exception {
		MockJavaAs3GroovyConfiguration config = new MockJavaAs3GroovyConfiguration();
		config.setTide(true);
		config.addFileSetClasses(EntityService.class, Entity1.class);
		JavaAs3GroovyTransformer provider = new JavaAs3GroovyTransformer(config, new MockListener()) {
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
		Assert.assertTrue("Base contains saveLazy", base.indexOf("[Lazy(\"arg0\")]") >= 0);
		Assert.assertTrue("Base contains saveLazy", base.indexOf("public function saveLazy(arg0:Entity1, resultHandler:Object = null, faultHandler:Function = null):AsyncToken") >= 0);
		Assert.assertTrue("Base contains saveLazy2", base.indexOf("[Lazy(\"entity\")]") >= 0);
		Assert.assertTrue("Base contains saveLazy2", base.indexOf("public function saveLazy2(arg0:String, entity:Entity1, resultHandler:Object = null, faultHandler:Function = null):AsyncToken") >= 0);
	}
	
}
