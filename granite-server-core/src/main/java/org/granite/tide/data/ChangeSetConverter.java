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
package org.granite.tide.data;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;

import org.granite.messaging.amf.io.convert.Converter;
import org.granite.messaging.amf.io.convert.Converters;
import org.granite.util.TypeUtil;


/**
 * @author William DRAI
 */
public class ChangeSetConverter extends Converter {
	
	private ConcurrentMap<Class<?>, Class<?>> proxyClasses = new ConcurrentHashMap<Class<?>, Class<?>>();

	public ChangeSetConverter(Converters converters) {
		super(converters);
	}

	@Override
	protected boolean internalCanConvert(Object value, Type targetType) {
		if (value instanceof ChangeSet && targetType instanceof Class<?>) {
			if (((ChangeSet)value).getChanges() == null || ((ChangeSet)value).getChanges().length == 0)
				throw new IllegalArgumentException("Incoming ChangeSet objects must contain at least one Change");
			
			String className = ((ChangeSet)value).getChanges()[0].getClassName();
			try {
				Class<?> clazz = TypeUtil.forName(className);
				return ((Class<?>)targetType).isAssignableFrom(clazz);
			} 
			catch (ClassNotFoundException e) {
				throw new RuntimeException("Cannot find class for ChangeSet argument " + className);
			}
		}
		return false;
	}

	@Override
	protected Object internalConvert(Object value, Type targetType) {
		ChangeSet changeSet = (ChangeSet)value;
		String className = changeSet.getChanges()[0].getClassName();
		try {
			Class<?> clazz = TypeUtil.forName(className);
			Class<?> proxyClass = proxyClasses.get(clazz);
			if (proxyClass == null) {
				ProxyFactory proxyFactory = new ProxyFactory();
				proxyFactory.setFilter(FINALIZE_FILTER);
				proxyFactory.setSuperclass(clazz);
				proxyFactory.setInterfaces(new Class<?>[] { ChangeSetProxy.class });
				proxyClass = proxyFactory.createClass();
				proxyClasses.put(clazz, proxyClass);
			}
				
			ProxyObject proxyObject = (ProxyObject)proxyClass.newInstance();
			proxyObject.setHandler(new ChangeSetProxyHandler(proxyObject, changeSet));
			return proxyObject;
		}
		catch (Exception e) {
			throw new RuntimeException("Cannot build proxy for Change argument " + className);
		}
	}

	private static final MethodFilter FINALIZE_FILTER = new MethodFilter() {
		public boolean isHandled(Method m) {
			return m.getParameterTypes().length > 0 || !m.getName().equals("finalize");
		}
	};

	private static class ChangeSetProxyHandler implements MethodHandler {
		
		private final Object proxyObject;
		private final ChangeSet changeSet;

		public ChangeSetProxyHandler(ProxyObject proxyObject, ChangeSet changeSet) {
			this.proxyObject = proxyObject;
			this.changeSet = changeSet;
		}

		public Object invoke(Object object, Method method, Method method1, Object[] args) throws Exception {
			String name = method.getName();
			if ("toString".equals(name)) {
				return changeSet.getChanges()[0].getClassName() + "@" + System.identityHashCode(object);
			}
			else if ("equals".equals(name)) {
				return proxyObject == object;
			}
			else if ("hashCode".equals(name)) {
				return System.identityHashCode(object);
			}
			else if ("getChangeSetProxyData".equals(name) && method.getParameterTypes().length == 0) {
				return changeSet;
			}
			return null;
		}
	}
}
