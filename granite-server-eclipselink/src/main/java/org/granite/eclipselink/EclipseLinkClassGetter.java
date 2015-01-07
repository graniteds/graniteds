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
package org.granite.eclipselink;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;

import org.granite.logging.Logger;
import org.granite.messaging.amf.io.util.DefaultClassGetter;
import org.granite.eclipselink.EclipseLinkClassGetter;
import org.granite.eclipselink.EclipseLinkProxy;
import org.granite.util.TypeUtil;

import org.eclipse.persistence.indirection.IndirectContainer;
import org.eclipse.persistence.indirection.ValueHolderInterface;

/**
 * @author William DRAI
 */
public class EclipseLinkClassGetter extends DefaultClassGetter {

    private final static Logger log = Logger.getLogger(EclipseLinkClassGetter.class);

    @Override
    public Class<?> getClass(Object o) {

        if (o instanceof ValueHolderInterface) {
            ValueHolderInterface holder = (ValueHolderInterface)o;
            
            String className = (
            	holder.isInstantiated() ?
            	holder.getValue().getClass().getName() :
            	Object.class.getName()
            ); 
            
            if (className != null && className.length() > 0) {
                try {
                    return TypeUtil.forName(className);
                } catch (Exception e) {
                    log.warn(e, "Could not get class with initializer: %s for: %s", className, className);
                }
            }
            // fallback...
            return Object.class;
        }
        else if (o instanceof EclipseLinkProxy) {
            return ((EclipseLinkProxy)o).getProxiedClass();
        }

        return super.getClass(o);
    }
    
    @Override
    public boolean isEntity(Object o) {
    	return o.getClass().isAnnotationPresent(Entity.class);    
    }
    
    @Override
    public boolean isInitialized(Object owner, String propertyName, Object propertyValue) {
        Object value = propertyValue;
        if (owner != null) {
            Field vhf = null;
            Class<?> c = owner.getClass();
            while (c != null && c != Object.class) {
                for (Field f : c.getDeclaredFields()) {
                    if (f.getName().equals("_persistence_" + propertyName + "_vh")) {
                        vhf = f;
                        break;
                    }
                }
                if (vhf != null)
                    break;
                c = c.getSuperclass();
            }
            if (vhf != null) {
                try {
                    vhf.setAccessible(true);
                    value = vhf.get(owner);
                }
                catch (Exception e) {
                    log.error(e, "Could not get persistence ValueHolder " + propertyName + " for class " + owner.getClass());
                }
            }
        }        
        if (value instanceof ValueHolderInterface)
            return ((ValueHolderInterface)value).isInstantiated();
        else if (value instanceof IndirectContainer)
            return ((IndirectContainer)value).isInstantiated();
        
        return true;
    }
    
    @Override
    public void initialize(Object owner, String propertyName, Object propertyValue) {
        if (propertyValue instanceof ValueHolderInterface)
            ((ValueHolderInterface)propertyValue).getValue().toString();
        else if (propertyValue instanceof IndirectContainer)
            ((IndirectContainer)propertyValue).getValueHolder().getValue().toString();
    }
    
    @Override
    public List<Object
    []> getFieldValues(Object obj, Object dest) {
        List<Object[]> fieldValues = new ArrayList<Object[]>();
        
        List<String> topLinkVhs = new ArrayList<String>();
        
        // Merges field values
        try {
            Class<?> clazz = obj.getClass();
            while (clazz != null) {
                Field[] fields = clazz.getDeclaredFields();
                for (Field field : fields) {
                    if ((field.getModifiers() & Modifier.STATIC) != 0 
                        || (field.getModifiers() & Modifier.FINAL) != 0 
                        || (field.getModifiers() & Modifier.VOLATILE) != 0 
                        || (field.getModifiers() & Modifier.NATIVE) != 0 
                        || (field.getModifiers() & Modifier.TRANSIENT) != 0)
                        continue;
                    
                    if (ValueHolderInterface.class.isAssignableFrom(field.getType())) {
                        field.setAccessible(true);
                        ValueHolderInterface vh = (ValueHolderInterface)field.get(obj);
                        if (!vh.isInstantiated()) {
                            topLinkVhs.add(field.getName());
                            field.set(dest, vh);
                        }
                    }
                    else if (!topLinkVhs.contains("_persistence_" + field.getName() + "_vh")) {
                        field.setAccessible(true);
                        Object o = field.get(obj);
                        if (dest != null) {
                            Object d = field.get(dest);
                            fieldValues.add(new Object[] { field, o, d });
                        }
                        else
                            fieldValues.add(new Object[] { field, o });
                    }
                }
                clazz = clazz.getSuperclass();
            }
        }
        catch (Exception e) {
            throw new RuntimeException("Could not merge entity ", e);
        }
        
        return fieldValues;
    }
}
