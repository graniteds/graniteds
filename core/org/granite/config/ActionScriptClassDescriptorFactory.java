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

package org.granite.config;

import java.util.List;

import org.granite.messaging.amf.io.util.ActionScriptClassDescriptor;
import org.granite.util.ClassUtil;

/**
 * @author Franck WOLFF
 */
public class ActionScriptClassDescriptorFactory implements ConfigurableFactory<Class<? extends ActionScriptClassDescriptor>> {

    private static final class NullActionScriptClassDescriptor extends ActionScriptClassDescriptor {
        public NullActionScriptClassDescriptor(String type, byte encoding) {
            super(type, encoding);
            throw new RuntimeException("Not implemented");
        }
        @Override
        public void defineProperty(String name) {
        }
        @Override
        public Object newJavaInstance() {
            return null;
        }
    }
    private static final Class<? extends ActionScriptClassDescriptor> NULL_ASC_DESCRIPTOR
        = NullActionScriptClassDescriptor.class;

    public Class<? extends ActionScriptClassDescriptor> getNullInstance() {
        return NULL_ASC_DESCRIPTOR;
    }

    public Class<? extends ActionScriptClassDescriptor> getInstance(String type, GraniteConfig config) throws GraniteConfigException {
        try {
            return ClassUtil.forName(type, ActionScriptClassDescriptor.class);
        } catch (Exception e) {
            throw new GraniteConfigException("Could not load ActionScriptClassDescriptor type: " + type, e);
        }
    }

    public Class<? extends ActionScriptClassDescriptor> getInstanceForBean(
        List<Class<? extends ActionScriptClassDescriptor>> scannedConfigurables,
        Class<?> beanClass,
        GraniteConfig config) throws GraniteConfigException {

        return NULL_ASC_DESCRIPTOR;
    }
}
