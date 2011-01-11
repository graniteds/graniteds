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

package org.granite.messaging.amf.io.util;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Franck WOLFF
 */
public class DefaultGroovyClassDescriptor extends JavaClassDescriptor {

    public DefaultGroovyClassDescriptor(Class<?> type) {
        super(type);
    }

    @Override
    protected List<Property> introspectProperties() {
        List<Property> properties = null;
        Class<?> type = getType();

        if (!isExternalizable() && !Map.class.isAssignableFrom(type) && !Hashtable.class.isAssignableFrom(type)) {
            properties = new ArrayList<Property>();

            try {
                Set<String> propertyNames = new HashSet<String>();

                // Add read/write properties (ie: public getter/setter).
                Introspector.flushFromCaches(type);	// Ensure that we don't get a cached reference, the Groovy class could have been modified
                BeanInfo info = Introspector.getBeanInfo(type);
                for (PropertyDescriptor property : info.getPropertyDescriptors()) {
                    String propertyName = property.getName();
                    if (property.getWriteMethod() != null && property.getReadMethod() != null) {
                        properties.add(new MethodProperty(converters, propertyName, property.getWriteMethod(), property.getReadMethod()));
                        propertyNames.add(propertyName);
                    }
                }

                // Add other public fields.
                Field[] fields = type.getFields();
                for (Field field : fields) {
                    String propertyName = field.getName();
                    if (!propertyNames.contains(propertyName) &&
                        !Modifier.isStatic(field.getModifiers()) &&
                        !Modifier.isTransient(field.getModifiers())) {
                        properties.add(new FieldProperty(converters, field));
                        propertyNames.add(propertyName);
                    }
                }
            }
            catch (RuntimeException e) {
                throw e;
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return properties;
    }
}
