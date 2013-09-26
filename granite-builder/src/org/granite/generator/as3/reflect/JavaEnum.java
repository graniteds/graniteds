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

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Franck WOLFF
 */
public class JavaEnum extends JavaAbstractType {

    private final List<JavaEnumValue> enumValues;

    public JavaEnum(JavaTypeFactory provider, Class<?> type, URL url) {
        super(provider, type, url);

        if (!type.isEnum())
            throw new IllegalArgumentException("type should be an enum: " + type);

        @SuppressWarnings("unchecked")
        Class<? extends Enum<?>> enumType = (Class<? extends Enum<?>>)type;

        List<JavaEnumValue> enumValues = new ArrayList<JavaEnumValue>();
        for(Enum<?> constant : enumType.getEnumConstants())
            enumValues.add(new JavaEnumValue(constant));
        this.enumValues = Collections.unmodifiableList(enumValues);
    }

    public JavaEnumValue getFirstEnumValue() {
    	return enumValues.isEmpty() ? null : enumValues.get(0);
    }
    
    public List<JavaEnumValue> getEnumValues() {
        return enumValues;
    }
}
