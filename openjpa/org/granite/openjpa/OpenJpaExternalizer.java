/*
  GRANITE DATA SERVICES
  Copyright (C) 2011 GRANITE DATA SERVICES S.A.S.

  This file is part of Granite Data Services.

  Granite Data Services is free software; you can redistribute it and/or modify
  it under the terms of the GNU Library General Public License as published by
  the Free Software Foundation; either version 2 of the License, or (at your
  option) any later version.

  Granite Data Services is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
  for more details.

  You should have received a copy of the GNU Library General Public License
  along with this library; if not, see <http://www.gnu.org/licenses/>.
*/

package org.granite.openjpa;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.IdClass;
import javax.persistence.MappedSuperclass;

import org.apache.openjpa.enhance.PersistenceCapable;
import org.apache.openjpa.kernel.OpenJPAStateManager;
import org.apache.openjpa.util.ProxyCollection;
import org.apache.openjpa.util.ProxyMap;
import org.granite.collections.BasicMap;
import org.granite.config.GraniteConfig;
import org.granite.context.GraniteContext;
import org.granite.logging.Logger;
import org.granite.messaging.amf.io.convert.Converters;
import org.granite.messaging.amf.io.util.ClassGetter;
import org.granite.messaging.amf.io.util.MethodProperty;
import org.granite.messaging.amf.io.util.Property;
import org.granite.messaging.amf.io.util.externalizer.DefaultExternalizer;
import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.granite.messaging.persistence.AbstractExternalizablePersistentCollection;
import org.granite.messaging.persistence.ExternalizablePersistentList;
import org.granite.messaging.persistence.ExternalizablePersistentMap;
import org.granite.messaging.persistence.ExternalizablePersistentSet;
import org.granite.util.ClassUtil;
import org.granite.util.StringUtil;

/**
 * @author Franck WOLFF
 */
public class OpenJpaExternalizer extends DefaultExternalizer {

	private static final Logger log = Logger.getLogger(OpenJpaExternalizer.class);

    @Override
    public Object newInstance(String type, ObjectInput in)
        throws IOException, ClassNotFoundException, InstantiationException, InvocationTargetException, IllegalAccessException {

        // If type is not an entity (@Embeddable for example), we don't read initialized/detachedState
        // and we fall back to DefaultExternalizer behavior.
        Class<?> clazz = ClassUtil.forName(type);
        if (!isRegularEntity(clazz))
            return super.newInstance(type, in);
        
        // Read initialized flag.
        boolean initialized = ((Boolean)in.readObject()).booleanValue();

        // Read detached state...
        String detachedState = (String)in.readObject();
        
        // Pseudo-proxy (uninitialized entity).
        if (!initialized) {
        	Object id = in.readObject();
        	if (id != null && (!clazz.isAnnotationPresent(IdClass.class) || !clazz.getAnnotation(IdClass.class).value().equals(id.getClass())))
        		throw new RuntimeException("Id for OpenJPA pseudo-proxy should be null or IdClass (" + type + ")");
        	return null;
        }
        
        // New entity.
        if (detachedState == null)
        	return super.newInstance(type, in);

        // Existing entity.
		Object entity = clazz.newInstance();
		if (detachedState.length() > 0) {
	        byte[] data = StringUtil.hexStringToBytes(detachedState);
			((PersistenceCapable)entity).pcSetDetachedState(deserializeDetachedState(data));
		}
		return entity;
    }

    @Override
    public void readExternal(Object o, ObjectInput in) throws IOException, ClassNotFoundException, IllegalAccessException {

        if (!isRegularEntity(o.getClass()) && !isEmbeddable(o.getClass())) {
        	log.debug("Delegating non regular entity reading to DefaultExternalizer...");
            super.readExternal(o, in);
        }
        // Regular @Entity or @MappedSuperclass
        else {
            GraniteConfig config = GraniteContext.getCurrentInstance().getGraniteConfig();

            Converters converters = config.getConverters();
            ClassGetter classGetter = config.getClassGetter();
            Class<?> oClass = classGetter.getClass(o);

            List<Property> fields = findOrderedFields(oClass);
            log.debug("Reading entity %s with fields %s", oClass.getName(), fields);
            for (Property field : fields) {
                Object value = in.readObject();
                
                if (!(field instanceof MethodProperty && field.isAnnotationPresent(ExternalizedProperty.class))) {
                	
                	// (Un)Initialized collections/maps.
                	if (value instanceof AbstractExternalizablePersistentCollection) {
                		// Uninitialized.
                		if (!((AbstractExternalizablePersistentCollection)value).isInitialized())
                			value = null;
                		// Initialized.
                		else {
	                		if (value instanceof ExternalizablePersistentSet)
	                			value = ((ExternalizablePersistentSet)value).getContentAsSet(field.getType());
	                		else if (value instanceof ExternalizablePersistentMap)
	                			value = ((ExternalizablePersistentMap)value).getContentAsMap(field.getType());
	                		else
	                			value = ((ExternalizablePersistentList)value).getContentAsList(field.getType());
                		}
                	}
                	// Others...
                	else
                		value = converters.convert(value, field.getType());
                    
                	field.setProperty(o, value, false);
                }
            }
        }
    }

    @Override
    public void writeExternal(Object o, ObjectOutput out) throws IOException, IllegalAccessException {

        ClassGetter classGetter = GraniteContext.getCurrentInstance().getGraniteConfig().getClassGetter();
        Class<?> oClass = classGetter.getClass(o);

        if (!isRegularEntity(o.getClass()) && !isEmbeddable(o.getClass())) { // @Embeddable or others...
        	log.debug("Delegating non regular entity writing to DefaultExternalizer...");
            super.writeExternal(o, out);
        }
        else {
        	PersistenceCapable pco = (PersistenceCapable)o;
        	
        	if (isRegularEntity(o.getClass())) {
	        	// Pseudo-proxy created for uninitialized entities (see below).
	        	if (Boolean.FALSE.equals(pco.pcGetDetachedState())) {
	            	// Write uninitialized flag.
	            	out.writeObject(Boolean.FALSE);
	            	// Write detached state.
	        		out.writeObject(null);
	        		// Write id.
	        		out.writeObject(null);
	        		return;
	        	}
	
	        	// Write initialized flag.
	        	out.writeObject(Boolean.TRUE);
	
	        	// Write detached state as a String, in the form of an hex representation
	        	// of the serialized detached state.
	        	byte[] detachedState = serializeDetachedState(pco);
	        	char[] hexDetachedState = StringUtil.bytesToHexChars(detachedState);
	            out.writeObject(new String(hexDetachedState));
        	}

            // Externalize entity fields.
            List<Property> fields = findOrderedFields(oClass);
        	Map<String, Boolean> loadedState = getLoadedState(pco, oClass);
            log.debug("Writing entity %s with fields %s", o.getClass().getName(), fields);
            for (Property field : fields) {
                Object value = field.getProperty(o);
                
                // Uninitialized associations.
                if (value == null && loadedState.containsKey(field.getName()) && !loadedState.get(field.getName())) {
            		Class<?> fieldClass = ClassUtil.classOfType(field.getType());
        			
            		// Create a "pseudo-proxy" for uninitialized entities: detached state is set to
            		// Boolean.FALSE (uninitialized flag).
            		if (PersistenceCapable.class.isAssignableFrom(fieldClass)) {
            			try {
            				value = fieldClass.newInstance();
            			} catch (Exception e) {
	                		throw new RuntimeException("Could not create OpenJPA pseudo-proxy for: " + field, e);
	                	}
            			((PersistenceCapable)value).pcSetDetachedState(Boolean.FALSE);
            		}
            		// Create pseudo-proxy for collections (set or list).
            		else if (Collection.class.isAssignableFrom(fieldClass)) {
            			if (Set.class.isAssignableFrom(fieldClass))
            				value = new ExternalizablePersistentSet((Object[])null, false, false);
            			else
            				value = new ExternalizablePersistentList((Object[])null, false, false);
            		}
            		// Create pseudo-proxy for maps.
            		else if (Map.class.isAssignableFrom(fieldClass)) {
            			value = new ExternalizablePersistentMap((Object[])null, false, false);
            		}
                }
                
                // Initialized collections.
                else if (value instanceof ProxyCollection) {
                	if (value instanceof Set<?>)
                		value = new ExternalizablePersistentSet(((ProxyCollection)value).toArray(), true, false);
                	else
                		value = new ExternalizablePersistentList(((ProxyCollection)value).toArray(), true, false);
                }
                
                // Initialized maps.
                else if (value instanceof ProxyMap) {
                	value = new ExternalizablePersistentMap((Object[])null, true, false);
                	((ExternalizablePersistentMap)value).setContentFromMap((ProxyMap)value);
                }
                
                // Transient maps.
                else if (value instanceof Map<?, ?>)
                	value = BasicMap.newInstance((Map<?, ?>)value);
                
                out.writeObject(value);
            }
        }
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
        return PersistenceCapable.class.isAssignableFrom(clazz) && (
        	clazz.isAnnotationPresent(Entity.class) || clazz.isAnnotationPresent(MappedSuperclass.class)
        );
    }

    protected boolean isEmbeddable(Class<?> clazz) {
        return PersistenceCapable.class.isAssignableFrom(clazz) && clazz.isAnnotationPresent(Embeddable.class);
    }
    
    // Very hacky!
    static Map<String, Boolean> getLoadedState(PersistenceCapable pc, Class<?> clazz) {
    	try {
    		BitSet loaded = null;
    		if (pc.pcGetStateManager() instanceof OpenJPAStateManager) {
		        OpenJPAStateManager sm = (OpenJPAStateManager)pc.pcGetStateManager();
		        loaded = sm.getLoaded();
    		}
    		// State manager may be null for some entities...
    		if (loaded == null) {
    			Object ds = pc.pcGetDetachedState();
    			if (ds != null && ds.getClass().isArray()) {
    				Object[] dsa = (Object[])ds;
    				if (dsa.length > 1 && dsa[1] instanceof BitSet)
    					loaded = (BitSet)dsa[1];
    			}
    		}
	    	
	        List<String> fieldNames = new ArrayList<String>();
	    	for (Class<?> c = clazz; c != null && PersistenceCapable.class.isAssignableFrom(c); c = c.getSuperclass()) { 
	        	Field pcFieldNames = c.getDeclaredField("pcFieldNames");
	        	pcFieldNames.setAccessible(true);
	        	fieldNames.addAll(0, Arrays.asList((String[])pcFieldNames.get(null)));
	    	}
	        
	    	Map<String, Boolean> loadedState = new HashMap<String, Boolean>();
	    	for (int i = 0; i < fieldNames.size(); i++)
	    		loadedState.put(fieldNames.get(i), (loaded != null && loaded.size() > i ? loaded.get(i) : true));
	    	return loadedState;
    	}
    	catch (Exception e) {
    		throw new RuntimeException("Could not get loaded state for: " + pc);
    	}
    }
    
    protected byte[] serializeDetachedState(PersistenceCapable pc) {
    	try {
	    	ByteArrayOutputStream baos = new ByteArrayOutputStream(256);
	        ObjectOutputStream oos = new ObjectOutputStream(baos);
	        oos.writeObject(pc.pcGetDetachedState());
	        return baos.toByteArray();
    	} catch (Exception e) {
    		throw new RuntimeException("Could not serialize detached state for: " + pc);
    	}
    }
    
    protected Object deserializeDetachedState(byte[] data) {
    	try {
	    	ByteArrayInputStream baos = new ByteArrayInputStream(data);
	        ObjectInputStream oos = new ObjectInputStream(baos);
	        return oos.readObject();
    	} catch (Exception e) {
    		throw new RuntimeException("Could not deserialize detached state for: " + data);
    	}
    }
}
