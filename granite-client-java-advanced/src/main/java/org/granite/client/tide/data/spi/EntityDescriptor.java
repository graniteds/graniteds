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
package org.granite.client.tide.data.spi;

import java.util.HashMap;
import java.util.Map;

import org.granite.logging.Logger;

/**
 * @author William DRAI
 */
public class EntityDescriptor {
    
    @SuppressWarnings("unused")
    private final static Logger log = Logger.getLogger(EntityDescriptor.class);
    
    private final Map<String, Boolean> lazy = new HashMap<String, Boolean>();
    
    
    public EntityDescriptor(Map<String, Boolean> lazy) {
   		if (lazy != null)
   			this.lazy.putAll(lazy);
    }
    
    public boolean isLazy(String propertyName) {
        return Boolean.TRUE.equals(lazy.get(propertyName));
    }
    
    public void setLazy(String propertyName) {
        lazy.put(propertyName, true);
    }
}
