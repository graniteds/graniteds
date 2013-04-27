package org.granite.test.jmf;

import java.io.IOException;
import java.io.PrintStream;
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
import org.granite.test.jmf.TestUtil.ByteArrayJMFDeserializer;
import org.granite.test.jmf.TestUtil.ByteArrayJMFDumper;
import org.granite.test.jmf.TestUtil.ByteArrayJMFSerializer;
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
	private SharedContext clientSharedContext;
	
	@Before
	public void before() {
		
		List<ExtendedObjectCodec> extendedObjectCodecs = Arrays.asList((ExtendedObjectCodec)
			new PersistentListCodec(),
			new PersistentSetCodec(),
			new PersistentBagCodec(),
			new PersistentMapCodec(),
			new PersistentSortedSetCodec(),
			new PersistentSortedMapCodec()
		);
		
		dumpSharedContext = new DefaultSharedContext(new DefaultCodecRegistry());
		serverSharedContext = new DefaultSharedContext(new DefaultCodecRegistry(extendedObjectCodecs));
		clientSharedContext = new DefaultSharedContext(new DefaultCodecRegistry());
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
		
		collection = serializeAndDeserializeServerToClient(list, false);
		Assert.assertTrue(collection instanceof org.granite.messaging.jmf.persistence.PersistentList);
		Assert.assertFalse(((org.granite.messaging.jmf.persistence.PersistentList<?>)collection).wasInitialized());
		try {
			((org.granite.messaging.jmf.persistence.PersistentList<Object>)collection).add(new Object());
			Assert.fail("Should throw a org.granite.messaging.jmf.persistence.LazyInitializationException");
		}
		catch (org.granite.messaging.jmf.persistence.LazyInitializationException e) {
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
		Assert.assertTrue(collection instanceof org.granite.messaging.jmf.persistence.PersistentList);
		Assert.assertTrue(((org.granite.messaging.jmf.persistence.PersistentList<?>)collection).wasInitialized());
		Assert.assertFalse(((org.granite.messaging.jmf.persistence.PersistentList<?>)collection).isDirty());
		Assert.assertTrue(((org.granite.messaging.jmf.persistence.PersistentList<?>)collection).isEmpty());
		
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
		Assert.assertTrue(collection instanceof org.granite.messaging.jmf.persistence.PersistentList);
		Assert.assertTrue(((org.granite.messaging.jmf.persistence.PersistentList<?>)collection).wasInitialized());
		Assert.assertFalse(((org.granite.messaging.jmf.persistence.PersistentList<?>)collection).isDirty());
		Assert.assertFalse(((org.granite.messaging.jmf.persistence.PersistentList<?>)collection).isEmpty());
		Assert.assertTrue(((org.granite.messaging.jmf.persistence.PersistentList<?>)collection).size() == 3);
		Assert.assertTrue(((org.granite.messaging.jmf.persistence.PersistentList<?>)collection).contains(1));
		Assert.assertTrue(((org.granite.messaging.jmf.persistence.PersistentList<?>)collection).contains(2));
		Assert.assertTrue(((org.granite.messaging.jmf.persistence.PersistentList<?>)collection).contains(3));
		Assert.assertTrue(((org.granite.messaging.jmf.persistence.PersistentList<?>)collection).indexOf(1) == 0);
		Assert.assertTrue(((org.granite.messaging.jmf.persistence.PersistentList<?>)collection).indexOf(2) == 1);
		Assert.assertTrue(((org.granite.messaging.jmf.persistence.PersistentList<?>)collection).indexOf(3) == 2);
		Assert.assertTrue(((org.granite.messaging.jmf.persistence.PersistentList<?>)collection).containsAll(Arrays.asList(2, 1, 3)));
		Assert.assertTrue(((org.granite.messaging.jmf.persistence.PersistentList<?>)collection).get(0).equals(1));
		Assert.assertTrue(((org.granite.messaging.jmf.persistence.PersistentList<?>)collection).get(1).equals(2));
		Assert.assertTrue(((org.granite.messaging.jmf.persistence.PersistentList<?>)collection).get(2).equals(3));
		Assert.assertTrue(((org.granite.messaging.jmf.persistence.PersistentList<?>)collection).equals(collection));
		Assert.assertFalse(((org.granite.messaging.jmf.persistence.PersistentList<?>)collection).retainAll(Arrays.asList(3, 2, 1)));
		Assert.assertFalse(((org.granite.messaging.jmf.persistence.PersistentList<Object>)collection).remove((Object)4));
		Assert.assertFalse(((org.granite.messaging.jmf.persistence.PersistentList<Object>)collection).removeAll(Arrays.asList(4, 5)));
		Assert.assertTrue(((org.granite.messaging.jmf.persistence.PersistentList<?>)collection).toString() != null);
		Assert.assertTrue(((org.granite.messaging.jmf.persistence.PersistentList<?>)collection).hashCode() != 0);
		Assert.assertTrue(((org.granite.messaging.jmf.persistence.PersistentList<?>)collection).iterator().next().equals(1));
		Assert.assertTrue(((org.granite.messaging.jmf.persistence.PersistentList<?>)collection).listIterator().next().equals(1));
		Assert.assertTrue(((org.granite.messaging.jmf.persistence.PersistentList<?>)collection).listIterator(1).next().equals(2));
		Assert.assertTrue(((org.granite.messaging.jmf.persistence.PersistentList<?>)collection).listIterator(2).next().equals(3));
		ListIterator<Object> lit = ((org.granite.messaging.jmf.persistence.PersistentList<Object>)collection).listIterator(1);
		lit.previous();
		lit.set(1);
		lit.next();
		lit.next();
		lit.set(2);
		lit.next();
		lit.set(3);
		Assert.assertTrue(((org.granite.messaging.jmf.persistence.PersistentList<?>)collection).lastIndexOf(1) == 0);
		Assert.assertTrue(((org.granite.messaging.jmf.persistence.PersistentList<?>)collection).lastIndexOf(2) == 1);
		Assert.assertTrue(((org.granite.messaging.jmf.persistence.PersistentList<?>)collection).lastIndexOf(3) == 2);
		Assert.assertTrue(((org.granite.messaging.jmf.persistence.PersistentList<Object>)collection).set(0, 1).equals(1));
		Assert.assertTrue(((org.granite.messaging.jmf.persistence.PersistentList<Object>)collection).set(1, 2).equals(2));
		Assert.assertTrue(((org.granite.messaging.jmf.persistence.PersistentList<Object>)collection).set(2, 3).equals(3));
		Assert.assertTrue(((org.granite.messaging.jmf.persistence.PersistentList<Object>)collection).subList(1, 2).equals(Arrays.asList(2)));
		
		Assert.assertFalse(((org.granite.messaging.jmf.persistence.PersistentList<?>)collection).isDirty());
		
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
		Assert.assertTrue(collection instanceof org.granite.messaging.jmf.persistence.PersistentList);
		Assert.assertTrue(((org.granite.messaging.jmf.persistence.PersistentList<?>)collection).wasInitialized());
		Assert.assertTrue(((org.granite.messaging.jmf.persistence.PersistentList<?>)collection).size() == 4);

		Assert.assertFalse(((org.granite.messaging.jmf.persistence.PersistentList<?>)collection).isDirty());
		((org.granite.messaging.jmf.persistence.PersistentList<Object>)collection).clear();
		Assert.assertTrue(((org.granite.messaging.jmf.persistence.PersistentList<?>)collection).isDirty());
		((org.granite.messaging.jmf.persistence.PersistentList<?>)collection).clearDirty();
		
		Assert.assertFalse(((org.granite.messaging.jmf.persistence.PersistentList<?>)collection).isDirty());
		Assert.assertTrue(((org.granite.messaging.jmf.persistence.PersistentList<?>)collection).size() == 0);
		((org.granite.messaging.jmf.persistence.PersistentList<Object>)collection).clear();
		Assert.assertFalse(((org.granite.messaging.jmf.persistence.PersistentList<?>)collection).isDirty());

		Assert.assertFalse(((org.granite.messaging.jmf.persistence.PersistentList<?>)collection).isDirty());
		((org.granite.messaging.jmf.persistence.PersistentList<Object>)collection).add(1);
		Assert.assertTrue(((org.granite.messaging.jmf.persistence.PersistentList<?>)collection).isDirty());
		((org.granite.messaging.jmf.persistence.PersistentList<?>)collection).clearDirty();

		Assert.assertFalse(((org.granite.messaging.jmf.persistence.PersistentList<?>)collection).isDirty());
		((org.granite.messaging.jmf.persistence.PersistentList<Object>)collection).addAll(Arrays.asList(2, 3, 4));
		Assert.assertTrue(((org.granite.messaging.jmf.persistence.PersistentList<?>)collection).isDirty());
		((org.granite.messaging.jmf.persistence.PersistentList<?>)collection).clearDirty();

		Assert.assertFalse(((org.granite.messaging.jmf.persistence.PersistentList<?>)collection).isDirty());
		((org.granite.messaging.jmf.persistence.PersistentList<Object>)collection).add(0, 0);
		Assert.assertTrue(((org.granite.messaging.jmf.persistence.PersistentList<?>)collection).isDirty());
		((org.granite.messaging.jmf.persistence.PersistentList<?>)collection).clearDirty();

		Assert.assertFalse(((org.granite.messaging.jmf.persistence.PersistentList<?>)collection).isDirty());
		((org.granite.messaging.jmf.persistence.PersistentList<Object>)collection).addAll(
			((org.granite.messaging.jmf.persistence.PersistentList<Object>)collection).size(), Arrays.asList(5, 6, 7));
		Assert.assertTrue(((org.granite.messaging.jmf.persistence.PersistentList<?>)collection).isDirty());
		((org.granite.messaging.jmf.persistence.PersistentList<?>)collection).clearDirty();

		Assert.assertFalse(((org.granite.messaging.jmf.persistence.PersistentList<?>)collection).isDirty());
		((org.granite.messaging.jmf.persistence.PersistentList<Object>)collection).remove(0);
		Assert.assertTrue(((org.granite.messaging.jmf.persistence.PersistentList<?>)collection).isDirty());
		((org.granite.messaging.jmf.persistence.PersistentList<?>)collection).clearDirty();

		Assert.assertFalse(((org.granite.messaging.jmf.persistence.PersistentList<?>)collection).isDirty());
		((org.granite.messaging.jmf.persistence.PersistentList<Object>)collection).remove((Object)7);
		Assert.assertTrue(((org.granite.messaging.jmf.persistence.PersistentList<?>)collection).isDirty());
		((org.granite.messaging.jmf.persistence.PersistentList<?>)collection).clearDirty();

		Assert.assertFalse(((org.granite.messaging.jmf.persistence.PersistentList<?>)collection).isDirty());
		((org.granite.messaging.jmf.persistence.PersistentList<Object>)collection).removeAll(Arrays.asList(5, 6));
		Assert.assertTrue(((org.granite.messaging.jmf.persistence.PersistentList<?>)collection).isDirty());
		((org.granite.messaging.jmf.persistence.PersistentList<?>)collection).clearDirty();

		Assert.assertFalse(((org.granite.messaging.jmf.persistence.PersistentList<?>)collection).isDirty());
		((org.granite.messaging.jmf.persistence.PersistentList<Object>)collection).retainAll(Arrays.asList(1, 2, 4));
		Assert.assertTrue(((org.granite.messaging.jmf.persistence.PersistentList<?>)collection).isDirty());
		((org.granite.messaging.jmf.persistence.PersistentList<?>)collection).clearDirty();

		Assert.assertFalse(((org.granite.messaging.jmf.persistence.PersistentList<?>)collection).isDirty());
		((org.granite.messaging.jmf.persistence.PersistentList<Object>)collection).set(2, 3);
		Assert.assertTrue(((org.granite.messaging.jmf.persistence.PersistentList<?>)collection).isDirty());
		((org.granite.messaging.jmf.persistence.PersistentList<?>)collection).clearDirty();
		
		Assert.assertFalse(((org.granite.messaging.jmf.persistence.PersistentList<?>)collection).isDirty());
		lit = ((org.granite.messaging.jmf.persistence.PersistentList<Object>)collection).listIterator(1);
		lit.previous();
		lit.set(0);
		Assert.assertTrue(((org.granite.messaging.jmf.persistence.PersistentList<?>)collection).isDirty());
		((org.granite.messaging.jmf.persistence.PersistentList<?>)collection).clearDirty();
		
		Assert.assertFalse(((org.granite.messaging.jmf.persistence.PersistentList<?>)collection).isDirty());
		lit = ((org.granite.messaging.jmf.persistence.PersistentList<Object>)collection).listIterator(1);
		lit.next();
		lit.set(1);
		lit.next();
		lit.set(2);
		Assert.assertTrue(((org.granite.messaging.jmf.persistence.PersistentList<?>)collection).isDirty());
		((org.granite.messaging.jmf.persistence.PersistentList<?>)collection).clearDirty();
		
		Assert.assertFalse(((org.granite.messaging.jmf.persistence.PersistentList<?>)collection).isDirty());
		lit = ((org.granite.messaging.jmf.persistence.PersistentList<Object>)collection).listIterator();
		lit.next();
		lit.remove();
		Assert.assertTrue(((org.granite.messaging.jmf.persistence.PersistentList<?>)collection).isDirty());
		((org.granite.messaging.jmf.persistence.PersistentList<?>)collection).clearDirty();
		
		Assert.assertFalse(((org.granite.messaging.jmf.persistence.PersistentList<?>)collection).isDirty());
		lit = ((org.granite.messaging.jmf.persistence.PersistentList<Object>)collection).listIterator(1);
		lit.next();
		lit.add(3);
		Assert.assertTrue(((org.granite.messaging.jmf.persistence.PersistentList<?>)collection).isDirty());
		((org.granite.messaging.jmf.persistence.PersistentList<?>)collection).clearDirty();
		
		Assert.assertFalse(((org.granite.messaging.jmf.persistence.PersistentList<?>)collection).isDirty());
		Iterator<Object> it = ((org.granite.messaging.jmf.persistence.PersistentList<Object>)collection).iterator();
		it.next();
		it.next();
		it.next();
		it.remove();
		Assert.assertTrue(((org.granite.messaging.jmf.persistence.PersistentList<?>)collection).isDirty());
		((org.granite.messaging.jmf.persistence.PersistentList<?>)collection).clearDirty();
		
		((org.granite.messaging.jmf.persistence.PersistentList<?>)collection).dirty();
		
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
		Assert.assertTrue(collection instanceof org.granite.messaging.jmf.persistence.PersistentSet);
		Assert.assertFalse(((org.granite.messaging.jmf.persistence.PersistentSet<?>)collection).wasInitialized());
		try {
			((org.granite.messaging.jmf.persistence.PersistentSet<Object>)collection).add(new Object());
			Assert.fail("Should throw a org.granite.messaging.jmf.persistence.LazyInitializationException");
		}
		catch (org.granite.messaging.jmf.persistence.LazyInitializationException e) {
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
		Assert.assertTrue(collection instanceof org.granite.messaging.jmf.persistence.PersistentSet);
		Assert.assertTrue(((org.granite.messaging.jmf.persistence.PersistentSet<?>)collection).wasInitialized());
		Assert.assertFalse(((org.granite.messaging.jmf.persistence.PersistentSet<?>)collection).isDirty());
		Assert.assertTrue(((org.granite.messaging.jmf.persistence.PersistentSet<?>)collection).isEmpty());
		
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
		Assert.assertTrue(collection instanceof org.granite.messaging.jmf.persistence.PersistentSet);
		Assert.assertTrue(((org.granite.messaging.jmf.persistence.PersistentSet<?>)collection).wasInitialized());
		Assert.assertFalse(((org.granite.messaging.jmf.persistence.PersistentSet<?>)collection).isDirty());
		Assert.assertFalse(((org.granite.messaging.jmf.persistence.PersistentSet<?>)collection).isEmpty());
		Assert.assertTrue(((org.granite.messaging.jmf.persistence.PersistentSet<?>)collection).size() == 3);
		
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
		Assert.assertTrue(collection instanceof org.granite.messaging.jmf.persistence.PersistentBag);
		Assert.assertFalse(((org.granite.messaging.jmf.persistence.PersistentBag<?>)collection).wasInitialized());
		try {
			((org.granite.messaging.jmf.persistence.PersistentBag<Object>)collection).add(new Object());
			Assert.fail("Should throw a org.granite.messaging.jmf.persistence.LazyInitializationException");
		}
		catch (org.granite.messaging.jmf.persistence.LazyInitializationException e) {
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
		Assert.assertTrue(collection instanceof org.granite.messaging.jmf.persistence.PersistentBag);
		Assert.assertTrue(((org.granite.messaging.jmf.persistence.PersistentBag<?>)collection).wasInitialized());
		Assert.assertFalse(((org.granite.messaging.jmf.persistence.PersistentBag<?>)collection).isDirty());
		Assert.assertTrue(((org.granite.messaging.jmf.persistence.PersistentBag<?>)collection).isEmpty());
		
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
		Assert.assertTrue(collection instanceof org.granite.messaging.jmf.persistence.PersistentBag);
		Assert.assertTrue(((org.granite.messaging.jmf.persistence.PersistentBag<?>)collection).wasInitialized());
		Assert.assertFalse(((org.granite.messaging.jmf.persistence.PersistentBag<?>)collection).isDirty());
		Assert.assertFalse(((org.granite.messaging.jmf.persistence.PersistentBag<?>)collection).isEmpty());
		Assert.assertTrue(((org.granite.messaging.jmf.persistence.PersistentBag<?>)collection).size() == 3);
		
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
		Assert.assertTrue(collection instanceof org.granite.messaging.jmf.persistence.PersistentMap);
		Assert.assertFalse(((org.granite.messaging.jmf.persistence.PersistentMap<?, ?>)collection).wasInitialized());
		try {
			((org.granite.messaging.jmf.persistence.PersistentMap<Object, Object>)collection).put(new Object(), new Object());
			Assert.fail("Should throw a org.granite.messaging.jmf.persistence.LazyInitializationException");
		}
		catch (org.granite.messaging.jmf.persistence.LazyInitializationException e) {
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
		Assert.assertTrue(collection instanceof org.granite.messaging.jmf.persistence.PersistentMap);
		Assert.assertTrue(((org.granite.messaging.jmf.persistence.PersistentMap<?, ?>)collection).wasInitialized());
		Assert.assertFalse(((org.granite.messaging.jmf.persistence.PersistentMap<?, ?>)collection).isDirty());
		Assert.assertTrue(((org.granite.messaging.jmf.persistence.PersistentMap<?, ?>)collection).isEmpty());
		
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
		Assert.assertTrue(collection instanceof org.granite.messaging.jmf.persistence.PersistentMap);
		Assert.assertTrue(((org.granite.messaging.jmf.persistence.PersistentMap<?, ?>)collection).wasInitialized());
		Assert.assertFalse(((org.granite.messaging.jmf.persistence.PersistentMap<?, ?>)collection).isDirty());
		Assert.assertFalse(((org.granite.messaging.jmf.persistence.PersistentMap<?, ?>)collection).isEmpty());
		Assert.assertTrue(((org.granite.messaging.jmf.persistence.PersistentMap<?, ?>)collection).size() == 3);
		
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
		Assert.assertTrue(collection instanceof org.granite.messaging.jmf.persistence.PersistentSortedSet);
		Assert.assertFalse(((org.granite.messaging.jmf.persistence.PersistentSortedSet<?>)collection).wasInitialized());
		try {
			((org.granite.messaging.jmf.persistence.PersistentSortedSet<Object>)collection).add(new Object());
			Assert.fail("Should throw a org.granite.messaging.jmf.persistence.LazyInitializationException");
		}
		catch (org.granite.messaging.jmf.persistence.LazyInitializationException e) {
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
		Assert.assertTrue(collection instanceof org.granite.messaging.jmf.persistence.PersistentSortedSet);
		Assert.assertTrue(((org.granite.messaging.jmf.persistence.PersistentSortedSet<?>)collection).wasInitialized());
		Assert.assertFalse(((org.granite.messaging.jmf.persistence.PersistentSortedSet<?>)collection).isDirty());
		Assert.assertTrue(((org.granite.messaging.jmf.persistence.PersistentSortedSet<?>)collection).isEmpty());
		
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
		Assert.assertTrue(collection instanceof org.granite.messaging.jmf.persistence.PersistentSortedSet);
		Assert.assertTrue(((org.granite.messaging.jmf.persistence.PersistentSortedSet<?>)collection).wasInitialized());
		Assert.assertFalse(((org.granite.messaging.jmf.persistence.PersistentSortedSet<?>)collection).isDirty());
		Assert.assertFalse(((org.granite.messaging.jmf.persistence.PersistentSortedSet<?>)collection).isEmpty());
		Assert.assertTrue(((org.granite.messaging.jmf.persistence.PersistentSortedSet<?>)collection).size() == 3);
		
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
		Assert.assertTrue(collection instanceof org.granite.messaging.jmf.persistence.PersistentSortedSet);
		Assert.assertTrue(((org.granite.messaging.jmf.persistence.PersistentSortedSet<?>)collection).wasInitialized());
		Assert.assertFalse(((org.granite.messaging.jmf.persistence.PersistentSortedSet<?>)collection).isDirty());
		Assert.assertFalse(((org.granite.messaging.jmf.persistence.PersistentSortedSet<?>)collection).isEmpty());
		Assert.assertTrue(((org.granite.messaging.jmf.persistence.PersistentSortedSet<?>)collection).size() == 3);
		
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
		Assert.assertTrue(collection instanceof org.granite.messaging.jmf.persistence.PersistentSortedMap);
		Assert.assertFalse(((org.granite.messaging.jmf.persistence.PersistentSortedMap<?, ?>)collection).wasInitialized());
		try {
			((org.granite.messaging.jmf.persistence.PersistentSortedMap<Object, Object>)collection).put(new Object(), new Object());
			Assert.fail("Should throw a org.granite.messaging.jmf.persistence.LazyInitializationException");
		}
		catch (org.granite.messaging.jmf.persistence.LazyInitializationException e) {
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
		Assert.assertTrue(collection instanceof org.granite.messaging.jmf.persistence.PersistentSortedMap);
		Assert.assertTrue(((org.granite.messaging.jmf.persistence.PersistentSortedMap<?, ?>)collection).wasInitialized());
		Assert.assertFalse(((org.granite.messaging.jmf.persistence.PersistentSortedMap<?, ?>)collection).isDirty());
		Assert.assertTrue(((org.granite.messaging.jmf.persistence.PersistentSortedMap<?, ?>)collection).isEmpty());
		
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
		Assert.assertTrue(collection instanceof org.granite.messaging.jmf.persistence.PersistentSortedMap);
		Assert.assertTrue(((org.granite.messaging.jmf.persistence.PersistentSortedMap<?, ?>)collection).wasInitialized());
		Assert.assertFalse(((org.granite.messaging.jmf.persistence.PersistentSortedMap<?, ?>)collection).isDirty());
		Assert.assertFalse(((org.granite.messaging.jmf.persistence.PersistentSortedMap<?, ?>)collection).isEmpty());
		Assert.assertTrue(((org.granite.messaging.jmf.persistence.PersistentSortedMap<?, ?>)collection).size() == 3);
		
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
		Assert.assertTrue(collection instanceof org.granite.messaging.jmf.persistence.PersistentSortedMap);
		Assert.assertTrue(((org.granite.messaging.jmf.persistence.PersistentSortedMap<?, ?>)collection).wasInitialized());
		Assert.assertFalse(((org.granite.messaging.jmf.persistence.PersistentSortedMap<?, ?>)collection).isDirty());
		Assert.assertFalse(((org.granite.messaging.jmf.persistence.PersistentSortedMap<?, ?>)collection).isEmpty());
		Assert.assertTrue(((org.granite.messaging.jmf.persistence.PersistentSortedMap<?, ?>)collection).size() == 3);
		
		collection = serializeAndDeserializeClientToServer(collection, false);
		Assert.assertTrue(collection instanceof PersistentSortedMap);
		Assert.assertTrue(((PersistentSortedMap)collection).wasInitialized());
		Assert.assertFalse(((PersistentSortedMap)collection).isDirty());
		Assert.assertFalse(((PersistentSortedMap)collection).isEmpty());
		Assert.assertTrue(((PersistentSortedMap)collection).size() == 3);
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
		
		
		PrintStream ps = TestUtil.newNullPrintStream();
		if (dump) {
			System.out.println(bytes.length + "B. " + TestUtil.toHexString(bytes));
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
