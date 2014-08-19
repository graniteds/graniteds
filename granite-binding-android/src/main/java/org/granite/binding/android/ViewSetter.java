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
package org.granite.binding.android;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import android.view.View;

/**
 * @author William DRAI
 */
public class ViewSetter<V extends View> {
	
	private final Class<V> viewClass;
	private final String property;
	private final List<Setter<V>> setters = new ArrayList<Setter<V>>();
	
	public ViewSetter(Class<V> viewClass, String property) {
		this.viewClass = viewClass;
		this.property = property;
		initMethodSetters(setters);
	}
	
	private void initMethodSetters(List<Setter<V>> setters) {
		for (Method s : viewClass.getMethods()) {
			if (s.getParameterTypes().length == 1 && s.getName().equals("set" + property.substring(0, 1).toUpperCase() + property.substring(1)))
				setters.add(new MethodSetter<V>(s));
		}
	}
	
	public void registerSetter(Setter<V> setter) {
		setters.add(setter);
	}
	
	public void setValue(V view, Object newValue) {
		try {
			for (Setter<V> setter : setters) {
				if (setter.accepts(newValue))
					setter.setValue(view, newValue);
			}
		}
		catch (Exception e) {
			throw new RuntimeException("Could not set view value", e);
		}
	}
	
	
	private static final class MethodSetter<V extends View> implements Setter<V> {
		
		private final Method method;
		private final Class<?> type;
		
		public MethodSetter(Method method) {
			this.method = method;
			Class<?> type = method.getParameterTypes()[0];
			if (type.isPrimitive()) {
				if (type == boolean.class)
					type = Boolean.class;
				else if (type == short.class)
					type = Short.class;
				else if (type == int.class)
					type = Integer.class;
				else if (type == long.class)
					type = Long.class;
				else if (type == byte.class)
					type = Byte.class;
				else if (type == char.class)
					type = Character.class;
			}
			this.type = type;
		}
		
		public boolean accepts(Object value) {
			return type.isInstance(value);
		}
		
		public void setValue(V view, Object value) throws Exception {
			method.invoke(view, value);
		}
	}
}