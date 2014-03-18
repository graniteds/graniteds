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
