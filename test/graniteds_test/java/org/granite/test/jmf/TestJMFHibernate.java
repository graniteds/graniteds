package org.granite.test.jmf;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.granite.hibernate.jmf.PersistentListCodec;
import org.granite.hibernate.jmf.PersistentMapCodec;
import org.granite.hibernate.jmf.PersistentSetCodec;
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
import org.hibernate.collection.PersistentList;
import org.hibernate.collection.PersistentSet;
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
		
		List<String> defaultStoredStrings = Arrays.asList(
			org.granite.messaging.jmf.persistence.PersistentList.class.getName(),
			org.granite.messaging.jmf.persistence.PersistentMap.class.getName(),
			org.granite.messaging.jmf.persistence.PersistentSet.class.getName(),
			org.granite.messaging.jmf.persistence.PersistentSortedSet.class.getName()
		);
		
		dumpSharedContext = new DefaultSharedContext(new DefaultCodecRegistry(), defaultStoredStrings);
		
		ExtendedObjectCodec[] extendedObjectCodecs = {
			new PersistentListCodec(),
			new PersistentMapCodec(),
			new PersistentSetCodec(),
			new PersistentSortedSetCodec()
		};
		serverSharedContext = new DefaultSharedContext(new DefaultCodecRegistry(Arrays.asList(extendedObjectCodecs)), defaultStoredStrings);
		
		clientSharedContext = new DefaultSharedContext(new DefaultCodecRegistry(), defaultStoredStrings);
	}
	
	@After
	public void after() {
		dumpSharedContext = null;
		serverSharedContext = null;
		clientSharedContext = null;
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testPersitentList() throws ClassNotFoundException, IOException {
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
	public void testPersitentSet() throws ClassNotFoundException, IOException {
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
