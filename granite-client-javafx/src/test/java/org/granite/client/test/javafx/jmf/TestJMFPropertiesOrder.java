/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of the Granite Data Services Platform.
 *
 *                               ***
 *
 *   Community License: GPL 3.0
 *
 *   This file is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published
 *   by the Free Software Foundation, either version 3 of the License,
 *   or (at your option) any later version.
 *
 *   This file is distributed in the hope that it will be useful, but
 *   WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *                               ***
 *
 *   Available Commercial License: GraniteDS SLA 1.0
 *
 *   This is the appropriate option if you are creating proprietary
 *   applications and you are not prepared to distribute and share the
 *   source code of your application under the GPL v3 license.
 *
 *   Please visit http://www.granitedataservices.com/license for more
 *   details.
 */
package org.granite.client.test.javafx.jmf;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
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
		
		clientAliasRegistry.registerAlias(ClientConcretePersitableChild.class);

		ConcretePersitableChild entity = new ConcretePersitableChild();
		entity.setId(12);
		entity.setA("AAAA");
		entity.setZ("ZZZZZZZZZ");
		
		Object clientEntity = serializeAndDeserializeServerToClient(entity, true);
		Assert.assertTrue(clientEntity instanceof ClientConcretePersitableChild);
		Assert.assertEquals(entity.getId(), ((ClientConcretePersitableChild)clientEntity).getId());
		Assert.assertEquals(entity.getA(), ((ClientConcretePersitableChild)clientEntity).getA());
		Assert.assertEquals(entity.getZ(), ((ClientConcretePersitableChild)clientEntity).getZ());
		
		Object serverEntity = serializeAndDeserializeClientToServer(clientEntity, true);
		Assert.assertTrue(serverEntity instanceof ConcretePersitableChild);
		Assert.assertEquals(((ClientConcretePersitableChild)clientEntity).getId(), ((ConcretePersitableChild)serverEntity).getId());
		Assert.assertEquals(((ClientConcretePersitableChild)clientEntity).getA(), ((ConcretePersitableChild)serverEntity).getA());
		Assert.assertEquals(((ClientConcretePersitableChild)clientEntity).getZ(), ((ConcretePersitableChild)serverEntity).getZ());
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
