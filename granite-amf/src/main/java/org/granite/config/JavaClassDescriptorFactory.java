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
package org.granite.config;

import java.util.List;

import org.granite.config.api.ConfigurableFactory;
import org.granite.config.api.GraniteConfigException;
import org.granite.messaging.amf.io.util.JavaClassDescriptor;
import org.granite.messaging.amf.io.util.Property;
import org.granite.util.TypeUtil;

/**
 * @author Franck WOLFF
 */
public class JavaClassDescriptorFactory implements ConfigurableFactory<Class<? extends JavaClassDescriptor>, Config> {

    private static final class NullJavaClassDescriptor extends JavaClassDescriptor {
        protected NullJavaClassDescriptor(Class<?> type) {
            super(type);
            throw new RuntimeException("Not implemented");
        }
        @Override
        protected List<Property> introspectProperties() {
            return null;
        }
    }
    private static final Class<? extends JavaClassDescriptor> NULL_JC_DESCRIPTOR
        = NullJavaClassDescriptor.class;

    public Class<? extends JavaClassDescriptor> getNullInstance() {
        return NULL_JC_DESCRIPTOR;
    }

    public Class<? extends JavaClassDescriptor> getInstance(String type, Config config) throws GraniteConfigException {
        try {
            return TypeUtil.forName(type, JavaClassDescriptor.class);
        } catch (Exception e) {
            throw new GraniteConfigException("Could not load JavaClassDescriptor type: " + type, e);
        }
    }

    public Class<? extends JavaClassDescriptor> getInstanceForBean(
        List<Class<? extends JavaClassDescriptor>> scannedConfigurables,
        Class<?> beanClass,
        Config config) throws GraniteConfigException {

        return NULL_JC_DESCRIPTOR;
    }
}
