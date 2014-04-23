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
package org.granite.toplink;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.IdClass;
import javax.persistence.MappedSuperclass;

import oracle.toplink.essentials.indirection.IndirectContainer;
import oracle.toplink.essentials.indirection.IndirectList;
import oracle.toplink.essentials.indirection.IndirectMap;
import oracle.toplink.essentials.indirection.IndirectSet;
import oracle.toplink.essentials.indirection.ValueHolderInterface;

import org.granite.collections.BasicMap;
import org.granite.config.ConvertersConfig;
import org.granite.context.GraniteContext;
import org.granite.logging.Logger;
import org.granite.messaging.amf.io.convert.Converters;
import org.granite.messaging.amf.io.util.ClassGetter;
import org.granite.messaging.amf.io.util.MethodProperty;
import org.granite.messaging.amf.io.util.Property;
import org.granite.messaging.amf.io.util.externalizer.DefaultExternalizer;
import org.granite.messaging.annotations.Include;
import org.granite.messaging.persistence.AbstractExternalizablePersistentCollection;
import org.granite.messaging.persistence.ExternalizablePersistentList;
import org.granite.messaging.persistence.ExternalizablePersistentMap;
import org.granite.messaging.persistence.ExternalizablePersistentSet;
import org.granite.util.TypeUtil;

/**
 * @author William DRAI
 */
public class TopLinkExternalizer extends DefaultExternalizer {

	private static final Logger log = Logger.getLogger(TopLinkExternalizer.class);

    @Override
    public Object newInstance(String type, ObjectInput in)
        throws IOException, ClassNotFoundException, InstantiationException, InvocationTargetException, IllegalAccessException {

        // If type is not an entity (@Embeddable for example), we don't read initialized/detachedState
        // and we fall back to DefaultExternalizer behavior.
        Class<?> clazz = TypeUtil.forName(type);
        if (!isRegularEntity(clazz))
            return super.newInstance(type, in);

        // Read initialized flag.
        boolean initialized = ((Boolean)in.readObject()).booleanValue();
        
        // Read detached state.
        @SuppressWarnings("unused")
		String detachedState = (String)in.readObject();
        
        // New or initialized entity.
        if (initialized)
        	return super.newInstance(type, in);
        
        // Pseudo-proxy (uninitialized entity).
    	Object id = in.readObject();
    	if (id != null && (!clazz.isAnnotationPresent(IdClass.class) || !clazz.getAnnotation(IdClass.class).value().equals(id.getClass())))
    		throw new RuntimeException("Id for TopLink pseudo-proxy should be null or IdClass (" + type + ")");
    	
    	return new TopLinkValueHolder();
    }

    @Override
    public void readExternal(Object o, ObjectInput in) throws IOException, ClassNotFoundException, IllegalAccessException {
    	// Ignore TopLink proxies
    	if (o instanceof TopLinkValueHolder)
    		return;
    	
        // @Embeddable or others...
        if (!isRegularEntity(o.getClass())) {
        	log.debug("Delegating non regular entity reading to DefaultExternalizer...");
            super.readExternal(o, in);
        }
        // Regural @Entity or @MappedSuperclass
        else {
            ConvertersConfig config = GraniteContext.getCurrentInstance().getGraniteConfig();
            
            Converters converters = config.getConverters();
            ClassGetter classGetter = config.getClassGetter();
            Class<?> oClass = classGetter.getClass(o);
            ParameterizedType[] declaringTypes = TypeUtil.getDeclaringTypes(oClass);

            List<Property> fields = findOrderedFields(oClass);
            log.debug("Reading entity %s with fields %s", oClass.getName(), fields);
            Map<String, Property> topLinkFields = new HashMap<String, Property>();
            for (Property field : fields) {
                if (field.getType() instanceof Class<?> && ValueHolderInterface.class.isAssignableFrom((Class<?>)field.getType())) {
                    topLinkFields.put(field.getName(), field);
                }
                else {
                    Object value = in.readObject();

                    if (value instanceof ValueHolderInterface) {
                        topLinkFields.get("_toplink_" + field.getName() + "_vh").setValue(o, value, false);
                    }
                    else if (!(field instanceof MethodProperty && field.isAnnotationPresent(Include.class, true))) { 
                        if (value instanceof AbstractExternalizablePersistentCollection)
                            value = newIndirectCollection((AbstractExternalizablePersistentCollection)value, field.getType());
                        else {
                        	Type targetType = TypeUtil.resolveTypeVariable(field.getType(), field.getDeclaringClass(), declaringTypes);
                            value = converters.convert(value, targetType);
                        }
                        
                        field.setValue(o, value, false);
                    }
                }
            }
        }
    }
    
    protected IndirectContainer newIndirectCollection(AbstractExternalizablePersistentCollection value, Type target) {
    	final boolean initialized = value.isInitialized();
		final Object[] content = value.getContent();
    	
		IndirectContainer coll = null;
		if (value instanceof ExternalizablePersistentSet) {
            if (initialized) {
                if (content != null) {
                    Set<?> set = ((ExternalizablePersistentSet)value).getContentAsSet(target);
                    coll = new IndirectSet(set);
                }
            } 
            else
                coll = new IndirectSet();
    	}
		else if (value instanceof ExternalizablePersistentList) {
	        if (initialized) {
	            if (content != null) {
                    List<?> list = ((ExternalizablePersistentList)value).getContentAsList(target);
	                coll = new IndirectList(list);
	            }
	        }
	        else
	            coll = new IndirectList();
		}
		else if (value instanceof ExternalizablePersistentMap) {
	    	if (initialized) {
	            if (content != null) {
                    Map<?, ?> map = ((ExternalizablePersistentMap)value).getContentAsMap(target);
	                coll = new IndirectMap(map);
	            }
	        }
	    	else
	            coll = new IndirectMap();
		}
		else {
			throw new RuntimeException("Illegal externalizable persitent class: " + value);
		}
    	
    	return coll;
    }
    

    @Override
    public void writeExternal(Object o, ObjectOutput out) throws IOException, IllegalAccessException {

        ClassGetter classGetter = ((ConvertersConfig)GraniteContext.getCurrentInstance().getGraniteConfig()).getClassGetter();
        Class<?> oClass = classGetter.getClass(o);

        if (o instanceof TopLinkProxy) {        	
            TopLinkProxy proxy = (TopLinkProxy)o;

            // Only write initialized flag, null detachedState & null id if proxy is uninitialized.
        	log.debug("Writing uninitialized TopLink ValueHolder %s", proxy.getProxiedClass().getName());
        	
        	// Write initialized flag.
        	out.writeObject(Boolean.FALSE);
        	// Write detachedState.
            out.writeObject(null);
        	// Write id.
            out.writeObject(null);

            return;
        }

        if (!isRegularEntity(o.getClass())) { // @Embeddable or others...
        	log.debug("Delegating non regular entity writing to DefaultExternalizer...");
            super.writeExternal(o, out);
        }
        else {
        	// Write initialized flag.
        	out.writeObject(Boolean.TRUE);
        	// Write detachedState.
            out.writeObject(null);

            // Externalize entity fields.
            List<Property> fields = findOrderedFields(oClass);
            List<String> lazyFieldNames = new ArrayList<String>();
            
            log.debug("Writing entity %s with fields %s", o.getClass().getName(), fields);
            for (Property field : fields) {
                if (!(field.getType() instanceof Class<?> && ValueHolderInterface.class.isAssignableFrom((Class<?>)field.getType()))) {
                    if (lazyFieldNames.contains(field.getName())) {
                        TopLinkProxy proxy = new TopLinkProxy((Class<?>)field.getType());
                        out.writeObject(proxy);
                    }
                    else {
                        Object value = field.getValue(o);
        
                        // Persistent collections & maps.
                        if (value instanceof IndirectContainer)
                        	value = newExternalizableCollection((IndirectContainer)value);
                        // Transient maps.
                        else if (value instanceof Map<?, ?>)
                        	value = BasicMap.newInstance((Map<?, ?>)value);
                        
                        out.writeObject(value);
                    }
                }
                else {
                    ValueHolderInterface vh = (ValueHolderInterface)field.getValue(o);
                    if (!vh.isInstantiated())
                        lazyFieldNames.add(field.getName().substring("_toplink_".length(), field.getName().length()-3));
                }
            }
        }
    }
    
    protected AbstractExternalizablePersistentCollection newExternalizableCollection(IndirectContainer value) {
    	AbstractExternalizablePersistentCollection coll = null;
    	boolean initialized = value.isInstantiated();
        
    	if (value instanceof IndirectSet) {
            coll = new ExternalizablePersistentSet(initialized ? ((IndirectSet)value).toArray() : null, initialized, false);
        }
        else if (value instanceof IndirectList) {
            coll = new ExternalizablePersistentList(initialized ? ((IndirectList)value).toArray() : null, initialized, false);
        }
        else if (value instanceof IndirectMap) {
            Object[] content = null;
            
            if (initialized) {
            	content = new Object[((IndirectMap)value).size()];
                int index = 0;
                @SuppressWarnings("unchecked")
                Set<Map.Entry<?, ?>> entries = ((IndirectMap)value).entrySet();
                for (Map.Entry<?, ?> entry : entries)
                    content[index++] = new Object[]{entry.getKey(), entry.getValue()};
            }
            
            coll = new ExternalizablePersistentMap(content, initialized, false);
        }
        else {
            throw new UnsupportedOperationException("Unsupported TopLink collection type: " + value);
        }
        
        return coll;
    }

    
    @Override
    public int accept(Class<?> clazz) {
        return (
            clazz.isAnnotationPresent(Entity.class) ||
            clazz.isAnnotationPresent(MappedSuperclass.class) ||
            clazz.isAnnotationPresent(Embeddable.class)
        ) ? 1 : -1;
    }

    protected boolean isRegularEntity(Class<?> clazz) {
        return clazz.isAnnotationPresent(Entity.class) || clazz.isAnnotationPresent(MappedSuperclass.class);
    }
}
