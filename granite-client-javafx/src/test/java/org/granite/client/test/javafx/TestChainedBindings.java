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
