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
package org.granite.openjpa;

import java.util.Map;

import javax.persistence.Entity;

import org.apache.openjpa.enhance.PersistenceCapable;
import org.granite.logging.Logger;
import org.granite.messaging.amf.io.util.DefaultClassGetter;

/**
 * @author Franck WOLFF
 */
public class OpenJpaClassGetter extends DefaultClassGetter {

    public final static Logger log = Logger.getLogger(OpenJpaClassGetter.class);

    @Override
    public Class<?> getClass(Object o) {
    	// Nothing special here: OpenJPA uses null value for uninitialized properties.
        return super.getClass(o);
    }
    
    @Override
    public boolean isEntity(Object o) {
    	return o.getClass().isAnnotationPresent(Entity.class);    
    }
    
    
    @Override
    public boolean isInitialized(Object owner, String propertyName, Object propertyValue) {
    	if (propertyValue != null || !(owner instanceof PersistenceCapable))
    		return true;
    	Map<String, Boolean> loadedState = OpenJpaExternalizer.getLoadedState((PersistenceCapable)owner, getClass(owner));
        return loadedState.containsKey(propertyName) && loadedState.get(propertyName);
    }
    
    @Override
    public void initialize(Object owner, String propertyName, Object propertyValue) {
    	// Nothing to do: getting the property value when an EntityManager is opened should actually fetch
    	// the uninitialized property...
    }
}
