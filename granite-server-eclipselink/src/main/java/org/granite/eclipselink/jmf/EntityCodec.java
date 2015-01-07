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
package org.granite.eclipselink.jmf;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.IdClass;
import javax.persistence.MappedSuperclass;

import org.eclipse.persistence.indirection.ValueHolderInterface;
import org.granite.eclipselink.EclipseLinkProxy;
import org.granite.eclipselink.EclipseLinkValueHolder;
import org.granite.logging.Logger;
import org.granite.messaging.jmf.ExtendedObjectInput;
import org.granite.messaging.jmf.ExtendedObjectOutput;
import org.granite.messaging.jmf.codec.ExtendedObjectCodec;
import org.granite.messaging.reflect.Property;
import org.granite.util.TypeUtil;

/**
 * @author Franck WOLFF
 */
public class EntityCodec implements ExtendedObjectCodec {

	private static final Logger log = Logger.getLogger(EntityCodec.class);

	public boolean canEncode(ExtendedObjectOutput out, Object v) {
		Class<?> cls = getClass(out, v);
		return (cls.isAnnotationPresent(Entity.class) || cls.isAnnotationPresent(MappedSuperclass.class) || v instanceof EclipseLinkProxy);
	}

	public String getEncodedClassName(ExtendedObjectOutput out, Object v) {
        return getClass(out, v).getName();
	}

	public void encode(ExtendedObjectOutput out, Object v) throws IOException, IllegalAccessException, InvocationTargetException {
        if (v instanceof EclipseLinkProxy) {
        	out.writeBoolean(false);
        	out.writeUTF(null);
            out.writeObject(null);
            return;
        }

        // Write initialized flag & detachedState.
        out.writeBoolean(true);
        out.writeUTF(null);
		
        // Write all properties in lexical order.
        Set<String> lazyFieldNames = new HashSet<String>();
		List<Property> properties = out.getReflection().findSerializableProperties(v.getClass());
		for (Property property : properties) {
			if (isIgnoredProperty(property))
				continue;
			
			if (ValueHolderInterface.class.isAssignableFrom(property.getType())) {
				ValueHolderInterface vh = (ValueHolderInterface)property.getObject(v);
				
				if (vh != null && !vh.isInstantiated())
                    lazyFieldNames.add(property.getName().substring("_persistence_".length(), property.getName().length() - 3));
			}
			else if (lazyFieldNames.contains(property.getName()))
				out.writeObject(new EclipseLinkProxy(property.getType()));
			else
				out.getAndWriteProperty(v, property);
		}
	}

	public boolean canDecode(ExtendedObjectInput in, String className) throws ClassNotFoundException {
		Class<?> cls = in.getReflection().loadClass(className);
		return (cls.isAnnotationPresent(Entity.class) || cls.isAnnotationPresent(MappedSuperclass.class));
	}

	public String getDecodedClassName(ExtendedObjectInput in, String className) {
		return in.getAlias(className);
	}

	public Object newInstance(ExtendedObjectInput in, String className)
		throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException,
		InvocationTargetException, SecurityException, NoSuchMethodException, IOException {
		
		Class<?> cls = in.getReflection().loadClass(className);
		
        // Read initialized flag & detachedState.
		boolean initialized = in.readBoolean();
    	in.readUTF();
		
		if (initialized)
			return in.getReflection().newInstance(cls);
		
        // Create a (pseudo) proxy.
		Serializable id = (Serializable)in.readObject();
    	if (id != null && (!cls.isAnnotationPresent(IdClass.class) || !cls.getAnnotation(IdClass.class).value().equals(id.getClass())))
    		throw new RuntimeException("Id for EclipseLink pseudo-proxy should be null or IdClass (" + className + ")");
		
		return new EclipseLinkValueHolder();
	}

	public void decode(ExtendedObjectInput in, Object v) throws IOException, ClassNotFoundException, IllegalAccessException, InvocationTargetException {
		if (!(v instanceof EclipseLinkValueHolder)) {
			Map<String, Property> lazyFields = new HashMap<String, Property>();
			List<Property> properties = in.getReflection().findSerializableProperties(v.getClass());
			for (Property property : properties) {
				if (isIgnoredProperty(property))
					continue;
				
				if (ValueHolderInterface.class.isAssignableFrom(property.getType()))
					lazyFields.put(property.getName(), property);
				else {
					Object value = in.readObject();
					
					if (value instanceof ValueHolderInterface)
						lazyFields.get("_persistence_" + property.getName() + "_vh").setObject(v, value);
					else if (property.isWritable())
						property.setObject(v, value);
				}
			}
		}
	}
	
	protected boolean isIgnoredProperty(Property property) {
		return "_persistence_fetchGroup".equals(property.getName());
	}
	
	protected Class<?> getClass(ExtendedObjectOutput out, Object v) {

        if (v instanceof ValueHolderInterface) {
            ValueHolderInterface holder = (ValueHolderInterface)v;
            
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
        
        if (v instanceof EclipseLinkProxy)
            return ((EclipseLinkProxy)v).getProxiedClass();

		return v.getClass();
	}
}
