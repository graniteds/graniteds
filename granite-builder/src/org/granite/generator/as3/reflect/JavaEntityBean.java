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

import javax.persistence.IdClass;

/**
 * @author Franck WOLFF
 */
public class JavaEntityBean extends JavaBean {

    protected final List<JavaFieldProperty> identifiers;
    protected final JavaType idClass;
    protected JavaProperty version;
    
    public JavaEntityBean(JavaTypeFactory provider, Class<?> type, URL url) {
        super(provider, type, url);

        // Find identifiers.
        List<JavaFieldProperty> tmpIdentifiers = new ArrayList<JavaFieldProperty>();
        for (JavaProperty property : properties.values()) {
            if (property instanceof JavaFieldProperty && provider.isId((JavaFieldProperty)property))
                tmpIdentifiers.add((JavaFieldProperty)property);
            else if (provider.isVersion(property))
            	version = property;
        }
        this.identifiers = (tmpIdentifiers.isEmpty() ? null : Collections.unmodifiableList(tmpIdentifiers));

        // Find IdClass (if any).
        this.idClass = (
            type.isAnnotationPresent(IdClass.class) ?
            provider.getJavaType(type.getAnnotation(IdClass.class).value()) :
            null
        );

        // Collect additional imports.
        if (idClass != null)
            addToImports(provider.getJavaImport(idClass.getType()));
    }

    public boolean hasIdentifiers() {
        return identifiers != null && !identifiers.isEmpty();
    }
    public List<JavaFieldProperty> getIdentifiers() {
        return identifiers;
    }
    public JavaFieldProperty getFirstIdentifier() {
        return (identifiers != null ? identifiers.get(0) : null);
    }

    public boolean hasIdClass() {
        return idClass != null;
    }
    public JavaType getIdClass() {
        return idClass;
    }
    
    public boolean hasVersion() {
    	return version != null;
    }
    public JavaProperty getVersion() {
    	return version;
    }
}
