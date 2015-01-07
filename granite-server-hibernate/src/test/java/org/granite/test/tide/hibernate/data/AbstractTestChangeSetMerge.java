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
package org.granite.test.tide.hibernate.data;

import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;

import org.granite.config.GraniteConfig;
import org.granite.config.flex.ServicesConfig;
import org.granite.context.GraniteContext;
import org.granite.context.SimpleGraniteContext;
import org.granite.test.tide.data.Address;
import org.granite.tide.data.Change;
import org.granite.tide.data.ChangeSet;
import org.granite.tide.data.CollectionChange;
import org.granite.tide.data.CollectionChanges;
import org.granite.tide.data.DataContext;
import org.hibernate.collection.PersistentSet;
import org.junit.Assert;
import org.junit.Test;

public abstract class AbstractTestChangeSetMerge {
	
	protected abstract void initPersistence();
	
	protected abstract void open();
	protected abstract <T> T find(Class<T> entityClass, Serializable id);
	protected abstract <T> T save(T entity);
	protected abstract <T> T merge(T entity);
	protected abstract void flush();
	protected abstract void close();

    private static final String GRANITE_CONFIG_PATH = "org/granite/test/tide/hibernate/data/granite-config-hibernate-changeset.xml";

	
	@SuppressWarnings("unchecked")
	@Test
	public void testSimpleChanges() throws Exception {
		initPersistence();
		
		Person1 p = new Person1(null, null, "P1");
		p.setFirstName("test");
		p.setLastName("test");
		open();
		p = save(p);
		flush();
		Long personId = p.getId();
		close();
		
		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(GRANITE_CONFIG_PATH);
		GraniteConfig graniteConfig = new GraniteConfig(null, is, null, "test");
		ServicesConfig servicesConfig = new ServicesConfig(null, null, false);
		SimpleGraniteContext.createThreadInstance(graniteConfig, servicesConfig, new HashMap<String, Object>());
		
		Change change = new Change(Person1.class.getName(), personId, 0L, "P1");
		change.getChanges().put("lastName", "toto");
		ChangeSet changeSet = new ChangeSet(new Change[] { change });
		
		Person1 proxy = (Person1)graniteConfig.getConverters().convert(changeSet, Person1.class);
		
		open();
		
		merge(proxy);
		
		flush();
		close();
		
		open();
		
		p = find(Person1.class, personId);
		
		Assert.assertEquals("Person name changed", "toto", p.getLastName());
		
		close();
		
		GraniteContext.release();
		DataContext.remove();
		
		SimpleGraniteContext.createThreadInstance(graniteConfig, servicesConfig, new HashMap<String, Object>());
		
		Person1 p2 = new Person1(personId, 1L, "P1");
		p2.setContacts(new PersistentSet(null));
		Address a2 = new Address(null, null, "A1");
		Contact1 c2 = new Contact1(null, null, "C1");
		c2.setPerson(p2);
		c2.setEmail("test@test.net");
		c2.setAddress(a2);
		Change change2 = new Change(Person1.class.getName(), personId, 1L, "P1");
		CollectionChanges ccs2 = new CollectionChanges(new CollectionChange[] { new CollectionChange(1, null, c2) });
		change2.getChanges().put("contacts", ccs2);
		ChangeSet changeSet2 = new ChangeSet(new Change[] { change2 });
		
		Person1 proxy2 = (Person1)graniteConfig.getConverters().convert(changeSet2, Person1.class);
		
		open();
		
		merge(proxy2);
		
		flush();
		close();
		
		open();
		
		p = find(Person1.class, personId);
		
		Assert.assertEquals("Person name changed", "toto", p.getLastName());
		
		close();
		
	}
}
