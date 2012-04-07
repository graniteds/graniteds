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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;

import org.granite.util.ClassUtil;
import org.granite.util.Introspector;
import org.granite.util.PropertyDescriptor;

/**
 * @author Franck WOLFF
 */
public class DefaultActionScriptClassDescriptor extends ActionScriptClassDescriptor {

    public DefaultActionScriptClassDescriptor(String type, byte encoding) {
        super(type, encoding);
    }

    @Override
    public void defineProperty(String name) {

        if (type.length() == 0 || instantiator != null)
            properties.add(new MapProperty(converters, name));
        else {
            try {
                Class<?> clazz = ClassUtil.forName(type);

                // Try to find public getter/setter.
                PropertyDescriptor[] props = Introspector.getPropertyDescriptors(clazz);
                for (PropertyDescriptor prop : props) {
                    if (name.equals(prop.getName()) && prop.getWriteMethod() != null && prop.getReadMethod() != null) {
                        properties.add(new MethodProperty(converters, name, prop.getWriteMethod(), prop.getReadMethod()));
                        return;
                    }
                }

                // Try to find public field.
                Field field = clazz.getField(name);
                if (!Modifier.isStatic(field.getModifiers()) && !Modifier.isTransient(field.getModifiers()))
                    properties.add(new FieldProperty(converters, field));

            }
            catch (NoSuchFieldException e) {
                if ("uid".equals(name)) // ObjectProxy specific property...
                    properties.add(new UIDProperty(converters));
                else
                	throw new RuntimeException(e);
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public Object newJavaInstance() {

        if (type.length() == 0)
            return new HashMap<String, Object>();

        String className = (instantiator != null ? instantiator : type);
        try {
            return ClassUtil.newInstance(className);
        } catch (Exception e) {
            throw new RuntimeException("Could not create instance of: " + className, e);
        }
    }
}
