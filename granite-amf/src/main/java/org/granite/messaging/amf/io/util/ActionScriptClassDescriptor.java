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

import java.util.ArrayList;
import java.util.List;

import org.granite.config.ConvertersConfig;
import org.granite.config.ExternalizersConfig;
import org.granite.config.api.AliasRegistryConfig;
import org.granite.context.GraniteContext;
import org.granite.messaging.amf.io.convert.Converters;
import org.granite.messaging.amf.io.util.externalizer.Externalizer;
import org.granite.messaging.amf.io.util.instantiator.AbstractInstantiator;

/**
 * @author Franck WOLFF
 */
public abstract class ActionScriptClassDescriptor {

    protected final String type;
    protected final String instantiator;
    protected final byte encoding;
    protected final Externalizer externalizer;
    protected final Converters converters;
    protected final List<Property> properties;

    protected ActionScriptClassDescriptor(String type, byte encoding) {
        Object config = GraniteContext.getCurrentInstance().getGraniteConfig();
        this.type = (type == null ? "" : ((AliasRegistryConfig)config).getAliasRegistry().getTypeForAlias(type));
        this.instantiator = ((ExternalizersConfig)config).getInstantiator(type);
        this.encoding = encoding;
        this.externalizer = findExternalizer();
        this.converters = ((ConvertersConfig)config).getConverters();
        this.properties = new ArrayList<Property>();
    }

    private Externalizer findExternalizer() {
        if (encoding != 0x01)
            return null;
        return ((ExternalizersConfig)GraniteContext.getCurrentInstance().getGraniteConfig()).getExternalizer(type);
    }

    public String getType() {
        return type;
    }

    public String getInstantiator() {
        return instantiator;
    }

    public Externalizer getExternalizer() {
        return externalizer;
    }

    public byte getEncoding() {
        return encoding;
    }

    public boolean isExternalizable() {
        return (encoding & 0x01) != 0;
    }

    public boolean isDynamic() {
        return (encoding & 0x02) != 0;
    }

    public abstract void defineProperty(String name);
    public abstract Object newJavaInstance();

    public int getPropertiesCount() {
        return properties.size();
    }
    public Property getProperty(int index) {
        return properties.get(index);
    }
    public String getPropertyName(int index) {
        return properties.get(index).getName();
    }

    public void setPropertyValue(int index, Object instance, Object value) {
        Property prop = properties.get(index);
        if (value instanceof AbstractInstantiator<?>)
            ((AbstractInstantiator<?>)value).addReferer(instance, prop);
        else
            prop.setValue(instance, value);
    }

    public void setPropertyValue(String name, Object instance, Object value) {
        // instance must be an instance of Map...
        Property prop = new MapProperty(converters, name);
        if (value instanceof AbstractInstantiator<?>)
            ((AbstractInstantiator<?>)value).addReferer(instance, prop);
        else
            prop.setValue(instance, value);
    }

    @Override
    public String toString() {
        return getClass().getName() + " {\n" +
            "  type=" + type + ",\n" +
            "  instantiator=" + instantiator + ",\n" +
            "  encoding=" + encoding + ",\n" +
            "  externalizer=" + externalizer + ",\n" +
            "  converters=" + converters + ",\n" +
            "  properties=" + properties + "\n" +
        "}";
    }
}
