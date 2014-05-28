/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2014 GRANITE DATA SERVICES S.A.S.
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
package org.granite.client.tide.data.impl;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import org.granite.client.tide.data.spi.DataManager;
import org.granite.client.util.PropertyHolder;
import org.granite.util.TypeUtil;

/**
 * @author William DRAI
 */
public class ObjectUtil {
	
	private static final Class<?> LOCAL_DATE = initClass("java.time.LocalDate");
	private static final Class<?> LOCAL_DATETIME = initClass("java.time.LocalDateTime");
	private static final Class<?> LOCAL_TIME = initClass("java.time.LocalTime");
	
	private static Class<?> initClass(String type) {
		try {
			return TypeUtil.forName(type);
		}
		catch (Throwable t) {
			// No Java 8
		}
		return null;
	}
	

    public static boolean isSimple(Object value) {
        return value instanceof String || value instanceof Boolean || value instanceof Number || value instanceof Date
        		|| (LOCAL_DATE != null && LOCAL_DATE.isInstance(value))
        		|| (LOCAL_DATETIME != null && LOCAL_DATETIME.isInstance(value))
        		|| (LOCAL_TIME != null && LOCAL_TIME.isInstance(value));        		
    }
    
    public static String toString(Object obj) {
        return obj != null ? obj.toString() : "null";
    }
    
    /**
     *  Check if a value is empty
     *
     *	@param val value
     *  @return value is empty
     */ 
    public static boolean isEmpty(Object val) {
        if (val == null)
            return true;
        else if (val instanceof String)
            return val.equals("");
        else if (val.getClass().isArray())
            return Array.getLength(val) == 0;
        else if (val instanceof Date)
            return ((Date)val).getTime() == 0L;
        else if (val instanceof Collection<?>)
            return ((Collection<?>)val).size() == 0;
        else if (val instanceof Map<?, ?>)
            return ((Map<?, ?>)val).size() == 0;
        return false; 
    }
    
    /**
     *  Equality for objects, using uid property when possible
     *
     *  @param obj1 object
     *  @param obj2 object
     * 
     *  @return true when objects are instances of the same entity
     */ 
    public static boolean objectEquals(DataManager dataManager, Object obj1, Object obj2) {
        if ((obj1 instanceof PropertyHolder && dataManager.isEntity(obj2)) || (dataManager.isEntity(obj1) && obj2 instanceof PropertyHolder))
            return false;
        
        if (dataManager.isEntity(obj1) && dataManager.isEntity(obj2) && obj1.getClass() == obj2.getClass()) {
            if (!dataManager.isInitialized(obj1) || !dataManager.isInitialized(obj2)) {
                // Compare with identifier for uninitialized entities
            	try {
                    return objectEquals(dataManager, dataManager.getId(obj1), dataManager.getId(obj2));
            	}
            	catch (Exception e) {
            		// No @Id;
            		return obj1.equals(obj2);
            	}
            }
            return dataManager.getUid(obj1).equals(dataManager.getUid(obj2));
        }
        
        if (obj1 == null)
        	return obj2 == null;
        
        return obj1.equals(obj2);
    }
}
