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

import java.io.Externalizable;
import java.util.List;
import java.util.Map;

import org.granite.config.GraniteConfig;
import org.granite.context.GraniteContext;
import org.granite.messaging.amf.RemoteClass;
import org.granite.messaging.amf.io.convert.Converters;
import org.granite.messaging.amf.io.util.externalizer.Externalizer;

/**
 * @author Franck WOLFF
 */
public abstract class JavaClassDescriptor {

    protected final Class<?> type;
    protected final String name;
    protected final Externalizer externalizer;
    protected final Converters converters;
    protected final byte encoding;
    protected final List<Property> properties;

    protected JavaClassDescriptor(Class<?> type) {
        GraniteConfig config = GraniteContext.getCurrentInstance().getGraniteConfig();
        this.type = type;
        this.name = getClassName(type);
        this.externalizer = config.getExternalizer(type.getName());
        this.converters = config.getConverters();
        this.encoding = findEncoding(type);
        this.properties = introspectProperties();
    }

    private byte findEncoding(Class<?> type) {
        if (externalizer != null || Externalizable.class.isAssignableFrom(type))
            return 0x01;
        if (Map.class.isAssignableFrom(type))
            return 0x02;
        return 0x00;
    }

    protected abstract List<Property> introspectProperties();

    public static String getClassName(Class<?> clazz) {
        if (Map.class.isAssignableFrom(clazz) && !Externalizable.class.isAssignableFrom(clazz)) {
            Externalizer externalizer = GraniteContext.getCurrentInstance().getGraniteConfig().getExternalizer(clazz.getName());
            if (externalizer == null)
            	return "";
        }
        RemoteClass alias = clazz.getAnnotation(RemoteClass.class);
        return alias != null ? alias.value() : clazz.getName();
    }
    
    public Class<?> getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public Externalizer getExternalizer() {
        return externalizer;
    }

    public byte getEncoding() {
        return encoding;
    }

    public boolean isExternalizable() {
        return encoding == 0x01;
    }

    public boolean isDynamic() {
        return encoding == 0x02;
    }

    public int getPropertiesCount() {
        return (properties != null ? properties.size() : 0);
    }

    public String getPropertyName(int index) {
        if (properties == null)
            throw new ArrayIndexOutOfBoundsException(index);
        return properties.get(index).getName();
    }

    public Object getPropertyValue(int index, Object instance) {
        if (properties == null)
            throw new ArrayIndexOutOfBoundsException(index);
        Property prop = properties.get(index);
        return prop.getProperty(instance);
    }
}
