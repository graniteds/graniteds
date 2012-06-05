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

package org.granite.datanucleus;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.jdo.annotations.EmbeddedOnly;
import javax.jdo.annotations.Extension;
import javax.jdo.spi.Detachable;
import javax.jdo.spi.PersistenceCapable;
import javax.jdo.spi.StateManager;
import javax.persistence.Version;

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
import org.granite.util.Reflections;
import org.granite.util.StringUtil;


/**
 * @author Stephen MORE
 * @author William DRAI
 */
@SuppressWarnings("unchecked")
public class DataNucleusExternalizer extends DefaultExternalizer {

	private static final Logger log = Logger.getLogger(DataNucleusExternalizer.class);
	
	private static final Integer NULL_ID = Integer.valueOf(0);
	
	private static boolean jpaEnabled;
	private static Class<? extends Annotation> entityAnnotation;
	private static Class<? extends Annotation> mappedSuperClassAnnotation;
	private static Class<? extends Annotation> embeddableAnnotation;
	private static Class<? extends Annotation> idClassAnnotation;
	static {
		try {
			ClassLoader cl = DataNucleusExternalizer.class.getClassLoader();
			entityAnnotation = (Class<? extends Annotation>)cl.loadClass("javax.persistence.Entity");
			mappedSuperClassAnnotation = (Class<? extends Annotation>)cl.loadClass("javax.persistence.MappedSuperclass");
			embeddableAnnotation = (Class<? extends Annotation>)cl.loadClass("javax.persistence.Embeddable");
			idClassAnnotation = (Class<? extends Annotation>)cl.loadClass("javax.persistence.IdClass");
			jpaEnabled = true;
		}
		catch (Exception e) {
			// JPA not present
			entityAnnotation = null;
			mappedSuperClassAnnotation = null;
			embeddableAnnotation = null;
			idClassAnnotation = null;
			jpaEnabled = false;
		}
	}
	

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

        // Read detachedState.
        String detachedState = (String)in.readObject();
        
        // New entity.
        if (initialized && detachedState == null)
        	return super.newInstance(type, in);
        
        // Pseudo-proxy (uninitialized entity).
        if (!initialized) {
        	Object id = in.readObject();
        	if (id != null && jpaEnabled) {
        		// Is there something similar for JDO ??
        		boolean error = !clazz.isAnnotationPresent(idClassAnnotation);
        		if (!error) {
        			Object idClass = clazz.getAnnotation(idClassAnnotation);
        			try {
	        			Method m = idClass.getClass().getMethod("value");
	        			error = !id.getClass().equals(m.invoke(idClass));
        			}
        			catch (Exception e) {
        				log.error(e, "Could not get idClass annotation value");
        				error = true;
        			}
        		}
        		if (error)
        			throw new RuntimeException("Id for DataNucleus pseudo-proxy should be null (" + type + ")");
        	}
        	return null;
        }
        
        // Existing entity.
		Object entity = clazz.newInstance();
		if (detachedState.length() > 0) {
	        byte[] data = StringUtil.hexStringToBytes(detachedState);
			deserializeDetachedState((Detachable)entity, data);
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
            Object[] detachedState = getDetachedState((Detachable)o);

            List<Property> fields = findOrderedFields(oClass, detachedState != null);
            log.debug("Reading entity %s with fields %s", oClass.getName(), fields);
            for (Property field : fields) {
                if (field.getName().equals("jdoDetachedState"))
                	continue;
                
                Object value = in.readObject();
                
                if (!(field instanceof MethodProperty && field.isAnnotationPresent(ExternalizedProperty.class, true))) {
                	
                	// (Un)Initialized collections/maps.
                	if (value instanceof AbstractExternalizablePersistentCollection)
                		value = newCollection((AbstractExternalizablePersistentCollection)value, field);
                	else
                		value = converters.convert(value, field.getType());
                    
                	field.setProperty(o, value, false);
                }
            }
        }
    }
    
    protected Object newCollection(AbstractExternalizablePersistentCollection value, Property field) {
    	final Type target = field.getType();
    	final boolean initialized = value.isInitialized();
		// final boolean dirty = value.isDirty();
		final Object[] content = value.getContent();
    	final boolean sorted = (
    		SortedSet.class.isAssignableFrom(ClassUtil.classOfType(target)) ||
    		SortedMap.class.isAssignableFrom(ClassUtil.classOfType(target))
    	);
    	
		Object coll = null;
		if (value instanceof ExternalizablePersistentSet) {
    		if (initialized) {
                if (content != null)
                	coll = ((ExternalizablePersistentSet)value).getContentAsSet(target);
            }
    		else
                coll = (sorted ? new TreeSet<Object>() : new HashSet<Object>());
    	}
		else if (value instanceof ExternalizablePersistentList) {
	        if (initialized) {
	            if (content != null)
	            	coll = ((ExternalizablePersistentList)value).getContentAsList(target);
	        }
	        else
	            coll = new ArrayList<Object>();
		}
		else if (value instanceof ExternalizablePersistentMap) {
	    	if (initialized) {
	            if (content != null)
	            	coll = ((ExternalizablePersistentMap)value).getContentAsMap(target);
	        }
	    	else
	            coll = (sorted ? new TreeMap<Object, Object>() : new HashMap<Object, Object>());
		}
		else {
			throw new RuntimeException("Illegal externalizable persitent class: " + value);
		}
    	
    	return coll;
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
        	Detachable pco = (Detachable)o;
        	preSerialize((PersistenceCapable)pco);
        	Object[] detachedState = getDetachedState(pco);
        	
        	if (isRegularEntity(o.getClass())) {        	
	        	// Pseudo-proxy created for uninitialized entities (see below).
	        	if (detachedState != null && detachedState[0] == NULL_ID) {
	            	// Write initialized flag.
	            	out.writeObject(Boolean.FALSE);
	            	// Write detached state.
	        		out.writeObject(null);
	        		// Write id.
	        		out.writeObject(null);
	        		return;
	        	}
	
	        	// Write initialized flag.
	        	out.writeObject(Boolean.TRUE);
	        	
	        	if (detachedState != null) {
	            	// Write detached state as a String, in the form of an hex representation
	            	// of the serialized detached state.
	    	        Object version = getVersion(pco);
	    	        if (version != null)
	    	        	detachedState[1] = version;
		        	byte[] binDetachedState = serializeDetachedState(detachedState);
		        	char[] hexDetachedState = StringUtil.bytesToHexChars(binDetachedState);
		            out.writeObject(new String(hexDetachedState));
	        	}
	        	else
	        		out.writeObject(null);
        	}

            // Externalize entity fields.
            List<Property> fields = findOrderedFields(oClass);
        	Map<String, Boolean> loadedState = getLoadedState(detachedState, oClass);
            log.debug("Writing entity %s with fields %s", o.getClass().getName(), fields);
            for (Property field : fields) {
                if (field.getName().equals("jdoDetachedState"))
                	continue;
                
                Object value = field.getProperty(o);
                if (isValueIgnored(value)) {
                	out.writeObject(null);
                	continue;
                }
                
                // Uninitialized associations.
                if (loadedState.containsKey(field.getName()) && !loadedState.get(field.getName())) {
            		Class<?> fieldClass = ClassUtil.classOfType(field.getType());
        			
            		// Create a "pseudo-proxy" for uninitialized entities: detached state is set to "0" (uninitialized flag).
            		if (Detachable.class.isAssignableFrom(fieldClass)) {
            			try {
            				value = fieldClass.newInstance();
            			} catch (Exception e) {
	                		throw new RuntimeException("Could not create DataNucleus pseudo-proxy for: " + field, e);
	                	}
            			setDetachedState((Detachable)value, new Object[] { NULL_ID, null, null, null });
            		}
            		// Create pseudo-proxy for collections (set or list).
            		else if (Collection.class.isAssignableFrom(fieldClass)) {
            			if (Set.class.isAssignableFrom(fieldClass))
            				value = new ExternalizablePersistentSet((Set<?>)null, false, false);
            			else
            				value = new ExternalizablePersistentList((List<?>)null, false, false);
            		}
            		// Create pseudo-proxy for maps.
            		else if (Map.class.isAssignableFrom(fieldClass)) {
            			value = new ExternalizablePersistentMap((Map<?, ?>)null, false, false);
            		}
                }
                
                // Initialized collections.
                else if (value instanceof Set<?>) {
            		value = new ExternalizablePersistentSet(((Set<?>)value).toArray(), true, false);
                }
            	else if (value instanceof List<?>) {
            		value = new ExternalizablePersistentList(((List<?>)value).toArray(), true, false);
            	}
                else if (value instanceof Map<?, ?>) {
                	value = new ExternalizablePersistentMap((Map<?, ?>)null, true, false);
                	((ExternalizablePersistentMap)value).setContentFromMap((Map<?, ?>)value);
                }
                out.writeObject(value);
            }
        }
    }

    @Override
    public int accept(Class<?> clazz) {
        return (
            clazz.isAnnotationPresent(entityAnnotation) ||
            clazz.isAnnotationPresent(mappedSuperClassAnnotation) ||
            clazz.isAnnotationPresent(embeddableAnnotation) ||
            clazz.isAnnotationPresent(javax.jdo.annotations.PersistenceCapable.class)
        ) ? 1 : -1;
    }

    protected boolean isRegularEntity(Class<?> clazz) {
    	if (jpaEnabled) {
	        return ((PersistenceCapable.class.isAssignableFrom(clazz) && Detachable.class.isAssignableFrom(clazz) && !clazz.isAnnotationPresent(EmbeddedOnly.class)) 
	        	|| clazz.isAnnotationPresent(entityAnnotation) || clazz.isAnnotationPresent(mappedSuperClassAnnotation))
	        	&& !(clazz.isAnnotationPresent(embeddableAnnotation));
    	}
        return PersistenceCapable.class.isAssignableFrom(clazz) && Detachable.class.isAssignableFrom(clazz) && !clazz.isAnnotationPresent(EmbeddedOnly.class);
    }
    
    protected boolean isEmbeddable(Class<?> clazz) {
    	if (jpaEnabled) {
	        return ((PersistenceCapable.class.isAssignableFrom(clazz) && Detachable.class.isAssignableFrom(clazz) && clazz.isAnnotationPresent(EmbeddedOnly.class)) 
	            || clazz.isAnnotationPresent(embeddableAnnotation))
	            && !(clazz.isAnnotationPresent(entityAnnotation) || clazz.isAnnotationPresent(mappedSuperClassAnnotation));
    	}
        return PersistenceCapable.class.isAssignableFrom(clazz) && Detachable.class.isAssignableFrom(clazz) && clazz.isAnnotationPresent(EmbeddedOnly.class);
    }

    @Override
    public List<Property> findOrderedFields(final Class<?> clazz, boolean returnSettersWhenAvailable) {
    	List<Property> orderedFields = super.findOrderedFields(clazz, returnSettersWhenAvailable);
    	if (clazz.isAnnotationPresent(EmbeddedOnly.class) || (jpaEnabled && clazz.isAnnotationPresent(embeddableAnnotation))) {
    		Iterator<Property> ifield = orderedFields.iterator();
    		while (ifield.hasNext()) {
    			Property field = ifield.next();
    			if (field.getName().equals("jdoDetachedState"))
    				ifield.remove();
    		}
    	}
    	return orderedFields;
    }
    
        
    private static void preSerialize(PersistenceCapable o) {
    	try {
    		Class<?> baseClass = o.getClass();
    		while (baseClass.getSuperclass() != Object.class &&
    			   baseClass.getSuperclass() != null &&
    			   PersistenceCapable.class.isAssignableFrom(baseClass.getSuperclass())) {
    			baseClass = baseClass.getSuperclass();
    		}
	    	Field f = baseClass.getDeclaredField("jdoStateManager");
	    	f.setAccessible(true);
	    	StateManager sm = (StateManager)f.get(o);
	    	if (sm != null) {
	    		setDetachedState((Detachable)o, null);
	    		sm.preSerialize(o);
	    	}
    	}
    	catch (Exception e) {
    		throw new RuntimeException("Cannot access jdoDetachedState for detached object", e);
    	}
    }
    
    private static Object[] getDetachedState(javax.jdo.spi.Detachable o) {
    	try {
    		Class<?> baseClass = o.getClass();
    		while (baseClass.getSuperclass() != Object.class && baseClass.getSuperclass() != null && PersistenceCapable.class.isAssignableFrom(baseClass.getSuperclass()))
    			baseClass = baseClass.getSuperclass();
	    	Field f = baseClass.getDeclaredField("jdoDetachedState");
	    	f.setAccessible(true);
	    	return (Object[])f.get(o);
    	}
    	catch (Exception e) {
    		throw new RuntimeException("Cannot access jdoDetachedState for detached object", e);
    	}
    }
    
    private static void setDetachedState(javax.jdo.spi.Detachable o, Object[] detachedState) {
    	try {
    		Class<?> baseClass = o.getClass();
    		while (baseClass.getSuperclass() != Object.class && baseClass.getSuperclass() != null && PersistenceCapable.class.isAssignableFrom(baseClass.getSuperclass()))
    			baseClass = baseClass.getSuperclass();
	    	Field f = baseClass.getDeclaredField("jdoDetachedState");
	    	f.setAccessible(true);
	    	f.set(o, detachedState);
    	}
    	catch (Exception e) {
    		throw new RuntimeException("Cannot access jdoDetachedState for detached object", e);
    	}
    }
    
    
    static Map<String, Boolean> getLoadedState(Detachable pc, Class<?> clazz) {
    	return getLoadedState(getDetachedState(pc), clazz);    	
    }
    
    static Map<String, Boolean> getLoadedState(Object[] detachedState, Class<?> clazz) {
    	try {
    		BitSet loaded = detachedState != null ? (BitSet)detachedState[2] : null;
	    	
	        List<String> fieldNames = new ArrayList<String>();
	    	for (Class<?> c = clazz; c != null && PersistenceCapable.class.isAssignableFrom(c); c = c.getSuperclass()) { 
	        	Field pcFieldNames = c.getDeclaredField("jdoFieldNames");
	        	pcFieldNames.setAccessible(true);
	        	fieldNames.addAll(0, Arrays.asList((String[])pcFieldNames.get(null)));
	    	}
	        
	    	Map<String, Boolean> loadedState = new HashMap<String, Boolean>();
	    	for (int i = 0; i < fieldNames.size(); i++)
	    		loadedState.put(fieldNames.get(i), (loaded != null && loaded.size() > i ? loaded.get(i) : true));
	    	return loadedState;
    	}
    	catch (Exception e) {
    		throw new RuntimeException("Could not get loaded state for: " + detachedState);
    	}
    }
    
    protected byte[] serializeDetachedState(Object[] detachedState) {
    	try {
	        // Force version
	    	ByteArrayOutputStream baos = new ByteArrayOutputStream(256);
	        ObjectOutputStream oos = new ObjectOutputStream(baos);
	        oos.writeObject(detachedState);
	        return baos.toByteArray();
    	} catch (Exception e) {
    		throw new RuntimeException("Could not serialize detached state for: " + detachedState);
    	}
    }
    
    protected void deserializeDetachedState(Detachable pc, byte[] data) {
    	try {
	    	ByteArrayInputStream baos = new ByteArrayInputStream(data);
	        ObjectInputStream oos = new ObjectInputStream(baos);
	        Object[] state = (Object[])oos.readObject();
	        setDetachedState(pc, state);
    	} catch (Exception e) {
    		throw new RuntimeException("Could not deserialize detached state for: " + data);
    	}
    }
    
    protected static Object getVersion(Object entity) {
		Class<?> entityClass = entity.getClass();
		
    	if (jpaEnabled && entityClass.isAnnotationPresent(entityAnnotation)) {
    	    for (Class<?> clazz = entityClass; clazz != Object.class; clazz = clazz.getSuperclass())  {
                for (Method method : clazz.getDeclaredMethods()) {
    			    if (method.isAnnotationPresent(Version.class)) {
    	    	    	return Reflections.invokeAndWrap(method, entity);
    			    }
    			}                
    	    }
    	    
            for (Class<?> clazz = entityClass; clazz != Object.class; clazz = clazz.getSuperclass())	{
            	for (Field field : clazz.getDeclaredFields()) {
            		if (field.isAnnotationPresent(Version.class)) {
            			if (!field.isAccessible())
            				field.setAccessible(true);
            	    	return Reflections.getAndWrap(field, entity);
            		}
               }
            }
    	    
    	    return null;
    	}
    	else if (!jpaEnabled && entity instanceof PersistenceCapable) {
    		if (entityClass.isAnnotationPresent(javax.jdo.annotations.Version.class)) {
    			javax.jdo.annotations.Version version = entityClass.getAnnotation(javax.jdo.annotations.Version.class);
    			for (Extension extension : version.extensions()) {
    				if (extension.vendorName().equals("datanucleus") && extension.key().equals("field-name")) {
    					String versionFieldName = extension.value();
    					
    					try {
    						Method versionGetter = entityClass.getMethod("get" + versionFieldName.substring(0, 1).toUpperCase() + versionFieldName.substring(1));
    						return Reflections.invokeAndWrap(versionGetter, entity);
    					}
    					catch (NoSuchMethodException e) {
    			            for (Class<?> clazz = entityClass; clazz != Object.class; clazz = clazz.getSuperclass())	{
    			            	for (Field field : clazz.getDeclaredFields()) {
    			            		if (field.getName().equals(versionFieldName)) {
    			            			if (!field.isAccessible())
    			            				field.setAccessible(true);
    	    			            	return Reflections.getAndWrap(field, entity);
    			            		}
    			               }
    			            }
    					}    					
    				} 
    				
    			}
    		}
    	}
    	
    	return null;
    }
}
