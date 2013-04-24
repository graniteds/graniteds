/*
  GRANITE DATA SERVICES
  Copyright (C) 2013 GRANITE DATA SERVICES S.A.S.

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

package org.granite.messaging.jmf.codec.ext;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.MappedSuperclass;

import org.granite.messaging.jmf.ExtendedObjectInput;
import org.granite.messaging.jmf.ExtendedObjectOutput;
import org.granite.messaging.jmf.codec.ExtendedObjectCodec;
import org.granite.messaging.jmf.codec.util.ObjectCodecUtil;

/**
 * @author Franck WOLFF
 */
public class EntityCodec implements ExtendedObjectCodec {

	public boolean canEncode(Class<?> cls) {
		return cls.isAnnotationPresent(Entity.class) || cls.isAnnotationPresent(MappedSuperclass.class);
	}

	public void encode(ExtendedObjectOutput out, Object v) throws IOException, IllegalAccessException {
		out.writeBoolean(true);
		
		List<Field> fields = ObjectCodecUtil.findSerializableFields(v.getClass());
		for (Field field : fields)
			out.writeField(v, field);
	}

	public boolean canDecode(Class<?> cls) {
		return cls.isAnnotationPresent(Entity.class) || cls.isAnnotationPresent(MappedSuperclass.class);
	}

	public String getEncodedClassName(Class<?> cls) {
		return cls.getName();
	}

	public Object newInstance(ExtendedObjectInput in, Class<?> cls)
		throws InstantiationException, IllegalAccessException, InvocationTargetException,
		SecurityException, NoSuchMethodException {
		
		return ObjectCodecUtil.findDefaultContructor(cls).newInstance();
	}

	public void decode(ExtendedObjectInput in, Object v) throws IOException, ClassNotFoundException, IllegalAccessException {
		
		Class<?> cls = v.getClass();
		
		in.readBoolean();
		
		List<Field> fields = ObjectCodecUtil.findSerializableFields(cls);
		for (Field field : fields)
			in.readAndSetField(v, field);
	}
}
