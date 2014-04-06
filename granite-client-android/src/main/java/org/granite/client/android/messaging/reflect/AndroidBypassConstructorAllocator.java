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
package org.granite.client.android.messaging.reflect;

import java.io.ObjectStreamClass;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.granite.messaging.reflect.BypassConstructorAllocator;
import org.granite.messaging.reflect.Instantiator;

/**
 * @author Franck WOLFF
 */
public class AndroidBypassConstructorAllocator implements BypassConstructorAllocator {

	private final Method newInstance;
	private final Object secondParameter;
	
	public AndroidBypassConstructorAllocator() {
		Object[] allocator = resolve();
		
		this.newInstance = (Method)allocator[0];
		this.secondParameter = allocator[1];
	}
	
	public Instantiator newInstantiator(final Class<?> cls) throws IllegalAccessException, InvocationTargetException {
		return new Instantiator() {

			@Override
			public Object newInstance() throws InstantiationException, IllegalAccessException, InvocationTargetException {
				return newInstance.invoke(null, cls, secondParameter);
			}
		};
	}
	
	private static Object[] resolve() {
        try {
            final Method newInstance = ObjectStreamClass.class.getDeclaredMethod("newInstance", Class.class, Class.class);
            newInstance.setAccessible(true);
            return new Object[] {newInstance, Object.class};
        }
        catch (Exception e) {
        	// fallback...
        }
        
        try {
            Method getConstructorId = ObjectStreamClass.class.getDeclaredMethod("getConstructorId", Class.class);
            getConstructorId.setAccessible(true);
            
            final Number constructorId = (Number)getConstructorId.invoke(null, Object.class);
            
            final Method newInstance = ObjectStreamClass.class.getDeclaredMethod("newInstance", Class.class, getConstructorId.getReturnType());
            newInstance.setAccessible(true);
            return new Object[] {newInstance, constructorId};
        }
        catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
	}
}
