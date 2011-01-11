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

package org.granite.generator.as3.reflect;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.granite.generator.as3.reflect.JavaMethod.MethodType;

/**
 * @author Franck WOLFF
 */
public class JavaInterface extends JavaAbstractType {

    protected final Set<JavaType> imports;

    protected final List<JavaInterface> interfaces;

    protected final List<JavaProperty> properties;

    public JavaInterface(JavaTypeFactory provider, Class<?> type, URL url) {
        super(provider, type, url);

        if (!type.isInterface())
            throw new IllegalArgumentException("type should be an interface: " + type);

        // Find superclass (controller filtered).
        this.interfaces = Collections.unmodifiableList(provider.getJavaTypeInterfaces(type));

        // Collect bean properties.
        this.properties = getSortedUnmodifiableList(initProperties());

        // Collect imports.
        Set<JavaType> tmpImports = new HashSet<JavaType>();
        for (JavaInterface interfaze : interfaces)
        	tmpImports.add(provider.getJavaImport(interfaze.getType()));
        for (JavaProperty property : properties)
            tmpImports.add(provider.getJavaImport(property.getType()));
        this.imports = Collections.unmodifiableSet(removeNull(tmpImports));
    }

    public Set<JavaType> getImports() {
        return imports;
    }

    public boolean hasSuperInterfaces() {
        return interfaces != null && !interfaces.isEmpty();
    }
    public List<JavaInterface> getSuperInterfaces() {
        return interfaces;
    }

    public Collection<JavaProperty> getProperties() {
        return properties;
    }

    protected Collection<JavaProperty> initProperties() {
        // Find (non static, non transient) declared fields or getter/setter for interfaces.
        List<JavaProperty> properties = new ArrayList<JavaProperty>();
        for (PropertyDescriptor propertyDescriptor : getPropertyDescriptors(getType())) {
            String name = propertyDescriptor.getName();
            JavaMethod readMethod = null;
            JavaMethod writeMethod = null;

            Method method = propertyDescriptor.getReadMethod();
            if (method != null)
                readMethod = new JavaMethod(method, MethodType.GETTER);

            method = propertyDescriptor.getWriteMethod();
            if (method != null)
                writeMethod = new JavaMethod(method, MethodType.SETTER);

            if (readMethod != null || writeMethod != null)
                properties.add(new JavaMethodProperty(provider, name, readMethod, writeMethod));
        }
        return properties;
    }
}
