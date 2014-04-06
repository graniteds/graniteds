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
package org.granite.generator.as3.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.granite.generator.as3.As3Type;
import org.granite.generator.as3.ClientType;
import org.granite.generator.as3.reflect.JavaMethod.MethodType;
import org.granite.messaging.annotations.Exclude;
import org.granite.messaging.annotations.Include;
import org.granite.tide.annotations.TideEvent;
import org.granite.util.ClassUtil;
import org.granite.util.ClassUtil.DeclaredAnnotation;
import org.granite.util.PropertyDescriptor;

/**
 * @author Franck WOLFF
 */
public class JavaBean extends JavaAbstractType {

    ///////////////////////////////////////////////////////////////////////////
    // Fields.

    protected final Set<JavaImport> imports = new HashSet<JavaImport>();

    protected final JavaType superclass;
    protected final ClientType clientSuperclass;

    protected final List<JavaInterface> interfaces;
    protected final List<JavaProperty> interfacesProperties;

    protected final Map<String, JavaProperty> properties;
    protected final JavaProperty uid;
    protected final List<JavaProperty> lazyProperties;

    ///////////////////////////////////////////////////////////////////////////
    // Constructor.

    public JavaBean(JavaTypeFactory provider, Class<?> type, URL url) {
        super(provider, type, url);

        // Find superclass (controller filtered).
        this.superclass = provider.getJavaTypeSuperclass(type);
        if (this.superclass == null && type.isAnnotationPresent(TideEvent.class))
        	clientSuperclass = new As3Type("org.granite.tide.events", "AbstractTideEvent");
        else
        	clientSuperclass = null;

        // Find implemented interfaces (filtered by the current transformer).
        this.interfaces = Collections.unmodifiableList(provider.getJavaTypeInterfaces(type));

        // Collect bean properties.
        Map<String, JavaProperty> properties = new LinkedHashMap<String, JavaProperty>();
        
        if (this.superclass == null) {
        	// Collect bean properties of non generated superclasses if necessary
        	// Example: com.myapp.AbstractEntity extends org.springframework.data.jpa.model.AbstractPersistable<T>
        	// gsup collects the superclass parameterized type so the type parameter can be translated to an actual type later
        	List<Class<?>> superclasses = new ArrayList<Class<?>>();
        	Type gsup = type.getGenericSuperclass();
        	Class<?> c = type.getSuperclass();
        	while (c.getGenericSuperclass() != null && !c.getName().equals(Object.class.getName())) {
        		superclasses.add(0, c);
        		c = c.getSuperclass();
        	}
        	
        	for (Class<?> sc : superclasses)
        		properties.putAll(initProperties(sc, gsup instanceof ParameterizedType ? (ParameterizedType)gsup : null));
        }
        
        properties.putAll(initProperties());

        this.properties = Collections.unmodifiableMap(properties);

        List<JavaProperty> tmpLazyProperties = new ArrayList<JavaProperty>();
        for (JavaProperty property : properties.values()) {
            if (provider.isLazy(property))
            	tmpLazyProperties.add(property);
        }
        this.lazyProperties = (tmpLazyProperties.isEmpty() ? null : Collections.unmodifiableList(tmpLazyProperties));

        // Collect properties from superclasses.
        Map<String, JavaProperty> allProperties = new HashMap<String, JavaProperty>(this.properties);
        for (JavaType supertype = this.superclass; supertype instanceof JavaBean; supertype = ((JavaBean)supertype).superclass)
        	allProperties.putAll(((JavaBean)supertype).properties);

        // Collect properties from interfaces.
        Map<String, JavaProperty> iPropertyMap = new HashMap<String, JavaProperty>();
        addImplementedInterfacesProperties(interfaces, iPropertyMap, allProperties);
        this.interfacesProperties = getSortedUnmodifiableList(iPropertyMap.values());

        // Find uid (if any).
        JavaProperty tmpUid = null;
        for (JavaProperty property : properties.values()) {
            if (provider.isUid(property)) {
                tmpUid = property;
                break;
            }
        }
        this.uid = tmpUid;

        // Collect imports.
        if (superclass != null)
            addToImports(provider.getJavaImport(superclass.getType()));
        for (JavaInterface interfaze : interfaces)
            addToImports(provider.getJavaImport(interfaze.getType()));
        for (JavaProperty property : properties.values())
            addToImports(provider.getJavaImports(property.getClientType(), true));
    }

    private void addImplementedInterfacesProperties(List<JavaInterface> interfaces, Map<String, JavaProperty> iPropertyMap, Map<String, JavaProperty> allProperties) {
    	for (JavaInterface interfaze : interfaces) {
    		for (JavaProperty property : interfaze.getProperties()) {
    		    String name = property.getName();
    		    if (!iPropertyMap.containsKey(name) && !allProperties.containsKey(name))
    		    	iPropertyMap.put(name, property);
    		}
    		addImplementedInterfacesProperties(interfaze.interfaces, iPropertyMap, allProperties);
        }
	}

	///////////////////////////////////////////////////////////////////////////
    // Properties.

    public Set<JavaImport> getImports() {
        return imports;
    }
    protected void addToImports(JavaImport javaImport) {
        if (javaImport != null)
            imports.add(javaImport);
    }
    protected void addToImports(Set<JavaImport> javaImports) {
        if (javaImports != null)
            imports.addAll(javaImports);
    }

    public boolean hasSuperclass() {
        return superclass != null;
    }
    public JavaType getSuperclass() {
        return superclass;
    }
    public ClientType getAs3Superclass() {
    	return clientSuperclass;
    }
    public ClientType getClientSuperclass() {
    	return clientSuperclass;
    }

    public boolean hasInterfaces() {
        return interfaces != null && !interfaces.isEmpty();
    }
    public List<JavaInterface> getInterfaces() {
        return interfaces;
    }

    public boolean hasInterfacesProperties() {
        return interfacesProperties != null && !interfacesProperties.isEmpty();
    }
    public List<JavaProperty> getInterfacesProperties() {
        return interfacesProperties;
    }

    public Collection<JavaProperty> getProperties() {
        return properties.values();
    }
    
    public boolean isAnnotationPresent(Class<? extends Annotation> annotation) {
    	return type.isAnnotationPresent(annotation);
    }

    public boolean hasUid() {
        return uid != null;
    }
    public JavaProperty getUid() {
        return uid;
    }
    
    public boolean isLazy(JavaProperty property) {
    	return lazyProperties != null && lazyProperties.contains(property);
    }
    
    public boolean hasEnumProperty() {
    	for (JavaProperty property : properties.values()) {
    		if (property.isEnum())
    			return true;
    	}
    	return false;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Utilities.

    protected SortedMap<String, JavaProperty> initProperties() {
    	return initProperties(type, null);
    }
    
    protected SortedMap<String, JavaProperty> initProperties(Class<?> type, ParameterizedType parentGenericType) {
        PropertyDescriptor[] propertyDescriptors = getPropertyDescriptors(type);
        SortedMap<String, JavaProperty> propertyMap = new TreeMap<String, JavaProperty>();

        // Standard declared fields.
        for (Field field : type.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers()) &&
            	!Modifier.isTransient(field.getModifiers()) &&
            	!"jdoDetachedState".equals(field.getName()) &&	// Specific for JDO statically enhanced classes
            	!field.isAnnotationPresent(Exclude.class)) {

            	String name = field.getName();
                JavaMethod readMethod = null;
                JavaMethod writeMethod = null;
                
                if (field.getType().isMemberClass() && !field.getType().isEnum())
                	throw new UnsupportedOperationException("Inner classes are not supported (except enums): " + field.getType());

                if (propertyDescriptors != null) {
                    for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
                        if (name.equals(propertyDescriptor.getName())) {
                            if (propertyDescriptor.getReadMethod() != null)
                                readMethod = new JavaMethod(propertyDescriptor.getReadMethod(), MethodType.GETTER);
                            if (propertyDescriptor.getWriteMethod() != null)
                                writeMethod = new JavaMethod(propertyDescriptor.getWriteMethod(), MethodType.SETTER);
                            break;
                        }
                    }
                }

                JavaFieldProperty property = new JavaFieldProperty(provider, field, readMethod, writeMethod, parentGenericType);
                propertyMap.put(name, property);
            }
        }

        // Getter annotated by @ExternalizedProperty.
        if (propertyDescriptors != null) {
            for (PropertyDescriptor property : propertyDescriptors) {
            	Method getter = property.getReadMethod();
                if (getter != null &&
                	getter.getDeclaringClass().equals(type) &&
                    !propertyMap.containsKey(property.getName())) {
                    
                	DeclaredAnnotation<Include> annotation = ClassUtil.getAnnotation(getter, Include.class);
                	if (annotation == null || (annotation.declaringClass != type && !annotation.declaringClass.isInterface()))
                		continue;

                    JavaMethod readMethod = new JavaMethod(getter, MethodType.GETTER);
                    JavaMethodProperty methodProperty = new JavaMethodProperty(provider, property.getName(), readMethod, null, parentGenericType);
                    propertyMap.put(property.getName(), methodProperty);
                }
            }
        }

        return propertyMap;
    }
}
