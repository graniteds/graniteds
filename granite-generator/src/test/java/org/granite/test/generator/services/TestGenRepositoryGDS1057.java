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

import org.junit.Assert;

import org.granite.generator.as3.reflect.JavaMethod;
import org.granite.generator.as3.reflect.JavaRemoteDestination;
import org.granite.generator.java.JavaGroovyTransformer;
import org.granite.test.generator.MockJavaFXGroovyConfiguration;
import org.granite.test.generator.Util;
import org.granite.test.generator.entities.Entity1;
import org.granite.test.generator.entities.Entity2;
import org.junit.Test;

public class TestGenRepositoryGDS1057 {

	@Test
	public void testGenRepository() {
		MockJavaFXGroovyConfiguration config = new MockJavaFXGroovyConfiguration();
		config.addFileSetClasses(Entity1.class, Entity2.class);
		JavaGroovyTransformer provider = new JavaGroovyTransformer(config, null);
		
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
		JavaGroovyTransformer provider = new JavaGroovyTransformer(config, null);
		
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
		JavaGroovyTransformer provider = new JavaGroovyTransformer(config, null);
		
		JavaRemoteDestination jrd1 = new JavaRemoteDestination(provider, SecureEntity2Repository.class, null);
		JavaMethod jm1 = Util.findMethod(jrd1, "findAll");
		Assert.assertEquals("Method return type", "List<Entity1>", jm1.getClientReturnType().getName());
		
		JavaRemoteDestination jrd2 = new JavaRemoteDestination(provider, SecureEntity3Repository.class, null);
		JavaMethod jm2 = Util.findMethod(jrd2, "findAll");
		Assert.assertEquals("Method return type", "List<Entity2>", jm2.getClientReturnType().getName());
	}
	
}
