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
package org.granite.client.test.javafx;

import org.granite.client.javafx.util.ChainedProperty;
import org.junit.Test;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.junit.Assert;

public class TestChainedBindings {

	@Test
	public void testNestedChainedProperty() {
		
		StringProperty s = new SimpleStringProperty();
		
		ObjectProperty<A> aProperty = new SimpleObjectProperty<A>();
		A a = new A();
		aProperty.set(a);
		
		s.bind(ChainedProperty.chain(aProperty, new ChainedProperty.PropertyGetter<A, B>() {
			@Override
			public Property<B> getProperty(A a) {
				return a.bProperty();
			}
		}).chain(new ChainedProperty.PropertyGetter<B, String>() {
			@Override
			public Property<String> getProperty(B b) {
				return b.sProperty();
			}
		}));
		
		Assert.assertNull(s.getValue());
		
		B b = new B();
		b.sProperty().set("Bla");		
		a.bProperty().set(b);
		
		Assert.assertEquals("Bla", s.get());
		
		b.sProperty().set("Blo");

		Assert.assertEquals("Blo", s.get());
		
		B b2 = new B();
		b2.sProperty().set("Blu");
		a.bProperty().set(b2);

		Assert.assertEquals("Blu", s.get());
		
		a.bProperty().set(null);
		
		Assert.assertNull(s.getValue());
	}
	
	
	private static class A {
		
		private ObjectProperty<B> b = new SimpleObjectProperty<B>(this, "b");
		
		public ObjectProperty<B> bProperty() {
			return b;
		}
	}
	
	private static class B {
		
		private StringProperty s = new SimpleStringProperty(this, "s");
		
		public StringProperty sProperty() {
			return s;
		}
	}
}
