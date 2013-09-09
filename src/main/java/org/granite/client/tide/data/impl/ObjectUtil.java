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

package org.granite.client.tide.data.impl;

import java.util.Date;

import org.granite.client.tide.PropertyHolder;
import org.granite.client.tide.data.spi.DataManager;

/**
 * @author William DRAI
 */
public class ObjectUtil {

    public static boolean isSimple(Object value) {
        return value instanceof String || value instanceof Boolean || value instanceof Number || value instanceof Date;
    }
    
    public static String toString(Object obj) {
        return obj != null ? obj.toString() : "null";
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
