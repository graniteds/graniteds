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
package org.granite.messaging.amf.io.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.granite.util.Introspector;
import org.granite.util.PropertyDescriptor;

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
                PropertyDescriptor[] descs = Introspector.getPropertyDescriptors(type);
                for (PropertyDescriptor property : descs) {
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
