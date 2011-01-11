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

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * @author Franck WOLFF
 */
public interface JavaProperty extends As3Typed, Comparable<JavaProperty> {

    public String getName();

    public boolean isReadable();

    public boolean isWritable();

    public boolean isExternalizedProperty();
    
    public boolean isEnum();

    public boolean isReadOverride();

    public boolean isWriteOverride();

    public JavaMethod getReadMethod();

    public JavaMethod getWriteMethod();

    public Class<?> getType();
    
    public Type[] getGenericTypes();

    public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass);
    
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass);
    
    public Annotation[] getDeclaredAnnotations();
}
