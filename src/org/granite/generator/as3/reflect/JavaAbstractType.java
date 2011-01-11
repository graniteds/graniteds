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
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.granite.generator.as3.As3Type;
import org.granite.util.ClassUtil;
import org.granite.util.URIUtil;

/**
 * @author Franck WOLFF
 */
public abstract class JavaAbstractType implements JavaType {

    ///////////////////////////////////////////////////////////////////////////
    // Generation type.
	
	public static enum GenerationType {
		NOT_GENERATED,
		GENERATED_SINGLE,
		GENERATED_WITH_BASE
	}

    ///////////////////////////////////////////////////////////////////////////
    // Fields.

    protected final JavaTypeFactory provider;
    protected final Class<?> type;
    protected final URL url;
    protected final As3Type as3Type;
    protected final Kind kind;
    protected final GenerationType generationType;

    private long lastModified = Long.MIN_VALUE;

    ///////////////////////////////////////////////////////////////////////////
    // Constructor.

    protected JavaAbstractType(JavaTypeFactory provider, Class<?> type, URL url) {
        if (provider == null || type == null)
            throw new IllegalArgumentException("Parameter provider and type cannot be null");

        this.provider = provider;
        this.type = type;
        this.url = url;
        this.as3Type = provider.getAs3Type(type);
        this.kind = provider.getKind(type);
        this.generationType = provider.getGenerationType(kind, type);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Properties.

    protected JavaTypeFactory getProvider() {
        return provider;
    }

    public Class<?> getType() {
        return type;
    }

	public String getName() {
    	if (type.isMemberClass())
    		return type.getEnclosingClass().getSimpleName() + '$' + type.getSimpleName();
        return type.getSimpleName();
    }

    public Package getPackage() {
        return type.getPackage();
    }

    public String getPackageName() {
        return (type.getPackage() != null ? type.getPackage().getName() : "");
    }

    public String getQualifiedName() {
        if (type.getPackage() == null)
            return getName();
        return new StringBuilder().append(getPackageName()).append('.').append(getName()).toString();
    }

    public URL getUrl() {
        return url;
    }

    public boolean isBean() {
		return kind == Kind.BEAN;
	}

	public boolean isEntity() {
		return kind == Kind.ENTITY;
	}

	public boolean isEnum() {
		return kind == Kind.ENUM;
	}

	public boolean isInterface() {
		return kind == Kind.INTERFACE;
	}
	
	public boolean isRemoteDestination() {
		return kind == Kind.REMOTE_DESTINATION;
	}

	public boolean isGenerated() {
		return generationType != GenerationType.NOT_GENERATED;
	}

	public boolean isWithBase() {
		return generationType == GenerationType.GENERATED_WITH_BASE;
	}

	public GenerationType getGenerationType() {
		return generationType;
	}

	public Kind getKind() {
		return kind;
	}

	public long getLastModified() {
        if (lastModified == Long.MIN_VALUE) {
            try {
                lastModified = URIUtil.lastModified(url);
            } catch (IOException e) {
                lastModified = -1L;
            }
        }
        return lastModified;
    }

    public As3Type getAs3Type() {
        return as3Type;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Utility.

    protected <T extends Collection<?>> T removeNull(T coll) {
        coll.remove(null);
        return coll;
    }

    protected PropertyDescriptor[] getPropertyDescriptors(Class<?> type) {
        PropertyDescriptor[] propertyDescriptors = ClassUtil.getProperties(type);
        return (propertyDescriptors != null ? propertyDescriptors : new PropertyDescriptor[0]);
    }

    protected List<JavaProperty> getSortedUnmodifiableList(Collection<JavaProperty> coll) {
        List<JavaProperty> list = (coll instanceof List<?> ? (List<JavaProperty>)coll : new ArrayList<JavaProperty>(coll));
        Collections.sort(list);
        return Collections.unmodifiableList(list);
    }
}
