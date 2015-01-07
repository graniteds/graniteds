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
package org.granite.client.javafx;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javafx.beans.value.ObservableValue;

import org.granite.client.persistence.Id;
import org.granite.client.util.BeanUtil;
import org.granite.client.util.PropertyHolder;
import org.granite.config.ConvertersConfig;
import org.granite.context.GraniteContext;
import org.granite.logging.Logger;
import org.granite.messaging.amf.io.convert.Converters;
import org.granite.messaging.amf.io.util.FieldProperty;
import org.granite.messaging.amf.io.util.MethodProperty;
import org.granite.messaging.amf.io.util.Property;
import org.granite.messaging.amf.io.util.externalizer.DefaultExternalizer;
import org.granite.messaging.annotations.Exclude;
import org.granite.messaging.annotations.Include;
import org.granite.messaging.annotations.Serialized;

/**
 * @author William DRAI
 */
public class JavaFXExternalizer extends DefaultExternalizer {

	private static final Logger log = Logger.getLogger(JavaFXExternalizer.class);
	

    public List<Property> findOrderedFields(final Class<?> clazz, boolean returnSettersWhenAvailable) {
        List<Property> fields = orderedFields.get(clazz);

        if (fields == null) {
            PropertyDescriptor[] propertyDescriptors = BeanUtil.getProperties(clazz);
            Converters converters = ((ConvertersConfig)GraniteContext.getCurrentInstance().getGraniteConfig()).getConverters();

            fields = new ArrayList<Property>();

            Set<String> allFieldNames = new HashSet<String>();
            allFieldNames.add("__initialized__");
            allFieldNames.add("__detachedState__");
            allFieldNames.add("__handlerManager");
            
            for (Class<?> c = clazz; c != null; c = c.getSuperclass()) {

                List<Property> newFields = new ArrayList<Property>();
                
                // Standard declared fields.
                for (Field field : c.getDeclaredFields()) {
                    if (!allFieldNames.contains(field.getName()) &&
                        !Modifier.isTransient(field.getModifiers()) &&
                        !Modifier.isStatic(field.getModifiers()) &&
                        !isPropertyIgnored(field) &&
                        !field.isAnnotationPresent(Exclude.class)) {
                    	
                    	boolean found = false;
                    	if (propertyDescriptors != null) {
                    		for (PropertyDescriptor pd : propertyDescriptors) {
                    			if (pd.getName().equals(field.getName())) {
                    				newFields.add(new JavaFXProperty(converters, field.getName(), field, pd.getReadMethod(), pd.getWriteMethod()));
                    				found = true;
                    				break;
                    			}
                    		}
                    	}
                		if (!found) {
                		    if (ObservableValue.class.isAssignableFrom(field.getType()))
                		        newFields.add(new JavaFXProperty(converters, field.getName(), field, null, null));
                		    else
                		        newFields.add(new FieldProperty(converters, field));
                		}
                    }
                    allFieldNames.add(field.getName());
                }

                // Getter annotated  by @ExternalizedProperty.
                if (propertyDescriptors != null) {
                    for (PropertyDescriptor property : propertyDescriptors) {
                        Method getter = property.getReadMethod();
                        if (getter != null &&
                            getter.isAnnotationPresent(Include.class) &&
                            getter.getDeclaringClass().equals(c) &&
                            !allFieldNames.contains(property.getName())) {

                            newFields.add(new MethodProperty(converters, property.getName(), null, getter));
                            allFieldNames.add(property.getName());
                        }
                    }
                }
                
                if (c.isAnnotationPresent(Serialized.class) && c.getAnnotation(Serialized.class).propertiesOrder().length > 0) {
                	final List<String> propertiesOrder = Arrays.asList(c.getAnnotation(Serialized.class).propertiesOrder());
	                Collections.sort(newFields, new Comparator<Property>() {
	                    public int compare(Property o1, Property o2) {
	                        return propertiesOrder.indexOf(o1.getName()) - propertiesOrder.indexOf(o2.getName());
	                    }
	                });
                }
                else {
                	// Lexical order
	                Collections.sort(newFields, new Comparator<Property>() {
	                    public int compare(Property o1, Property o2) {
	                        return o1.getName().compareTo(o2.getName());
	                    }
	                });
                }
                
                fields.addAll(0, newFields);
            }

            List<Property> previousFields = (returnSettersWhenAvailable ? orderedSetterFields : orderedFields).putIfAbsent(clazz, fields);
            if (previousFields != null)
                fields = previousFields;
        }

        return fields;
    }    
    
    @Override
    public void readExternal(Object o, ObjectInput in) throws IOException, ClassNotFoundException, IllegalAccessException {
    	
    	Field initializedField = null;
    	Field detachedStateField = null;
    	Class<?> clazz = o.getClass();
    	while (clazz != null && clazz != Object.class) {
    		try {
    			initializedField = clazz.getDeclaredField("__initialized__");
    		}
    		catch (NoSuchFieldException e) {
    		}
    		try {
    			detachedStateField = clazz.getDeclaredField("__detachedState__");
    		}
    		catch (NoSuchFieldException e) {
    		}
    		clazz = clazz.getSuperclass();
    	}
    	
    	boolean initialized = true;
    	
    	if (initializedField != null && detachedStateField != null) {
	        // Read initialized flag.
	        initialized = ((Boolean)in.readObject()).booleanValue();
	
	        // Read detachedState.
	        String detachedState = (String)in.readObject();
	        
	        initializedField.setAccessible(true);
	        initializedField.set(o, initialized);
	        detachedStateField.setAccessible(true);
	        detachedStateField.set(o, detachedState);
    	}
        
    	if (initialized) {
            List<Property> fields = findOrderedFields(o.getClass(), false);
            log.debug("Reading entity %s with fields %s", o.getClass().getName(), fields);
            for (Property field : fields) {
                Object value = in.readObject();
                field.setValue(o, value, true);
            }
        }
    	else {
            List<Property> fields = findOrderedFields(o.getClass(), false);
            log.debug("Reading entity %s with fields %s", o.getClass().getName(), fields);
            for (Property field : fields) {
            	if (!field.isAnnotationPresent(Id.class))
            		continue;
                Object value = in.readObject();
                field.setValue(o, value, true);
            }    		
    	}
    }

    @Override
    public void writeExternal(Object o, ObjectOutput out) throws IOException, IllegalAccessException {
    	
    	boolean initialized = true;
    	String detachedState = null;

    	Field initializedField = null;
    	Field detachedStateField = null;
    	Class<?> clazz = o.getClass();
    	while (clazz != null && clazz != Object.class) {
    		try {
    			initializedField = clazz.getDeclaredField("__initialized__");
    			initializedField.setAccessible(true);
    			initialized = initializedField.getBoolean(o);
    		}
    		catch (NoSuchFieldException e) {
    		}
    		try {
    			detachedStateField = clazz.getDeclaredField("__detachedState__");
    			detachedStateField.setAccessible(true);
    			detachedState = (String)detachedStateField.get(o);
    		}
    		catch (NoSuchFieldException e) {
    		}
    		clazz = clazz.getSuperclass();
    	}
    	
    	if (initializedField != null && detachedStateField != null) {
    		if (!initialized) {
	        	// Write initialized flag.
	        	out.writeObject(Boolean.FALSE);
	        	// Write detachedState.
	        	out.writeObject(detachedState);
	        	
	        	for (Property field : findOrderedFields(o.getClass(), false)) {
	        		if (field.isAnnotationPresent(Id.class)) {
		            	// Write entity id.
		                out.writeObject(field.getValue(o));
		                break;
	        		}
	        	}
	            return;
    		}
        	
        	// Write initialized flag.
        	out.writeObject(Boolean.TRUE);
        	// Write detachedState.
        	out.writeObject(detachedState);
    	}
    	
        // Externalize entity fields.
        List<Property> fields = findOrderedFields(o.getClass(), false);
        log.debug("Writing entity %s with fields %s", o.getClass().getName(), fields);
        for (Property field : fields) {
            Object value = field.getValue(o);
            
            if (value != null && value instanceof PropertyHolder)
            	value = ((PropertyHolder)value).getObject();
            
            if (isValueIgnored(value))
            	out.writeObject(null);
            else
            	out.writeObject(value);
        }
    }
}
