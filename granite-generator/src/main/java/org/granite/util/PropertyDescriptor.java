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
package org.granite.util;

import java.lang.reflect.Method;

public class PropertyDescriptor {
	
	private String name;
    private Method getter;
    private Method setter;

    public PropertyDescriptor(String propertyName, Method getter, Method setter) {
        this.name = propertyName;
        setReadMethod(getter);
        setWriteMethod(setter);
    }
    
    public String getName() {
    	return this.name;
    }

    public void setWriteMethod(Method setter) {
        this.setter = setter;
    }

    public void setReadMethod(Method getter) {
        this.getter = getter;
    }

    public Method getWriteMethod() {
        return setter;
    }

    public Method getReadMethod() {
        return getter;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof PropertyDescriptor))
        	return false;

        PropertyDescriptor pd = (PropertyDescriptor)object;
        if (!((this.getter == null && pd.getter == null) 
        		|| (this.getter != null && this.getter.equals(pd.getter))))
        	return false;
        
        if (!((this.setter == null && pd.setter == null) 
        		|| (this.setter != null && this.setter.equals(pd.setter))))
        	return false;
        
        return this.getPropertyType() == pd.getPropertyType();
    }

    @Override
    public int hashCode() {
    	int hashCode = getter != null ? getter.hashCode() : 0;
    	if (setter != null)
    		hashCode = hashCode*31 + setter.hashCode();
    	if (getPropertyType() != null)
    		hashCode = hashCode*31 + getPropertyType().hashCode();
    	return hashCode;
    }

    public Class<?> getPropertyType() {
        if (getter != null)
            return getter.getReturnType();
        if (setter != null) {
            Class<?>[] parameterTypes = setter.getParameterTypes();
            return parameterTypes[0];
        }
        return null;
    }
}