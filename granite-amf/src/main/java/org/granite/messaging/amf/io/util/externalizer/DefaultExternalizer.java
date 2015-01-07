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
package org.granite.messaging.amf.io.util.externalizer;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import org.granite.collections.BasicMap;
import org.granite.config.ConvertersConfig;
import org.granite.config.ExternalizersConfig;
import org.granite.context.GraniteContext;
import org.granite.logging.Logger;
import org.granite.messaging.amf.io.convert.Converters;
import org.granite.messaging.amf.io.util.FieldProperty;
import org.granite.messaging.amf.io.util.MethodProperty;
import org.granite.messaging.amf.io.util.Property;
import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedBean;
import org.granite.messaging.amf.io.util.instantiator.AbstractInstantiator;
import org.granite.messaging.annotations.Exclude;
import org.granite.messaging.annotations.Include;
import org.granite.util.Introspector;
import org.granite.util.PropertyDescriptor;
import org.granite.util.TypeUtil;
import org.granite.util.TypeUtil.DeclaredAnnotation;
import org.granite.util.XMap;

/**
 * @author Franck WOLFF
 */
public class DefaultExternalizer implements Externalizer {

	private static final Logger log = Logger.getLogger(DefaultExternalizer.class);
	protected static final byte[] BYTES_0 = new byte[0];
	
    private final ReentrantLock lock = new ReentrantLock();
    protected final ConcurrentHashMap<Class<?>, List<Property>> orderedFields =
        new ConcurrentHashMap<Class<?>, List<Property>>();
    protected final ConcurrentHashMap<Class<?>, List<Property>> orderedSetterFields =
        new ConcurrentHashMap<Class<?>, List<Property>>();
    protected final ConcurrentHashMap<String, Constructor<?>> constructors =
        new ConcurrentHashMap<String, Constructor<?>>();
    
    protected boolean dynamicClass = false;
    

    public void configure(XMap properties) {
    	if (properties != null) {
	    	String dynamicclass = properties.get("dynamic-class");
	    	if (Boolean.TRUE.toString().equalsIgnoreCase(dynamicclass))
	    		dynamicClass = true;
    	}
    }
    
    public Object newInstance(final String type, ObjectInput in)
        throws IOException, ClassNotFoundException, InstantiationException,
               InvocationTargetException, IllegalAccessException {

        Constructor<?> constructor = !dynamicClass ? constructors.get(type) : null;

        if (constructor == null) {
            Class<?> clazz = TypeUtil.forName(type);
            constructor = findDefaultConstructor(clazz);
            if (!dynamicClass) {
	            Constructor<?> previousConstructor = constructors.putIfAbsent(type, constructor);
	            if (previousConstructor != null)
	                constructor = previousConstructor; // Should be the same instance, anyway...
            }
        }

        return constructor.newInstance();
    }

    public void readExternal(Object o, ObjectInput in)
        throws IOException, ClassNotFoundException, IllegalAccessException {
    	
        if (o instanceof AbstractInstantiator<?>) {
            AbstractInstantiator<?> instantiator = (AbstractInstantiator<?>)o;
            List<String> fields = instantiator.getOrderedFieldNames();
            log.debug("Reading bean with instantiator %s with fields %s", instantiator.getClass().getName(), fields);
            for (String fieldName : fields)
                instantiator.put(fieldName, in.readObject());
        }
        else {
            List<Property> fields = findOrderedFields(o.getClass());
            log.debug("Reading bean %s with fields %s", o.getClass().getName(), fields);
            for (Property field : fields) {
                Object value = in.readObject();
                if (!(field instanceof MethodProperty && field.isAnnotationPresent(Include.class, true)))
                	field.setValue(o, value);
            }
        }
    }

    public void writeExternal(Object o, ObjectOutput out)
        throws IOException, IllegalAccessException {

        ExternalizersConfig config = GraniteContext.getCurrentInstance().getGraniteConfig();
        String instantiatorType = config.getInstantiator(o.getClass().getName());
        if (instantiatorType != null) {
            try {
                AbstractInstantiator<?> instantiator =
                    (AbstractInstantiator<?>)TypeUtil.newInstance(instantiatorType);
                List<String> fields = instantiator.getOrderedFieldNames();
                log.debug("Writing bean with instantiator %s with fields %s", instantiator.getClass().getName(), fields);
                for (String fieldName : fields) {
                    Field field = o.getClass().getDeclaredField(fieldName);
                    field.setAccessible(true);
                    out.writeObject(field.get(o));
                }
            } catch (Exception e) {
                throw new RuntimeException("Error with instantiatorType: " + instantiatorType, e);
            }
        }
        else {
            List<Property> fields = findOrderedFields(o.getClass());
            log.debug("Writing bean %s with fields %s", o.getClass().getName(), fields);
            for (Property field : fields) {
                Object value = field.getValue(o);
                if (value instanceof Map<?, ?>)
                    value = BasicMap.newInstance((Map<?, ?>)value);
                if (isValueIgnored(value))
                	out.writeObject(null);
                else
                	out.writeObject(value);
            }
        }
    }
    
    protected boolean isValueIgnored(Object value) {
    	return false;
    }

    public List<Property> findOrderedFields(final Class<?> clazz) {
    	return findOrderedFields(clazz, false);
    }
    
    public List<Property> findOrderedFields(final Class<?> clazz, boolean returnSettersWhenAvailable) {
        List<Property> fields = !dynamicClass ? (returnSettersWhenAvailable ? orderedSetterFields.get(clazz) : orderedFields.get(clazz)) : null;

        if (fields == null) {
        	if (dynamicClass)
        		Introspector.flushFromCaches(clazz);
            
        	PropertyDescriptor[] propertyDescriptors = TypeUtil.getProperties(clazz);
            Converters converters = ((ConvertersConfig)GraniteContext.getCurrentInstance().getGraniteConfig()).getConverters();

            fields = new ArrayList<Property>();

            Set<String> allFieldNames = new HashSet<String>();
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
                    	if (returnSettersWhenAvailable && propertyDescriptors != null) {
                    		for (PropertyDescriptor pd : propertyDescriptors) {
                    			if (pd.getName().equals(field.getName()) && pd.getWriteMethod() != null) {
                    				newFields.add(new MethodProperty(converters, field.getName(), pd.getWriteMethod(), pd.getReadMethod()));
                    				found = true;
                    				break;
                    			}
                    		}
                    	}
                		if (!found)
                    		newFields.add(new FieldProperty(converters, field));
                    }
                    allFieldNames.add(field.getName());
                }

                // Getter annotated  by @ExternalizedProperty.
                if (propertyDescriptors != null) {
                    for (PropertyDescriptor property : propertyDescriptors) {
                        Method getter = property.getReadMethod();
                        if (getter != null && !allFieldNames.contains(property.getName())) {
                            
                        	DeclaredAnnotation<Include> annotation = TypeUtil.getAnnotation(getter, Include.class);
                        	if (annotation == null || (annotation.declaringClass != c && !annotation.declaringClass.isInterface()))
                        		continue;

                            newFields.add(new MethodProperty(
                                converters,
                                property.getName(),
                                null,
                                getter
                            ));
                            allFieldNames.add(property.getName());
                        }
                    }
                }

                Collections.sort(newFields, new Comparator<Property>() {
                    public int compare(Property o1, Property o2) {
                        return o1.getName().compareTo(o2.getName());
                    }
                });

                fields.addAll(0, newFields);
            }

            if (!dynamicClass) {
	            List<Property> previousFields = (returnSettersWhenAvailable ? orderedSetterFields : orderedFields).putIfAbsent(clazz, fields);
	            if (previousFields != null)
	                fields = previousFields;
            }
        }

        return fields;
    }
    
    protected boolean isPropertyIgnored(Field field) {
    	return false;
    }

    protected boolean isPropertyIgnored(Method method) {
    	return false;
    }

    protected <T> Constructor<T> findDefaultConstructor(Class<T> clazz) {
        Constructor<T> constructor = null;

        GraniteContext context = GraniteContext.getCurrentInstance();
        ExternalizersConfig config = context.getGraniteConfig();
        String instantiator = config.getInstantiator(clazz.getName());
        if (instantiator != null) {
            try {
                Class<T> instantiatorClass = TypeUtil.forName(instantiator, clazz);
                constructor = instantiatorClass.getConstructor();
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(
                    "Could not load instantiator class: " + instantiator + " for: " + clazz.getName(), e
                );
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(
                    "Could not find default constructor in instantiator class: " + instantiator, e
                );
            }
        }
        else {
            try {
                constructor = clazz.getConstructor();
            } catch (NoSuchMethodException e) {
                // fall down...
            }

            if (constructor == null) {
                String key = DefaultConstructorFactory.class.getName();
                DefaultConstructorFactory factory = getDefaultConstructorFactory(context, key);
                constructor = factory.findDefaultConstructor(clazz);
            }
        }

        return constructor;
    }

    private DefaultConstructorFactory getDefaultConstructorFactory(
        GraniteContext context,
        String key) {

        lock.lock();
        try {
            DefaultConstructorFactory factory =
                (DefaultConstructorFactory)context.getApplicationMap().get(key);
            if (factory == null) {
                try {
                    factory = new SunDefaultConstructorFactory();
                } catch (Exception e) {
                    // fall down...
                }
                if (factory == null)
                    factory = new NoDefaultConstructorFactory();
                context.getApplicationMap().put(key, factory);
            }
            return factory;
        } finally {
            lock.unlock();
        }
    }

    public int accept(Class<?> clazz) {
        return clazz.isAnnotationPresent(ExternalizedBean.class) ? 0 : -1;
    }
}

interface DefaultConstructorFactory {
    public <T> Constructor<T> findDefaultConstructor(Class<T> clazz);
}

class NoDefaultConstructorFactory implements DefaultConstructorFactory {

    public <T> Constructor<T> findDefaultConstructor(Class<T> clazz) {
        throw new RuntimeException("Could not find default constructor in class: " + clazz);
    }
}

class SunDefaultConstructorFactory implements DefaultConstructorFactory {

    private final Object reflectionFactory;
    private final Method newConstructorForSerialization;

    public SunDefaultConstructorFactory() {
        try {
            Class<?> factoryClass = TypeUtil.forName("sun.reflect.ReflectionFactory");
            Method getReflectionFactory = factoryClass.getDeclaredMethod("getReflectionFactory");
            reflectionFactory = getReflectionFactory.invoke(null);
            newConstructorForSerialization = factoryClass.getDeclaredMethod(
                "newConstructorForSerialization",
                new Class[]{Class.class, Constructor.class}
            );
        } catch (Exception e) {
            throw new RuntimeException("Could not create Sun Factory", e);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> Constructor<T> findDefaultConstructor(Class<T> clazz) {
        try {
            Constructor<?> constructor = Object.class.getDeclaredConstructor();
            constructor = (Constructor<?>)newConstructorForSerialization.invoke(
                reflectionFactory,
                new Object[]{clazz, constructor}
            );
            constructor.setAccessible(true);
            return (Constructor<T>)constructor;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
