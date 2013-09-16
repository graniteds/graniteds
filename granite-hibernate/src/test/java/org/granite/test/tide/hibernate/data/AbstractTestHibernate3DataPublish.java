/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
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

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletContext;

import org.granite.config.GraniteConfig;
import org.granite.config.ServletGraniteConfig;
import org.granite.config.api.Configuration;
import org.granite.config.api.internal.ConfigurationImpl;
import org.granite.config.flex.ServicesConfig;
import org.granite.config.flex.ServletServicesConfig;
import org.granite.context.SimpleGraniteContext;
import org.granite.test.tide.MockServletContext;
import org.granite.test.tide.data.Alias5;
import org.granite.test.tide.data.Contact5;
import org.granite.test.tide.data.LineItem;
import org.granite.test.tide.data.Location5;
import org.granite.test.tide.data.Order3;
import org.granite.tide.data.DataContext;
import org.granite.tide.data.DataEnabled.PublishMode;
import org.granite.tide.data.DefaultDataDispatcher;
import org.granite.tide.data.DefaultDataTopicParams;
import org.hibernate.collection.PersistentSet;
import org.junit.Assert;
import org.junit.Test;


public abstract class AbstractTestHibernate3DataPublish {
	
	protected void initContext() throws Exception {
		initPersistence();
		
		ServletContext servletContext = new MockServletContext();
        Configuration cfg = new ConfigurationImpl();
        cfg.setGraniteConfig("/WEB-INF/granite/granite-config-hibernate.xml");
        cfg.setFlexServicesConfig("/WEB-INF/flex/services-config-hibernate.xml");
        servletContext.setAttribute(ServletGraniteConfig.GRANITE_CONFIG_CONFIGURATION_KEY, cfg);
        GraniteConfig graniteConfig = ServletGraniteConfig.loadConfig(servletContext);
        ServicesConfig servicesConfig = ServletServicesConfig.loadConfig(servletContext);
        SimpleGraniteContext.createThreadInstance(graniteConfig, servicesConfig, new HashMap<String, Object>());
	}
	
	protected abstract void initPersistence() throws Exception;
	
	protected abstract void open();
	
	protected abstract <T> T find(Class<T> entityClass, Serializable id);
	
	protected abstract <T> T save(T entity);
	
	protected abstract <T> void remove(T entity);
	
	protected abstract void flush();
	
	protected abstract void flush(boolean commit);
	
	protected abstract void flushOnly();
	
	protected abstract void close();
	
    
    @Test
    public void testSimpleChanges() throws Exception {
    	initContext();
        
        Order3 o = new Order3(null, null, "O1");
        o.setDescription("Order");
        o.setLineItems(new HashSet<LineItem>());
        LineItem i1 = new LineItem(null, null, "I1");
        i1.setDescription("Item 1");
        i1.setOrder(o);
        o.getLineItems().add(i1);
        LineItem i2 = new LineItem(null, null, "I2");
        i2.setDescription("Item 2");
        i2.setOrder(o);
        o.getLineItems().add(i2);
        open();
        o = save(o);
        flush();
        Long orderId = o.getId();
        for (LineItem i : o.getLineItems()) {
            if ("I2".equals(i.getUid())) {
                i2 = i;
                break;
            }
        }
        Long itemId = i2.getId();
        close();
        
        DataContext.remove();
        DataContext.init(new DefaultDataDispatcher(null, "testTopic", DefaultDataTopicParams.class), PublishMode.MANUAL);
        
        open();
        o = find(Order3.class, orderId);
        i2 = find(LineItem.class, itemId);
        remove(i2);
        flush();
        close();
        
        Object[][] updates = DataContext.get().getUpdates();
        
        Assert.assertEquals("1 update", 1, updates.length);
        Assert.assertEquals("REMOVE", updates[0][0]);
        Assert.assertEquals(i2, updates[0][1]);
    }
    
    @Test
    public void testSimpleChanges2() throws Exception {
    	initContext();
        
        Order3 o = new Order3(null, null, "O1");
        o.setDescription("Order");
        o.setLineItems(new HashSet<LineItem>());
        LineItem i1 = new LineItem(null, null, "I1");
        i1.setDescription("Item 1");
        i1.setOrder(o);
        o.getLineItems().add(i1);
        open();
        o = save(o);
        flush();
        Long orderId = o.getId();
        close();
        
        DataContext.remove();
        DataContext.init(new DefaultDataDispatcher(null, "testTopic", DefaultDataTopicParams.class), PublishMode.MANUAL);
        
        open();
        o = find(Order3.class, orderId);
        LineItem i2 = new LineItem(null, null, "I2");
        i2.setDescription("Item 2");
        i2.setOrder(o);
        o.getLineItems().add(i2);
        flush();
        close();
        
        Object[][] updates = DataContext.get().getUpdates();
        
        Assert.assertEquals("2 updates", 2, updates.length);
        Assert.assertEquals("PERSIST", updates[0][0]);
        Assert.assertEquals(i2, updates[0][1]);
        Assert.assertEquals("UPDATE", updates[1][0]);
        Assert.assertEquals(o, updates[1][1]);
    }
    
    @Test
    public void testSimpleChanges3() throws Exception {
    	initContext();
        
        Order3 o = new Order3(null, null, "O1");
        o.setDescription("Order");
        o.setLineItems(new HashSet<LineItem>());
        LineItem i1 = new LineItem(null, null, "I1");
        i1.setDescription("Item 1");
        i1.setOrder(o);
        o.getLineItems().add(i1);
        open();
        o = save(o);
        flush();
        Long orderId = o.getId();
        close();
        
        DataContext.remove();
        DataContext.init(new DefaultDataDispatcher(null, "testTopic", DefaultDataTopicParams.class), PublishMode.MANUAL);
        
        open();
        o = find(Order3.class, orderId);
        LineItem i2 = new LineItem(null, null, "I2");
        i2.setDescription("Item 2");
        i2.setOrder(o);
        o.getLineItems().add(i2);
        flush(false);
        i2.setDescription("Item 2b");
        flush();
        close();
        
        Object[][] updates = DataContext.get().getUpdates();
        
        Assert.assertEquals("3 updates", 3, updates.length);
        Assert.assertEquals("PERSIST", updates[0][0]);
        Assert.assertEquals(i2, updates[0][1]);
        Assert.assertEquals("UPDATE", updates[1][0]);
        Assert.assertEquals(i2, updates[1][1]);
        Assert.assertEquals("UPDATE", updates[2][0]);
        Assert.assertEquals(o, updates[2][1]);
    }
    
    @Test
    public void testCascadeRemove() throws Exception {
		initContext();
        
        Order3 o = new Order3(null, null, "O1");
        o.setDescription("Order");
        o.setLineItems(new HashSet<LineItem>());
        LineItem i1 = new LineItem(null, null, "I1");
        i1.setDescription("Item 1");
        i1.setOrder(o);
        o.getLineItems().add(i1);
        LineItem i2 = new LineItem(null, null, "I2");
        i2.setDescription("Item 2");
        i2.setOrder(o);
        o.getLineItems().add(i2);
        open();
        o = save(o);
        Long orderId = o.getId();
        flush();
        close();
        
        DataContext.remove();
        DataContext.init(new DefaultDataDispatcher(null, "testTopic", DefaultDataTopicParams.class), PublishMode.MANUAL);
        
        open();
        o = find(Order3.class, orderId);
        remove(o);
        flush();
        close();
        
        Object[][] updates = DataContext.get().getUpdates();
        
        Assert.assertEquals("3 removals", 3, updates.length);
        Assert.assertEquals("REMOVE", updates[0][0]);
        Assert.assertEquals("REMOVE", updates[1][0]);
        Assert.assertEquals("REMOVE", updates[2][0]);
        boolean orderFound = false;
        for (Object[] update : updates) {
        	if (update[1] == o)
        		orderFound = true;
        }
        if (!orderFound)
        	Assert.fail("Order not found");
    }
	
	@Test
	public void testRemoveAll() throws Exception {
		initContext();
		
		Contact5 c = new Contact5(1L, null, "C1");
		@SuppressWarnings("unchecked")
		Set<Alias5> ca = new PersistentSet(null, new HashSet<Alias5>());
		c.setAliases(ca);
		Alias5 a1 = new Alias5(1L, null, "A1");
		Alias5 a2 = new Alias5(2L, null, "A2");

		open();
		c = save(c);
		a1.setContact(c);
		a1 = save(a1);
		a2.setContact(c);
		a2 = save(a2);
		c.getAliases().add(a1);
		c.getAliases().add(a2);
		c = save(c);
		flush();
		Long cId = c.getId();
		close();
		
		DataContext.init(null, null, PublishMode.MANUAL);
		
		open();
		c = find(Contact5.class, cId);
		Set<Alias5> aliases = new HashSet<Alias5>();
		aliases.addAll(c.getAliases());
		c.getAliases().clear();
		for (Alias5 a : aliases)
			remove(a);
		flush();
		close();
		
        Object[][] updates = DataContext.get().getUpdates();
        
        Assert.assertEquals("3 updates", 3, updates.length);
        Assert.assertEquals("UPDATE", updates[0][0]);
        Assert.assertEquals(c, updates[0][1]);
        Assert.assertEquals("REMOVE", updates[1][0]);
        Assert.assertEquals("REMOVE", updates[2][0]);
	}
	
	@Test
	public void testRemoveAll2() throws Exception {
		initContext();
		
		Contact5 c = new Contact5(1L, null, "C1");
		@SuppressWarnings("unchecked")
		Set<Alias5> ca = new PersistentSet(null, new HashSet<Alias5>());
		c.setAliases(ca);
		@SuppressWarnings("unchecked")
		Set<Location5> cl = new PersistentSet(null, new HashSet<Location5>());
		c.setLocations(cl);
		Alias5 a1 = new Alias5(1L, null, "A1");
		Alias5 a2 = new Alias5(2L, null, "A2");
		Location5 l1 = new Location5(1L, null, "L1");
		Location5 l2 = new Location5(2L, null, "L2");		

		open();
		c = save(c);
		a1.setContact(c);
		a1 = save(a1);
		a2.setContact(c);
		a2 = save(a2);
		l1.setContact(c);
		l1 = save(l1);
		l2.setContact(c);
		l2 = save(l2);
		c.getAliases().add(a1);
		c.getAliases().add(a2);
		c.getLocations().add(l1);
		c.getLocations().add(l2);
		c = save(c);
		flush();
		Long cId = c.getId();
		close();
		
		DataContext.init(null, null, PublishMode.MANUAL);
		
		open();
		c = find(Contact5.class, cId);
		Set<Alias5> aliases = new HashSet<Alias5>();
		aliases.addAll(c.getAliases());
		c.getAliases().clear();
		for (Alias5 a : aliases)
			remove(a);
		flush();
		close();
		
        Object[][] updates = DataContext.get().getUpdates();
        
        Assert.assertEquals("3 updates", 3, updates.length);
        Assert.assertEquals("UPDATE", updates[0][0]);
        Assert.assertEquals(c, updates[0][1]);
        Assert.assertEquals("REMOVE", updates[1][0]);
        Assert.assertEquals("REMOVE", updates[2][0]);
	}
}
