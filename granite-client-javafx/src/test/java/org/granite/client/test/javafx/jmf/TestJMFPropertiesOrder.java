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
package org.granite.client.test.javafx.jmf;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.granite.client.messaging.ClientAliasRegistry;
import org.granite.client.messaging.jmf.ClientSharedContext;
import org.granite.client.messaging.jmf.DefaultClientSharedContext;
import org.granite.client.messaging.jmf.ext.ClientEntityCodec;
import org.granite.client.test.javafx.jmf.Util.ByteArrayJMFDeserializer;
import org.granite.client.test.javafx.jmf.Util.ByteArrayJMFDumper;
import org.granite.client.test.javafx.jmf.Util.ByteArrayJMFSerializer;
import org.granite.hibernate.jmf.EntityCodec;
import org.granite.hibernate.jmf.PersistentBagCodec;
import org.granite.hibernate.jmf.PersistentListCodec;
import org.granite.hibernate.jmf.PersistentMapCodec;
import org.granite.hibernate.jmf.PersistentSetCodec;
import org.granite.hibernate.jmf.PersistentSortedMapCodec;
import org.granite.hibernate.jmf.PersistentSortedSetCodec;
import org.granite.messaging.jmf.DefaultCodecRegistry;
import org.granite.messaging.jmf.DefaultSharedContext;
import org.granite.messaging.jmf.JMFDumper;
import org.granite.messaging.jmf.SharedContext;
import org.granite.messaging.jmf.codec.ExtendedObjectCodec;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestJMFPropertiesOrder {
	
	private SharedContext dumpSharedContext;
	private SharedContext serverSharedContext;
	private ClientAliasRegistry clientAliasRegistry = new ClientAliasRegistry();
	private ClientSharedContext clientSharedContext;
	
	@Before
	public void before() {
		
		List<ExtendedObjectCodec> serverExtendedObjectCodecs = Arrays.asList(
			new EntityCodec(),
			new PersistentListCodec(),
			new PersistentSetCodec(),
			new PersistentBagCodec(),
			new PersistentMapCodec(),
			new PersistentSortedSetCodec(),
			new PersistentSortedMapCodec()
		);
		List<ExtendedObjectCodec> clientExtendedObjectCodecs = Arrays.asList((ExtendedObjectCodec)
			new ClientEntityCodec()
		);
		
		dumpSharedContext = new DefaultSharedContext(new DefaultCodecRegistry());
		
		serverSharedContext = new DefaultSharedContext(new DefaultCodecRegistry(serverExtendedObjectCodecs));
		
		clientSharedContext = new DefaultClientSharedContext(new DefaultCodecRegistry(clientExtendedObjectCodecs), null, null, clientAliasRegistry);
	}
	
	@After
	public void after() {
		dumpSharedContext = null;
		serverSharedContext = null;
		clientSharedContext = null;
	}

	@Test
	public void testPropertiesOrder() throws ClassNotFoundException, IOException {
		
		clientAliasRegistry.registerAlias(ClientConcretePersistableChild.class);

		ConcretePersistableChild entity = new ConcretePersistableChild();
		entity.setId(12);
		entity.setA("AAAA");
		entity.setZ("ZZZZZZZZZ");
		
		Object clientEntity = serializeAndDeserializeServerToClient(entity, true);
		Assert.assertTrue(clientEntity instanceof ClientConcretePersistableChild);
		Assert.assertEquals(entity.getId(), ((ClientConcretePersistableChild)clientEntity).getId());
		Assert.assertEquals(entity.getA(), ((ClientConcretePersistableChild)clientEntity).getA());
		Assert.assertEquals(entity.getZ(), ((ClientConcretePersistableChild)clientEntity).getZ());
		
		Object serverEntity = serializeAndDeserializeClientToServer(clientEntity, true);
		Assert.assertTrue(serverEntity instanceof ConcretePersistableChild);
		Assert.assertEquals(((ClientConcretePersistableChild)clientEntity).getId(), ((ConcretePersistableChild)serverEntity).getId());
		Assert.assertEquals(((ClientConcretePersistableChild)clientEntity).getA(), ((ConcretePersistableChild)serverEntity).getA());
		Assert.assertEquals(((ClientConcretePersistableChild)clientEntity).getZ(), ((ConcretePersistableChild)serverEntity).getZ());
	}

	@Test
	public void testEntityPropertiesOrder() throws ClassNotFoundException, IOException {
		
		clientAliasRegistry.registerAlias(ClientConcreteEntity.class);

		ConcreteEntity entity = new ConcreteEntity();
		entity.setId(12L);
		entity.setCreateDate(new Date());
		entity.setCreateUser("John");
		entity.setBla("3247932");
		entity.setCla("eiwuryti");
		
		Object clientEntity = serializeAndDeserializeServerToClient(entity, true);
		Assert.assertTrue(clientEntity instanceof ClientConcreteEntity);
		Assert.assertEquals(entity.getId(), ((ClientConcreteEntity)clientEntity).getId());
		Assert.assertEquals(entity.getUid(), ((ClientConcreteEntity)clientEntity).getUid());
		Assert.assertEquals(entity.getVersion(), ((ClientConcreteEntity)clientEntity).getVersion());
		Assert.assertEquals(entity.getCreateDate(), ((ClientConcreteEntity)clientEntity).getCreateDate());
		Assert.assertEquals(entity.getCreateUser(), ((ClientConcreteEntity)clientEntity).getCreateUser());
		Assert.assertEquals(entity.getBla(), ((ClientConcreteEntity)clientEntity).getBla());
		Assert.assertEquals(entity.getCla(), ((ClientConcreteEntity)clientEntity).getCla());
		
		Object serverEntity = serializeAndDeserializeClientToServer(clientEntity, true);
		Assert.assertTrue(serverEntity instanceof ConcreteEntity);
		Assert.assertEquals(((ClientConcreteEntity)clientEntity).getId(), ((ConcreteEntity)serverEntity).getId());
		Assert.assertEquals(((ClientConcreteEntity)clientEntity).getUid(), ((ConcreteEntity)serverEntity).getUid());
		Assert.assertEquals(((ClientConcreteEntity)clientEntity).getVersion(), ((ConcreteEntity)serverEntity).getVersion());
		Assert.assertEquals(((ClientConcreteEntity)clientEntity).getCreateDate(), ((ConcreteEntity)serverEntity).getCreateDate());
		Assert.assertEquals(((ClientConcreteEntity)clientEntity).getCreateUser(), ((ConcreteEntity)serverEntity).getCreateUser());
		Assert.assertEquals(((ClientConcreteEntity)clientEntity).getBla(), ((ConcreteEntity)serverEntity).getBla());
		Assert.assertEquals(((ClientConcreteEntity)clientEntity).getCla(), ((ConcreteEntity)serverEntity).getCla());
	}
//	
//	private Object serializeAndDeserializeServerToServer(Object obj, boolean dump) throws ClassNotFoundException, IOException {
//		return serializeAndDeserialize(serverSharedContext, dumpSharedContext, serverSharedContext, obj, dump);
//	}
	
	private Object serializeAndDeserializeServerToClient(Object obj, boolean dump) throws ClassNotFoundException, IOException {
		return serializeAndDeserialize(serverSharedContext, dumpSharedContext, clientSharedContext, obj, dump);
	}
	
	private Object serializeAndDeserializeClientToServer(Object obj, boolean dump) throws ClassNotFoundException, IOException {
		return serializeAndDeserialize(clientSharedContext, dumpSharedContext, serverSharedContext, obj, dump);
	}
	
	private Object serializeAndDeserialize(
		SharedContext serializeSharedContext,
		SharedContext dumpSharedContext,
		SharedContext deserializeSharedContext,
		Object obj,
		boolean dump) throws ClassNotFoundException, IOException {
		
		ByteArrayJMFSerializer serializer = new ByteArrayJMFSerializer(serializeSharedContext);
		serializer.writeObject(obj);
		serializer.close();
		byte[] bytes = serializer.toByteArray();
		
		
		PrintStream ps = Util.newNullPrintStream();
		if (dump) {
			System.out.println(bytes.length + "B. " + Util.toHexString(bytes));
			ps = System.out;
		}
		
		JMFDumper dumper = new ByteArrayJMFDumper(bytes, dumpSharedContext, ps);
		dumper.dump();
		dumper.close();
		
		ByteArrayJMFDeserializer deserializer = new ByteArrayJMFDeserializer(bytes, deserializeSharedContext);
		Object clone = deserializer.readObject();
		deserializer.close();
		return clone;
	}
}
