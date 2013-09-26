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

/**
 * @author Franck WOLFF
 */
public class AndroidBypassConstructorAllocator implements BypassConstructorAllocator {

	private final BypassConstructorAllocator allocator;
	
	public AndroidBypassConstructorAllocator() {
		this.allocator = findAllocator();
	}

	@Override
	public <T> T newInstance(Class<T> cls)
		throws InstantiationException, IllegalAccessException, IllegalArgumentException,
		InvocationTargetException, SecurityException, NoSuchMethodException {
		return allocator.newInstance(cls);
	}
	
	private static BypassConstructorAllocator findAllocator() {
        try {
            final Method newInstance = ObjectStreamClass.class.getDeclaredMethod("newInstance", Class.class, Class.class);
            newInstance.setAccessible(true);
            
            return new BypassConstructorAllocator() {
                
            	@SuppressWarnings("unchecked")
				@Override
                public <T> T newInstance(Class<T> cls)
                    throws InstantiationException, IllegalAccessException, IllegalArgumentException,
                    InvocationTargetException, SecurityException, NoSuchMethodException {

                    return (T)newInstance.invoke(null, cls, Object.class);
                }
            };
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
            
            return new BypassConstructorAllocator() {
                
            	@SuppressWarnings("unchecked")
				@Override
                public <T> T newInstance(Class<T> cls)
                    throws InstantiationException, IllegalAccessException, IllegalArgumentException,
                    InvocationTargetException, SecurityException, NoSuchMethodException {

                    return (T)newInstance.invoke(null, cls, constructorId);
                }
            };
        }
        catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
	}
}
