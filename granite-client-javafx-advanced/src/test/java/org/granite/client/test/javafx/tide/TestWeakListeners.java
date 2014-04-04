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

package org.granite.client.test.javafx.tide;

import java.util.List;

import javafx.collections.ListChangeListener;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.WeakListChangeListener;

import org.granite.client.javafx.util.ListListenerHelper;
import org.junit.Assert;
import org.junit.Test;


public class TestWeakListeners {
    
    @Test
    public void testWeakListeners() throws Exception {
    	ListListenerHelper<Person> helper = new ListListenerHelper<Person>();
    	
    	@SuppressWarnings("unchecked")
		ListChangeListener<Person>[] references = new ListChangeListener[2];
    	references[0] = new TestListChangeListener();
    	references[1] = new TestListChangeListener();
    	
    	WeakListChangeListener<Person> weakListener1 = new WeakListChangeListener<Person>(references[0]);
    	WeakListChangeListener<Person> weakListener2 = new WeakListChangeListener<Person>(references[1]);
    	
    	helper.addListener(weakListener1);
    	helper.addListener(weakListener2);
    	
    	Assert.assertEquals("Listeners before gc", 2, helper.getListChangeListenersListenersSize());
    	
    	references[0] = null;
    	references[1] = null;
    	
    	System.gc();
    	
    	final Boolean[] changed = new Boolean[1];
    	
    	helper.addListener(new ListChangeListener<Person>() {
			@Override
			public void onChanged(ListChangeListener.Change<? extends Person> change) {
				changed[0] = true;
			}    		
    	});
    	
    	helper.fireValueChangedEvent(new MockChange());
    	
    	Assert.assertEquals("Listeners after gc", 1, helper.getListChangeListenersListenersSize());
    	Assert.assertTrue("Last one triggered", changed[0]);
    }
    
    @Test
    public void testWeakListeners2() throws Exception {
    	ListListenerHelper<Person> helper = new ListListenerHelper<Person>();
    	
    	@SuppressWarnings("unchecked")
		ListChangeListener<Person>[] references = new ListChangeListener[3];
    	references[0] = new TestListChangeListener();
    	references[1] = new TestListChangeListener();
    	references[2] = new TestListChangeListener();
    	
    	WeakListChangeListener<Person> weakListener1 = new WeakListChangeListener<Person>(references[0]);
    	WeakListChangeListener<Person> weakListener2 = new WeakListChangeListener<Person>(references[1]);
    	WeakListChangeListener<Person> weakListener3 = new WeakListChangeListener<Person>(references[2]);
    	
    	final Boolean[] changed = new Boolean[1];
    	
    	helper.addListener(weakListener1);
    	helper.addListener(weakListener2);    	
    	helper.addListener(new ListChangeListener<Person>() {
			@Override
			public void onChanged(ListChangeListener.Change<? extends Person> change) {
				changed[0] = true;
			}    		
    	});
    	helper.addListener(weakListener3);
    	
    	Assert.assertEquals("Listeners before gc", 4, helper.getListChangeListenersListenersSize());
    	
    	references[1] = null;
    	
    	System.gc();
    	
    	helper.removeListener(weakListener1);
    	
    	helper.fireValueChangedEvent(new MockChange());
    	
    	Assert.assertEquals("Listeners after gc", 2, helper.getListChangeListenersListenersSize());
    	Assert.assertTrue("Last one triggered", changed[0]);
    }
    
    @Test
    public void testWeakListeners3() throws Exception {
    	ListListenerHelper<Person> helper = new ListListenerHelper<Person>();
    	
    	@SuppressWarnings("unchecked")
		ListChangeListener<Person>[] references = new ListChangeListener[3];
    	references[0] = new TestListChangeListener();
    	references[1] = new TestListChangeListener();
    	references[2] = new TestListChangeListener();
    	
    	WeakListChangeListener<Person> weakListener1 = new WeakListChangeListener<Person>(references[0]);
    	WeakListChangeListener<Person> weakListener2 = new WeakListChangeListener<Person>(references[1]);
    	WeakListChangeListener<Person> weakListener3 = new WeakListChangeListener<Person>(references[2]);
    	
    	final Boolean[] changed = new Boolean[1];
    	
    	helper.addListener(weakListener1);
    	helper.addListener(new ListChangeListener<Person>() {
			@Override
			public void onChanged(ListChangeListener.Change<? extends Person> change) {
				changed[0] = true;
			}    		
    	});
    	helper.addListener(weakListener2);    	
    	helper.addListener(weakListener3);
    	
    	Assert.assertEquals("Listeners before gc", 4, helper.getListChangeListenersListenersSize());
    	
    	references[1] = null;
    	
    	System.gc();
    	
    	helper.removeListener(weakListener1);
    	
    	helper.fireValueChangedEvent(new MockChange());
    	
    	Assert.assertEquals("Listeners after gc", 2, helper.getListChangeListenersListenersSize());
    	Assert.assertTrue("Last one triggered", changed[0]);
    }
    
    public static class TestListChangeListener implements ListChangeListener<Person> {
		@Override
		public void onChanged(ListChangeListener.Change<? extends Person> change) {
		}    	
    }
    
    public static class MockChange extends Change<Person> {
    	public MockChange() {
    		super(null);
    	}
    	
		@Override
		public int getFrom() {
			return 0;
		}
	
		@Override
		protected int[] getPermutation() {
			return null;
		}
	
		@Override
		public List<Person> getRemoved() {
			return null;
		}
	
		@Override
		public int getTo() {
			return 0;
		}
	
		@Override
		public boolean next() {
			return false;
		}
	
		@Override
		public void reset() {
		}
    }
}
