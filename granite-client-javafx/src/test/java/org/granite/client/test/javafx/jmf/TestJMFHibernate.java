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
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;

import org.granite.client.javafx.platform.JavaFXPlatform;
import org.granite.client.messaging.ClientAliasRegistry;
import org.granite.client.messaging.jmf.ClientSharedContext;
import org.granite.client.messaging.jmf.DefaultClientSharedContext;
import org.granite.client.messaging.jmf.ext.ClientEntityCodec;
import org.granite.client.persistence.Persistence;
import org.granite.client.platform.Platform;
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
import org.hibernate.LazyInitializationException;
import org.hibernate.collection.PersistentBag;
import org.hibernate.collection.PersistentList;
import org.hibernate.collection.PersistentMap;
import org.hibernate.collection.PersistentSet;
import org.hibernate.collection.PersistentSortedMap;
import org.hibernate.collection.PersistentSortedSet;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestJMFHibernate {
	
	private SharedContext dumpSharedContext;
	private SharedContext serverSharedContext;
	private ClientAliasRegistry clientAliasRegistry;
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
		
		clientAliasRegistry = new ClientAliasRegistry();
		
		clientSharedContext = new DefaultClientSharedContext(new DefaultCodecRegistry(clientExtendedObjectCodecs), null, null, clientAliasRegistry);
	}
	
	@After
	public void after() {
		dumpSharedContext = null;
		serverSharedContext = null;
		clientSharedContext = null;
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testPersistentList() throws ClassNotFoundException, IOException {
		PersistentList list = new PersistentList(null);
		
		Object collection = serializeAndDeserializeServerToServer(list, false);
		Assert.assertTrue(collection instanceof PersistentList);
		Assert.assertFalse(((PersistentList)collection).wasInitialized());
		try {
			((PersistentList)collection).add(new Object());
			Assert.fail("Should throw a org.hibernate.LazyInitializationException");
		}
		catch (LazyInitializationException e) {
		}
		
		collection = serializeAndDeserializeServerToClient(list, true);
		Assert.assertTrue(collection instanceof org.granite.client.persistence.collection.PersistentList);
		Assert.assertFalse(((org.granite.client.persistence.collection.PersistentList<?>)collection).wasInitialized());
		try {
			((org.granite.client.persistence.collection.PersistentList<Object>)collection).add(new Object());
			Assert.fail("Should throw a org.granite.messaging.jmf.persistence.LazyInitializationException");
		}
		catch (org.granite.client.persistence.LazyInitializationException e) {
		}
		
		collection = serializeAndDeserializeClientToServer(collection, false);
		Assert.assertTrue(collection instanceof PersistentList);
		Assert.assertFalse(((PersistentList)collection).wasInitialized());
		try {
			((PersistentList)collection).add(new Object());
			Assert.fail("Should throw a org.hibernate.LazyInitializationException");
		}
		catch (LazyInitializationException e) {
		}
		
		list = new PersistentList(null, new ArrayList<Object>());
		
		collection = serializeAndDeserializeServerToServer(list, false);
		Assert.assertTrue(collection instanceof PersistentList);
		Assert.assertTrue(((PersistentList)collection).wasInitialized());
		Assert.assertFalse(((PersistentList)collection).isDirty());
		Assert.assertTrue(((PersistentList)collection).isEmpty());
		
		collection = serializeAndDeserializeServerToClient(list, false);
		Assert.assertTrue(collection instanceof org.granite.client.persistence.collection.PersistentList);
		Assert.assertTrue(((org.granite.client.persistence.collection.PersistentList<?>)collection).wasInitialized());
		Assert.assertFalse(((org.granite.client.persistence.collection.PersistentList<?>)collection).isDirty());
		Assert.assertTrue(((org.granite.client.persistence.collection.PersistentList<?>)collection).isEmpty());
		
		collection = serializeAndDeserializeClientToServer(collection, false);
		Assert.assertTrue(collection instanceof PersistentList);
		Assert.assertTrue(((PersistentList)collection).wasInitialized());
		Assert.assertFalse(((PersistentList)collection).isDirty());
		Assert.assertTrue(((PersistentList)collection).isEmpty());
		
		list = new PersistentList(null, Arrays.asList(1, 2, 3));
		
		collection = serializeAndDeserializeServerToServer(list, false);
		Assert.assertTrue(collection instanceof PersistentList);
		Assert.assertTrue(((PersistentList)collection).wasInitialized());
		Assert.assertFalse(((PersistentList)collection).isDirty());
		Assert.assertTrue(((PersistentList)collection).size() == 3);
		
		collection = serializeAndDeserializeServerToClient(list, false);
		Assert.assertTrue(collection instanceof org.granite.client.persistence.collection.PersistentList);
		Assert.assertTrue(((org.granite.client.persistence.collection.PersistentList<?>)collection).wasInitialized());
		Assert.assertFalse(((org.granite.client.persistence.collection.PersistentList<?>)collection).isDirty());
		Assert.assertFalse(((org.granite.client.persistence.collection.PersistentList<?>)collection).isEmpty());
		Assert.assertTrue(((org.granite.client.persistence.collection.PersistentList<?>)collection).size() == 3);
		Assert.assertTrue(((org.granite.client.persistence.collection.PersistentList<?>)collection).contains(1));
		Assert.assertTrue(((org.granite.client.persistence.collection.PersistentList<?>)collection).contains(2));
		Assert.assertTrue(((org.granite.client.persistence.collection.PersistentList<?>)collection).contains(3));
		Assert.assertTrue(((org.granite.client.persistence.collection.PersistentList<?>)collection).indexOf(1) == 0);
		Assert.assertTrue(((org.granite.client.persistence.collection.PersistentList<?>)collection).indexOf(2) == 1);
		Assert.assertTrue(((org.granite.client.persistence.collection.PersistentList<?>)collection).indexOf(3) == 2);
		Assert.assertTrue(((org.granite.client.persistence.collection.PersistentList<?>)collection).containsAll(Arrays.asList(2, 1, 3)));
		Assert.assertTrue(((org.granite.client.persistence.collection.PersistentList<?>)collection).get(0).equals(1));
		Assert.assertTrue(((org.granite.client.persistence.collection.PersistentList<?>)collection).get(1).equals(2));
		Assert.assertTrue(((org.granite.client.persistence.collection.PersistentList<?>)collection).get(2).equals(3));
		Assert.assertTrue(((org.granite.client.persistence.collection.PersistentList<?>)collection).equals(collection));
		Assert.assertFalse(((org.granite.client.persistence.collection.PersistentList<?>)collection).retainAll(Arrays.asList(3, 2, 1)));
		Assert.assertFalse(((org.granite.client.persistence.collection.PersistentList<Object>)collection).remove((Object)4));
		Assert.assertFalse(((org.granite.client.persistence.collection.PersistentList<Object>)collection).removeAll(Arrays.asList(4, 5)));
		Assert.assertTrue(((org.granite.client.persistence.collection.PersistentList<?>)collection).toString() != null);
		Assert.assertTrue(((org.granite.client.persistence.collection.PersistentList<?>)collection).hashCode() != 0);
		Assert.assertTrue(((org.granite.client.persistence.collection.PersistentList<?>)collection).iterator().next().equals(1));
		Assert.assertTrue(((org.granite.client.persistence.collection.PersistentList<?>)collection).listIterator().next().equals(1));
		Assert.assertTrue(((org.granite.client.persistence.collection.PersistentList<?>)collection).listIterator(1).next().equals(2));
		Assert.assertTrue(((org.granite.client.persistence.collection.PersistentList<?>)collection).listIterator(2).next().equals(3));
		ListIterator<Object> lit = ((org.granite.client.persistence.collection.PersistentList<Object>)collection).listIterator(1);
		lit.previous();
		lit.set(1);
		lit.next();
		lit.next();
		lit.set(2);
		lit.next();
		lit.set(3);
		Assert.assertTrue(((org.granite.client.persistence.collection.PersistentList<?>)collection).lastIndexOf(1) == 0);
		Assert.assertTrue(((org.granite.client.persistence.collection.PersistentList<?>)collection).lastIndexOf(2) == 1);
		Assert.assertTrue(((org.granite.client.persistence.collection.PersistentList<?>)collection).lastIndexOf(3) == 2);
		Assert.assertTrue(((org.granite.client.persistence.collection.PersistentList<Object>)collection).set(0, 1).equals(1));
		Assert.assertTrue(((org.granite.client.persistence.collection.PersistentList<Object>)collection).set(1, 2).equals(2));
		Assert.assertTrue(((org.granite.client.persistence.collection.PersistentList<Object>)collection).set(2, 3).equals(3));
		Assert.assertTrue(((org.granite.client.persistence.collection.PersistentList<Object>)collection).subList(1, 2).equals(Arrays.asList(2)));
		
		Assert.assertFalse(((org.granite.client.persistence.collection.PersistentList<?>)collection).isDirty());
		
		collection = serializeAndDeserializeClientToServer(collection, false);
		Assert.assertTrue(collection instanceof PersistentList);
		Assert.assertTrue(((PersistentList)collection).wasInitialized());
		Assert.assertFalse(((PersistentList)collection).isDirty());
		Assert.assertTrue(((PersistentList)collection).size() == 3);
		
		list = new PersistentList(null, new ArrayList<Integer>());
		list.add(1);
		list.add(2);
		list.add(3);
		list.add(4);
		list.clearDirty();
		
		collection = serializeAndDeserializeServerToServer(list, false);
		Assert.assertTrue(collection instanceof PersistentList);
		Assert.assertTrue(((PersistentList)collection).wasInitialized());
		Assert.assertFalse(((PersistentList)collection).isDirty());
		Assert.assertTrue(((PersistentList)collection).size() == 4);
		
		collection = serializeAndDeserializeServerToClient(list, false);
		Assert.assertTrue(collection instanceof org.granite.client.persistence.collection.PersistentList);
		Assert.assertTrue(((org.granite.client.persistence.collection.PersistentList<?>)collection).wasInitialized());
		Assert.assertTrue(((org.granite.client.persistence.collection.PersistentList<?>)collection).size() == 4);

		Assert.assertFalse(((org.granite.client.persistence.collection.PersistentList<?>)collection).isDirty());
		((org.granite.client.persistence.collection.PersistentList<Object>)collection).clear();
		Assert.assertTrue(((org.granite.client.persistence.collection.PersistentList<?>)collection).isDirty());
		((org.granite.client.persistence.collection.PersistentList<?>)collection).clearDirty();
		
		Assert.assertFalse(((org.granite.client.persistence.collection.PersistentList<?>)collection).isDirty());
		Assert.assertTrue(((org.granite.client.persistence.collection.PersistentList<?>)collection).size() == 0);
		((org.granite.client.persistence.collection.PersistentList<Object>)collection).clear();
		Assert.assertFalse(((org.granite.client.persistence.collection.PersistentList<?>)collection).isDirty());

		Assert.assertFalse(((org.granite.client.persistence.collection.PersistentList<?>)collection).isDirty());
		((org.granite.client.persistence.collection.PersistentList<Object>)collection).add(1);
		Assert.assertTrue(((org.granite.client.persistence.collection.PersistentList<?>)collection).isDirty());
		((org.granite.client.persistence.collection.PersistentList<?>)collection).clearDirty();

		Assert.assertFalse(((org.granite.client.persistence.collection.PersistentList<?>)collection).isDirty());
		((org.granite.client.persistence.collection.PersistentList<Object>)collection).addAll(Arrays.asList(2, 3, 4));
		Assert.assertTrue(((org.granite.client.persistence.collection.PersistentList<?>)collection).isDirty());
		((org.granite.client.persistence.collection.PersistentList<?>)collection).clearDirty();

		Assert.assertFalse(((org.granite.client.persistence.collection.PersistentList<?>)collection).isDirty());
		((org.granite.client.persistence.collection.PersistentList<Object>)collection).add(0, 0);
		Assert.assertTrue(((org.granite.client.persistence.collection.PersistentList<?>)collection).isDirty());
		((org.granite.client.persistence.collection.PersistentList<?>)collection).clearDirty();

		Assert.assertFalse(((org.granite.client.persistence.collection.PersistentList<?>)collection).isDirty());
		((org.granite.client.persistence.collection.PersistentList<Object>)collection).addAll(
			((org.granite.client.persistence.collection.PersistentList<Object>)collection).size(), Arrays.asList(5, 6, 7));
		Assert.assertTrue(((org.granite.client.persistence.collection.PersistentList<?>)collection).isDirty());
		((org.granite.client.persistence.collection.PersistentList<?>)collection).clearDirty();

		Assert.assertFalse(((org.granite.client.persistence.collection.PersistentList<?>)collection).isDirty());
		((org.granite.client.persistence.collection.PersistentList<Object>)collection).remove(0);
		Assert.assertTrue(((org.granite.client.persistence.collection.PersistentList<?>)collection).isDirty());
		((org.granite.client.persistence.collection.PersistentList<?>)collection).clearDirty();

		Assert.assertFalse(((org.granite.client.persistence.collection.PersistentList<?>)collection).isDirty());
		((org.granite.client.persistence.collection.PersistentList<Object>)collection).remove((Object)7);
		Assert.assertTrue(((org.granite.client.persistence.collection.PersistentList<?>)collection).isDirty());
		((org.granite.client.persistence.collection.PersistentList<?>)collection).clearDirty();

		Assert.assertFalse(((org.granite.client.persistence.collection.PersistentList<?>)collection).isDirty());
		((org.granite.client.persistence.collection.PersistentList<Object>)collection).removeAll(Arrays.asList(5, 6));
		Assert.assertTrue(((org.granite.client.persistence.collection.PersistentList<?>)collection).isDirty());
		((org.granite.client.persistence.collection.PersistentList<?>)collection).clearDirty();

		Assert.assertFalse(((org.granite.client.persistence.collection.PersistentList<?>)collection).isDirty());
		((org.granite.client.persistence.collection.PersistentList<Object>)collection).retainAll(Arrays.asList(1, 2, 4));
		Assert.assertTrue(((org.granite.client.persistence.collection.PersistentList<?>)collection).isDirty());
		((org.granite.client.persistence.collection.PersistentList<?>)collection).clearDirty();

		Assert.assertFalse(((org.granite.client.persistence.collection.PersistentList<?>)collection).isDirty());
		((org.granite.client.persistence.collection.PersistentList<Object>)collection).set(2, 3);
		Assert.assertTrue(((org.granite.client.persistence.collection.PersistentList<?>)collection).isDirty());
		((org.granite.client.persistence.collection.PersistentList<?>)collection).clearDirty();
		
		Assert.assertFalse(((org.granite.client.persistence.collection.PersistentList<?>)collection).isDirty());
		lit = ((org.granite.client.persistence.collection.PersistentList<Object>)collection).listIterator(1);
		lit.previous();
		lit.set(0);
		Assert.assertTrue(((org.granite.client.persistence.collection.PersistentList<?>)collection).isDirty());
		((org.granite.client.persistence.collection.PersistentList<?>)collection).clearDirty();
		
		Assert.assertFalse(((org.granite.client.persistence.collection.PersistentList<?>)collection).isDirty());
		lit = ((org.granite.client.persistence.collection.PersistentList<Object>)collection).listIterator(1);
		lit.next();
		lit.set(1);
		lit.next();
		lit.set(2);
		Assert.assertTrue(((org.granite.client.persistence.collection.PersistentList<?>)collection).isDirty());
		((org.granite.client.persistence.collection.PersistentList<?>)collection).clearDirty();
		
		Assert.assertFalse(((org.granite.client.persistence.collection.PersistentList<?>)collection).isDirty());
		lit = ((org.granite.client.persistence.collection.PersistentList<Object>)collection).listIterator();
		lit.next();
		lit.remove();
		Assert.assertTrue(((org.granite.client.persistence.collection.PersistentList<?>)collection).isDirty());
		((org.granite.client.persistence.collection.PersistentList<?>)collection).clearDirty();
		
		Assert.assertFalse(((org.granite.client.persistence.collection.PersistentList<?>)collection).isDirty());
		lit = ((org.granite.client.persistence.collection.PersistentList<Object>)collection).listIterator(1);
		lit.next();
		lit.add(3);
		Assert.assertTrue(((org.granite.client.persistence.collection.PersistentList<?>)collection).isDirty());
		((org.granite.client.persistence.collection.PersistentList<?>)collection).clearDirty();
		
		Assert.assertFalse(((org.granite.client.persistence.collection.PersistentList<?>)collection).isDirty());
		Iterator<Object> it = ((org.granite.client.persistence.collection.PersistentList<Object>)collection).iterator();
		it.next();
		it.next();
		it.next();
		it.remove();
		Assert.assertTrue(((org.granite.client.persistence.collection.PersistentList<?>)collection).isDirty());
		((org.granite.client.persistence.collection.PersistentList<?>)collection).clearDirty();
		
		((org.granite.client.persistence.collection.PersistentList<?>)collection).dirty();
		
		collection = serializeAndDeserializeClientToServer(collection, false);
		Assert.assertTrue(collection instanceof PersistentList);
		Assert.assertTrue(((PersistentList)collection).wasInitialized());
		Assert.assertTrue(((PersistentList)collection).isDirty());
		Assert.assertTrue(((PersistentList)collection).size() == 2);
		Assert.assertTrue(((PersistentList)collection).get(0).equals(1));
		Assert.assertTrue(((PersistentList)collection).get(1).equals(2));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testPersistentSet() throws ClassNotFoundException, IOException {
		PersistentSet set = new PersistentSet(null);
		
		Object collection = serializeAndDeserializeServerToServer(set, false);
		Assert.assertTrue(collection instanceof PersistentSet);
		Assert.assertFalse(((PersistentSet)collection).wasInitialized());
		try {
			((PersistentSet)collection).add(new Object());
			Assert.fail("Should throw a org.hibernate.LazyInitializationException");
		}
		catch (LazyInitializationException e) {
		}
		
		collection = serializeAndDeserializeServerToClient(set, false);
		Assert.assertTrue(collection instanceof org.granite.client.persistence.collection.PersistentSet);
		Assert.assertFalse(((org.granite.client.persistence.collection.PersistentSet<?>)collection).wasInitialized());
		try {
			((org.granite.client.persistence.collection.PersistentSet<Object>)collection).add(new Object());
			Assert.fail("Should throw a org.granite.messaging.jmf.persistence.LazyInitializationException");
		}
		catch (org.granite.client.persistence.LazyInitializationException e) {
		}
		
		collection = serializeAndDeserializeClientToServer(collection, false);
		Assert.assertTrue(collection instanceof PersistentSet);
		Assert.assertFalse(((PersistentSet)collection).wasInitialized());
		try {
			((PersistentSet)collection).add(new Object());
			Assert.fail("Should throw a org.hibernate.LazyInitializationException");
		}
		catch (LazyInitializationException e) {
		}
		
		set = new PersistentSet(null, new HashSet<Object>());
		
		collection = serializeAndDeserializeServerToServer(set, false);
		Assert.assertTrue(collection instanceof PersistentSet);
		Assert.assertTrue(((PersistentSet)collection).wasInitialized());
		Assert.assertFalse(((PersistentSet)collection).isDirty());
		Assert.assertTrue(((PersistentSet)collection).isEmpty());
		
		collection = serializeAndDeserializeServerToClient(set, false);
		Assert.assertTrue(collection instanceof org.granite.client.persistence.collection.PersistentSet);
		Assert.assertTrue(((org.granite.client.persistence.collection.PersistentSet<?>)collection).wasInitialized());
		Assert.assertFalse(((org.granite.client.persistence.collection.PersistentSet<?>)collection).isDirty());
		Assert.assertTrue(((org.granite.client.persistence.collection.PersistentSet<?>)collection).isEmpty());
		
		collection = serializeAndDeserializeClientToServer(collection, false);
		Assert.assertTrue(collection instanceof PersistentSet);
		Assert.assertTrue(((PersistentSet)collection).wasInitialized());
		Assert.assertFalse(((PersistentSet)collection).isDirty());
		Assert.assertTrue(((PersistentSet)collection).isEmpty());
		
		set = new PersistentSet(null, new HashSet<Integer>(Arrays.asList(1, 2, 3)));
		
		collection = serializeAndDeserializeServerToServer(set, false);
		Assert.assertTrue(collection instanceof PersistentSet);
		Assert.assertTrue(((PersistentSet)collection).wasInitialized());
		Assert.assertFalse(((PersistentSet)collection).isDirty());
		Assert.assertFalse(((PersistentSet)collection).isEmpty());
		Assert.assertTrue(((PersistentSet)collection).size() == 3);
		
		collection = serializeAndDeserializeServerToClient(set, false);
		Assert.assertTrue(collection instanceof org.granite.client.persistence.collection.PersistentSet);
		Assert.assertTrue(((org.granite.client.persistence.collection.PersistentSet<?>)collection).wasInitialized());
		Assert.assertFalse(((org.granite.client.persistence.collection.PersistentSet<?>)collection).isDirty());
		Assert.assertFalse(((org.granite.client.persistence.collection.PersistentSet<?>)collection).isEmpty());
		Assert.assertTrue(((org.granite.client.persistence.collection.PersistentSet<?>)collection).size() == 3);
		
		collection = serializeAndDeserializeClientToServer(collection, false);
		Assert.assertTrue(collection instanceof PersistentSet);
		Assert.assertTrue(((PersistentSet)collection).wasInitialized());
		Assert.assertFalse(((PersistentSet)collection).isDirty());
		Assert.assertFalse(((PersistentSet)collection).isEmpty());
		Assert.assertTrue(((PersistentSet)collection).size() == 3);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testPersistentBag() throws ClassNotFoundException, IOException {
		PersistentBag bag = new PersistentBag(null);
		
		Object collection = serializeAndDeserializeServerToServer(bag, false);
		Assert.assertTrue(collection instanceof PersistentBag);
		Assert.assertFalse(((PersistentBag)collection).wasInitialized());
		try {
			((PersistentBag)collection).add(new Object());
			Assert.fail("Should throw a org.hibernate.LazyInitializationException");
		}
		catch (LazyInitializationException e) {
		}
		
		collection = serializeAndDeserializeServerToClient(bag, false);
		Assert.assertTrue(collection instanceof org.granite.client.persistence.collection.PersistentBag);
		Assert.assertFalse(((org.granite.client.persistence.collection.PersistentBag<?>)collection).wasInitialized());
		try {
			((org.granite.client.persistence.collection.PersistentBag<Object>)collection).add(new Object());
			Assert.fail("Should throw a org.granite.messaging.jmf.persistence.LazyInitializationException");
		}
		catch (org.granite.client.persistence.LazyInitializationException e) {
		}
		
		collection = serializeAndDeserializeClientToServer(collection, false);
		Assert.assertTrue(collection instanceof PersistentBag);
		Assert.assertFalse(((PersistentBag)collection).wasInitialized());
		try {
			((PersistentBag)collection).add(new Object());
			Assert.fail("Should throw a org.hibernate.LazyInitializationException");
		}
		catch (LazyInitializationException e) {
		}
		
		bag = new PersistentBag(null, new HashSet<Object>());
		
		collection = serializeAndDeserializeServerToServer(bag, false);
		Assert.assertTrue(collection instanceof PersistentBag);
		Assert.assertTrue(((PersistentBag)collection).wasInitialized());
		Assert.assertFalse(((PersistentBag)collection).isDirty());
		Assert.assertTrue(((PersistentBag)collection).isEmpty());
		
		collection = serializeAndDeserializeServerToClient(bag, false);
		Assert.assertTrue(collection instanceof org.granite.client.persistence.collection.PersistentBag);
		Assert.assertTrue(((org.granite.client.persistence.collection.PersistentBag<?>)collection).wasInitialized());
		Assert.assertFalse(((org.granite.client.persistence.collection.PersistentBag<?>)collection).isDirty());
		Assert.assertTrue(((org.granite.client.persistence.collection.PersistentBag<?>)collection).isEmpty());
		
		collection = serializeAndDeserializeClientToServer(collection, false);
		Assert.assertTrue(collection instanceof PersistentBag);
		Assert.assertTrue(((PersistentBag)collection).wasInitialized());
		Assert.assertFalse(((PersistentBag)collection).isDirty());
		Assert.assertTrue(((PersistentBag)collection).isEmpty());
		
		bag = new PersistentBag(null, new HashSet<Integer>(Arrays.asList(1, 2, 3)));
		
		collection = serializeAndDeserializeServerToServer(bag, false);
		Assert.assertTrue(collection instanceof PersistentBag);
		Assert.assertTrue(((PersistentBag)collection).wasInitialized());
		Assert.assertFalse(((PersistentBag)collection).isDirty());
		Assert.assertFalse(((PersistentBag)collection).isEmpty());
		Assert.assertTrue(((PersistentBag)collection).size() == 3);
		
		collection = serializeAndDeserializeServerToClient(bag, false);
		Assert.assertTrue(collection instanceof org.granite.client.persistence.collection.PersistentBag);
		Assert.assertTrue(((org.granite.client.persistence.collection.PersistentBag<?>)collection).wasInitialized());
		Assert.assertFalse(((org.granite.client.persistence.collection.PersistentBag<?>)collection).isDirty());
		Assert.assertFalse(((org.granite.client.persistence.collection.PersistentBag<?>)collection).isEmpty());
		Assert.assertTrue(((org.granite.client.persistence.collection.PersistentBag<?>)collection).size() == 3);
		
		collection = serializeAndDeserializeClientToServer(collection, false);
		Assert.assertTrue(collection instanceof PersistentBag);
		Assert.assertTrue(((PersistentBag)collection).wasInitialized());
		Assert.assertFalse(((PersistentBag)collection).isDirty());
		Assert.assertFalse(((PersistentBag)collection).isEmpty());
		Assert.assertTrue(((PersistentBag)collection).size() == 3);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testPersistentMap() throws ClassNotFoundException, IOException {
		PersistentMap map = new PersistentMap(null);
		
		Object collection = serializeAndDeserializeServerToServer(map, false);
		Assert.assertTrue(collection instanceof PersistentMap);
		Assert.assertFalse(((PersistentMap)collection).wasInitialized());
		try {
			((PersistentMap)collection).put(new Object(), new Object());
			Assert.fail("Should throw a org.hibernate.LazyInitializationException");
		}
		catch (LazyInitializationException e) {
		}
		
		collection = serializeAndDeserializeServerToClient(map, false);
		Assert.assertTrue(collection instanceof org.granite.client.persistence.collection.PersistentMap);
		Assert.assertFalse(((org.granite.client.persistence.collection.PersistentMap<?, ?>)collection).wasInitialized());
		try {
			((org.granite.client.persistence.collection.PersistentMap<Object, Object>)collection).put(new Object(), new Object());
			Assert.fail("Should throw a org.granite.messaging.jmf.persistence.LazyInitializationException");
		}
		catch (org.granite.client.persistence.LazyInitializationException e) {
		}
		
		collection = serializeAndDeserializeClientToServer(collection, false);
		Assert.assertTrue(collection instanceof PersistentMap);
		Assert.assertFalse(((PersistentMap)collection).wasInitialized());
		try {
			((PersistentMap)collection).put(new Object(), new Object());
			Assert.fail("Should throw a org.hibernate.LazyInitializationException");
		}
		catch (LazyInitializationException e) {
		}
		
		map = new PersistentMap(null, new HashMap<Object, Object>());
		
		collection = serializeAndDeserializeServerToServer(map, false);
		Assert.assertTrue(collection instanceof PersistentMap);
		Assert.assertTrue(((PersistentMap)collection).wasInitialized());
		Assert.assertFalse(((PersistentMap)collection).isDirty());
		Assert.assertTrue(((PersistentMap)collection).isEmpty());
		
		collection = serializeAndDeserializeServerToClient(map, false);
		Assert.assertTrue(collection instanceof org.granite.client.persistence.collection.PersistentMap);
		Assert.assertTrue(((org.granite.client.persistence.collection.PersistentMap<?, ?>)collection).wasInitialized());
		Assert.assertFalse(((org.granite.client.persistence.collection.PersistentMap<?, ?>)collection).isDirty());
		Assert.assertTrue(((org.granite.client.persistence.collection.PersistentMap<?, ?>)collection).isEmpty());
		
		collection = serializeAndDeserializeClientToServer(collection, false);
		Assert.assertTrue(collection instanceof PersistentMap);
		Assert.assertTrue(((PersistentMap)collection).wasInitialized());
		Assert.assertFalse(((PersistentMap)collection).isDirty());
		Assert.assertTrue(((PersistentMap)collection).isEmpty());
		
		Map<Integer, Boolean> content = new HashMap<Integer, Boolean>();
		content.put(1, true);
		content.put(2, false);
		content.put(3, null);
		map = new PersistentMap(null, content);
		
		collection = serializeAndDeserializeServerToServer(map, false);
		Assert.assertTrue(collection instanceof PersistentMap);
		Assert.assertTrue(((PersistentMap)collection).wasInitialized());
		Assert.assertFalse(((PersistentMap)collection).isDirty());
		Assert.assertFalse(((PersistentMap)collection).isEmpty());
		Assert.assertTrue(((PersistentMap)collection).size() == 3);
		
		collection = serializeAndDeserializeServerToClient(map, false);
		Assert.assertTrue(collection instanceof org.granite.client.persistence.collection.PersistentMap);
		Assert.assertTrue(((org.granite.client.persistence.collection.PersistentMap<?, ?>)collection).wasInitialized());
		Assert.assertFalse(((org.granite.client.persistence.collection.PersistentMap<?, ?>)collection).isDirty());
		Assert.assertFalse(((org.granite.client.persistence.collection.PersistentMap<?, ?>)collection).isEmpty());
		Assert.assertTrue(((org.granite.client.persistence.collection.PersistentMap<?, ?>)collection).size() == 3);
		
		collection = serializeAndDeserializeClientToServer(collection, false);
		Assert.assertTrue(collection instanceof PersistentMap);
		Assert.assertTrue(((PersistentMap)collection).wasInitialized());
		Assert.assertFalse(((PersistentMap)collection).isDirty());
		Assert.assertFalse(((PersistentMap)collection).isEmpty());
		Assert.assertTrue(((PersistentMap)collection).size() == 3);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testPersistentSortedSet() throws ClassNotFoundException, IOException {
		PersistentSortedSet sortedSet = new PersistentSortedSet(null);
		
		Object collection = serializeAndDeserializeServerToServer(sortedSet, false);
		Assert.assertTrue(collection instanceof PersistentSortedSet);
		Assert.assertFalse(((PersistentSortedSet)collection).wasInitialized());
		try {
			((PersistentSortedSet)collection).add(new Object());
			Assert.fail("Should throw a org.hibernate.LazyInitializationException");
		}
		catch (LazyInitializationException e) {
		}
		
		collection = serializeAndDeserializeServerToClient(sortedSet, false);
		Assert.assertTrue(collection instanceof org.granite.client.persistence.collection.PersistentSortedSet);
		Assert.assertFalse(((org.granite.client.persistence.collection.PersistentSortedSet<?>)collection).wasInitialized());
		try {
			((org.granite.client.persistence.collection.PersistentSortedSet<Object>)collection).add(new Object());
			Assert.fail("Should throw a org.granite.messaging.jmf.persistence.LazyInitializationException");
		}
		catch (org.granite.client.persistence.LazyInitializationException e) {
		}
		
		collection = serializeAndDeserializeClientToServer(collection, false);
		Assert.assertTrue(collection instanceof PersistentSortedSet);
		Assert.assertFalse(((PersistentSortedSet)collection).wasInitialized());
		try {
			((PersistentSortedSet)collection).add(new Object());
			Assert.fail("Should throw a org.hibernate.LazyInitializationException");
		}
		catch (LazyInitializationException e) {
		}
		
		sortedSet = new PersistentSortedSet(null, new TreeSet<Object>());
		
		collection = serializeAndDeserializeServerToServer(sortedSet, false);
		Assert.assertTrue(collection instanceof PersistentSortedSet);
		Assert.assertTrue(((PersistentSortedSet)collection).wasInitialized());
		Assert.assertFalse(((PersistentSortedSet)collection).isDirty());
		Assert.assertTrue(((PersistentSortedSet)collection).isEmpty());
		
		collection = serializeAndDeserializeServerToClient(sortedSet, false);
		Assert.assertTrue(collection instanceof org.granite.client.persistence.collection.PersistentSortedSet);
		Assert.assertTrue(((org.granite.client.persistence.collection.PersistentSortedSet<?>)collection).wasInitialized());
		Assert.assertFalse(((org.granite.client.persistence.collection.PersistentSortedSet<?>)collection).isDirty());
		Assert.assertTrue(((org.granite.client.persistence.collection.PersistentSortedSet<?>)collection).isEmpty());
		
		collection = serializeAndDeserializeClientToServer(collection, false);
		Assert.assertTrue(collection instanceof PersistentSortedSet);
		Assert.assertTrue(((PersistentSortedSet)collection).wasInitialized());
		Assert.assertFalse(((PersistentSortedSet)collection).isDirty());
		Assert.assertTrue(((PersistentSortedSet)collection).isEmpty());
		
		sortedSet = new PersistentSortedSet(null, new TreeSet<Integer>(Arrays.asList(1, 2, 3)));
		
		collection = serializeAndDeserializeServerToServer(sortedSet, false);
		Assert.assertTrue(collection instanceof PersistentSortedSet);
		Assert.assertTrue(((PersistentSortedSet)collection).wasInitialized());
		Assert.assertFalse(((PersistentSortedSet)collection).isDirty());
		Assert.assertFalse(((PersistentSortedSet)collection).isEmpty());
		Assert.assertTrue(((PersistentSortedSet)collection).size() == 3);
		
		collection = serializeAndDeserializeServerToClient(sortedSet, false);
		Assert.assertTrue(collection instanceof org.granite.client.persistence.collection.PersistentSortedSet);
		Assert.assertTrue(((org.granite.client.persistence.collection.PersistentSortedSet<?>)collection).wasInitialized());
		Assert.assertFalse(((org.granite.client.persistence.collection.PersistentSortedSet<?>)collection).isDirty());
		Assert.assertFalse(((org.granite.client.persistence.collection.PersistentSortedSet<?>)collection).isEmpty());
		Assert.assertTrue(((org.granite.client.persistence.collection.PersistentSortedSet<?>)collection).size() == 3);
		
		collection = serializeAndDeserializeClientToServer(collection, false);
		Assert.assertTrue(collection instanceof PersistentSortedSet);
		Assert.assertTrue(((PersistentSortedSet)collection).wasInitialized());
		Assert.assertFalse(((PersistentSortedSet)collection).isDirty());
		Assert.assertFalse(((PersistentSortedSet)collection).isEmpty());
		Assert.assertTrue(((PersistentSortedSet)collection).size() == 3);
		
		sortedSet = new PersistentSortedSet(null, new TreeSet<Integer>(new Comparator<Integer>() {
			@Override
			public int compare(Integer o1, Integer o2) {
				return o2.compareTo(o1);
			}
		}));
		sortedSet.addAll(Arrays.asList(1, 2, 3));
		sortedSet.clearDirty();
		
		collection = serializeAndDeserializeServerToServer(sortedSet, false);
		Assert.assertTrue(collection instanceof PersistentSortedSet);
		Assert.assertTrue(((PersistentSortedSet)collection).wasInitialized());
		Assert.assertFalse(((PersistentSortedSet)collection).isDirty());
		Assert.assertFalse(((PersistentSortedSet)collection).isEmpty());
		Assert.assertTrue(((PersistentSortedSet)collection).size() == 3);
		
		collection = serializeAndDeserializeServerToClient(sortedSet, false);
		Assert.assertTrue(collection instanceof org.granite.client.persistence.collection.PersistentSortedSet);
		Assert.assertTrue(((org.granite.client.persistence.collection.PersistentSortedSet<?>)collection).wasInitialized());
		Assert.assertFalse(((org.granite.client.persistence.collection.PersistentSortedSet<?>)collection).isDirty());
		Assert.assertFalse(((org.granite.client.persistence.collection.PersistentSortedSet<?>)collection).isEmpty());
		Assert.assertTrue(((org.granite.client.persistence.collection.PersistentSortedSet<?>)collection).size() == 3);
		
		collection = serializeAndDeserializeClientToServer(collection, false);
		Assert.assertTrue(collection instanceof PersistentSortedSet);
		Assert.assertTrue(((PersistentSortedSet)collection).wasInitialized());
		Assert.assertFalse(((PersistentSortedSet)collection).isDirty());
		Assert.assertFalse(((PersistentSortedSet)collection).isEmpty());
		Assert.assertTrue(((PersistentSortedSet)collection).size() == 3);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testPersistentSortedMap() throws ClassNotFoundException, IOException {
		PersistentSortedMap sortedMap = new PersistentSortedMap(null);
		
		Object collection = serializeAndDeserializeServerToServer(sortedMap, false);
		Assert.assertTrue(collection instanceof PersistentSortedMap);
		Assert.assertFalse(((PersistentSortedMap)collection).wasInitialized());
		try {
			((PersistentSortedMap)collection).put(new Object(), new Object());
			Assert.fail("Should throw a org.hibernate.LazyInitializationException");
		}
		catch (LazyInitializationException e) {
		}
		
		collection = serializeAndDeserializeServerToClient(sortedMap, false);
		Assert.assertTrue(collection instanceof org.granite.client.persistence.collection.PersistentSortedMap);
		Assert.assertFalse(((org.granite.client.persistence.collection.PersistentSortedMap<?, ?>)collection).wasInitialized());
		try {
			((org.granite.client.persistence.collection.PersistentSortedMap<Object, Object>)collection).put(new Object(), new Object());
			Assert.fail("Should throw a org.granite.messaging.jmf.persistence.LazyInitializationException");
		}
		catch (org.granite.client.persistence.LazyInitializationException e) {
		}
		
		collection = serializeAndDeserializeClientToServer(collection, false);
		Assert.assertTrue(collection instanceof PersistentSortedMap);
		Assert.assertFalse(((PersistentSortedMap)collection).wasInitialized());
		try {
			((PersistentSortedMap)collection).put(new Object(), new Object());
			Assert.fail("Should throw a org.hibernate.LazyInitializationException");
		}
		catch (LazyInitializationException e) {
		}
		
		sortedMap = new PersistentSortedMap(null, new TreeMap<Object, Object>());
		
		collection = serializeAndDeserializeServerToServer(sortedMap, false);
		Assert.assertTrue(collection instanceof PersistentSortedMap);
		Assert.assertTrue(((PersistentSortedMap)collection).wasInitialized());
		Assert.assertFalse(((PersistentSortedMap)collection).isDirty());
		Assert.assertTrue(((PersistentSortedMap)collection).isEmpty());
		
		collection = serializeAndDeserializeServerToClient(sortedMap, false);
		Assert.assertTrue(collection instanceof org.granite.client.persistence.collection.PersistentSortedMap);
		Assert.assertTrue(((org.granite.client.persistence.collection.PersistentSortedMap<?, ?>)collection).wasInitialized());
		Assert.assertFalse(((org.granite.client.persistence.collection.PersistentSortedMap<?, ?>)collection).isDirty());
		Assert.assertTrue(((org.granite.client.persistence.collection.PersistentSortedMap<?, ?>)collection).isEmpty());
		
		collection = serializeAndDeserializeClientToServer(collection, false);
		Assert.assertTrue(collection instanceof PersistentSortedMap);
		Assert.assertTrue(((PersistentSortedMap)collection).wasInitialized());
		Assert.assertFalse(((PersistentSortedMap)collection).isDirty());
		Assert.assertTrue(((PersistentSortedMap)collection).isEmpty());
		
		SortedMap<Integer, Boolean> content = new TreeMap<Integer, Boolean>();
		content.put(1, true);
		content.put(2, false);
		content.put(3, null);
		sortedMap = new PersistentSortedMap(null, content);
		
		collection = serializeAndDeserializeServerToServer(sortedMap, false);
		Assert.assertTrue(collection instanceof PersistentSortedMap);
		Assert.assertTrue(((PersistentSortedMap)collection).wasInitialized());
		Assert.assertFalse(((PersistentSortedMap)collection).isDirty());
		Assert.assertFalse(((PersistentSortedMap)collection).isEmpty());
		Assert.assertTrue(((PersistentSortedMap)collection).size() == 3);
		
		collection = serializeAndDeserializeServerToClient(sortedMap, false);
		Assert.assertTrue(collection instanceof org.granite.client.persistence.collection.PersistentSortedMap);
		Assert.assertTrue(((org.granite.client.persistence.collection.PersistentSortedMap<?, ?>)collection).wasInitialized());
		Assert.assertFalse(((org.granite.client.persistence.collection.PersistentSortedMap<?, ?>)collection).isDirty());
		Assert.assertFalse(((org.granite.client.persistence.collection.PersistentSortedMap<?, ?>)collection).isEmpty());
		Assert.assertTrue(((org.granite.client.persistence.collection.PersistentSortedMap<?, ?>)collection).size() == 3);
		
		collection = serializeAndDeserializeClientToServer(collection, false);
		Assert.assertTrue(collection instanceof PersistentSortedMap);
		Assert.assertTrue(((PersistentSortedMap)collection).wasInitialized());
		Assert.assertFalse(((PersistentSortedMap)collection).isDirty());
		Assert.assertFalse(((PersistentSortedMap)collection).isEmpty());
		Assert.assertTrue(((PersistentSortedMap)collection).size() == 3);
		
		sortedMap = new PersistentSortedMap(null, new TreeMap<Integer, Boolean>(new Comparator<Integer>() {
			@Override
			public int compare(Integer o1, Integer o2) {
				return o2.compareTo(o1);
			}
		}));
		sortedMap.put(1, true);
		sortedMap.put(2, false);
		sortedMap.put(3, null);
		sortedMap.clearDirty();
		
		collection = serializeAndDeserializeServerToServer(sortedMap, false);
		Assert.assertTrue(collection instanceof PersistentSortedMap);
		Assert.assertTrue(((PersistentSortedMap)collection).wasInitialized());
		Assert.assertFalse(((PersistentSortedMap)collection).isDirty());
		Assert.assertFalse(((PersistentSortedMap)collection).isEmpty());
		Assert.assertTrue(((PersistentSortedMap)collection).size() == 3);
		
		collection = serializeAndDeserializeServerToClient(sortedMap, false);
		Assert.assertTrue(collection instanceof org.granite.client.persistence.collection.PersistentSortedMap);
		Assert.assertTrue(((org.granite.client.persistence.collection.PersistentSortedMap<?, ?>)collection).wasInitialized());
		Assert.assertFalse(((org.granite.client.persistence.collection.PersistentSortedMap<?, ?>)collection).isDirty());
		Assert.assertFalse(((org.granite.client.persistence.collection.PersistentSortedMap<?, ?>)collection).isEmpty());
		Assert.assertTrue(((org.granite.client.persistence.collection.PersistentSortedMap<?, ?>)collection).size() == 3);
		
		collection = serializeAndDeserializeClientToServer(collection, false);
		Assert.assertTrue(collection instanceof PersistentSortedMap);
		Assert.assertTrue(((PersistentSortedMap)collection).wasInitialized());
		Assert.assertFalse(((PersistentSortedMap)collection).isDirty());
		Assert.assertFalse(((PersistentSortedMap)collection).isEmpty());
		Assert.assertTrue(((PersistentSortedMap)collection).size() == 3);
	}

	@SuppressWarnings("cast")
	@Test
	public void testEntity() throws ClassNotFoundException, IOException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		clientAliasRegistry.registerAlias(ClientEntity.class);
		clientAliasRegistry.registerAlias(ClientCollectionEntity.class);

		final Integer id = Integer.valueOf(3);
		final String uid = UUID.randomUUID().toString();
		final Integer version = Integer.valueOf(2);
		final String name = "John Doe";
		
		ServerEntity serverEntity = new ServerEntity(id, version);
		serverEntity.setUid(uid);
		serverEntity.setName(name);
		serverEntity.getList().add(new ServerCollectionEntity(10, 11));
		
		Persistence persistence = Platform.persistence();
		
		Object clientEntity = serializeAndDeserializeServerToClient(serverEntity, true);
		Assert.assertTrue(clientEntity instanceof ClientEntity);
		Assert.assertTrue(persistence.isInitialized(clientEntity));
		Assert.assertNull(persistence.getDetachedState(clientEntity));
		Assert.assertEquals(id, ((ClientEntity)clientEntity).getId());
		Assert.assertEquals(uid, persistence.getUid(clientEntity));
		Assert.assertEquals(version, persistence.getVersion(clientEntity));
		Assert.assertEquals(name, ((ClientEntity)clientEntity).getName());
		Assert.assertTrue(persistence.isInitialized(((ClientEntity)clientEntity).getList()));
		Assert.assertTrue(((ClientEntity)clientEntity).getList().size() == 1);
		Assert.assertTrue(((ClientEntity)clientEntity).getList().get(0) instanceof ClientCollectionEntity);
		
		Object serverEntityCopy = serializeAndDeserializeClientToServer(clientEntity, true);
		Assert.assertTrue(serverEntityCopy instanceof ServerEntity);
		Assert.assertEquals(id, ((ServerEntity)serverEntityCopy).getId());
		Assert.assertEquals(uid, ((ServerEntity)serverEntityCopy).getUid());
		Assert.assertEquals(version, ((ServerEntity)serverEntityCopy).getVersion());
		Assert.assertEquals(name, ((ServerEntity)serverEntityCopy).getName());
		Assert.assertTrue(((ServerEntity)serverEntityCopy).getList().size() == 1);
		Assert.assertTrue(((ServerEntity)serverEntityCopy).getList().get(0) instanceof ServerCollectionEntity);
	}

	@SuppressWarnings("cast")
	@Test
	public void testFXEntity() throws ClassNotFoundException, IOException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		clientAliasRegistry.registerAlias(ClientFXEntity.class);
		clientAliasRegistry.registerAlias(ClientFXCollectionEntity.class);

		final Integer id = Integer.valueOf(3);
		final String uid = UUID.randomUUID().toString();
		final Integer version = Integer.valueOf(2);
		final String name = "John Doe";
		
		ServerEntity serverEntity = new ServerEntity(id, version);
		serverEntity.setUid(uid);
		serverEntity.setName(name);
		serverEntity.getList().add(new ServerCollectionEntity(10, 11));
		
		Platform javafxPlatform = new JavaFXPlatform();
		Persistence persistence = javafxPlatform.getPersistence();
		
		Object clientEntity = serializeAndDeserializeServerToClient(serverEntity, true);
		Assert.assertTrue(clientEntity instanceof ClientFXEntity);
		Assert.assertTrue(persistence.isInitialized(clientEntity));
		Assert.assertNull(persistence.getDetachedState(clientEntity));
		Assert.assertEquals(id, ((ClientFXEntity)clientEntity).getId());
		Assert.assertEquals(uid, persistence.getUid(clientEntity));
		Assert.assertEquals(version, persistence.getVersion(clientEntity));
		Assert.assertEquals(name, ((ClientFXEntity)clientEntity).getName());
		Assert.assertTrue(persistence.isInitialized(((ClientFXEntity)clientEntity).getList()));
		Assert.assertTrue(((ClientFXEntity)clientEntity).getList().size() == 1);
		Assert.assertTrue(((ClientFXEntity)clientEntity).getList().get(0) instanceof ClientFXCollectionEntity);
		
		Object serverEntityCopy = serializeAndDeserializeClientToServer(clientEntity, true);
		Assert.assertTrue(serverEntityCopy instanceof ServerEntity);
		Assert.assertEquals(id, ((ServerEntity)serverEntityCopy).getId());
		Assert.assertEquals(uid, ((ServerEntity)serverEntityCopy).getUid());
		Assert.assertEquals(version, ((ServerEntity)serverEntityCopy).getVersion());
		Assert.assertEquals(name, ((ServerEntity)serverEntityCopy).getName());
		Assert.assertTrue(((ServerEntity)serverEntityCopy).getList().size() == 1);
		Assert.assertTrue(((ServerEntity)serverEntityCopy).getList().get(0) instanceof ServerCollectionEntity);
	}
	
	private Object serializeAndDeserializeServerToServer(Object obj, boolean dump) throws ClassNotFoundException, IOException {
		return serializeAndDeserialize(serverSharedContext, dumpSharedContext, serverSharedContext, obj, dump);
	}
	
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
