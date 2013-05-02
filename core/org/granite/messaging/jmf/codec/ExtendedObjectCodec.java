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

package org.granite.messaging.jmf.codec;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.granite.messaging.jmf.ExtendedObjectInput;
import org.granite.messaging.jmf.ExtendedObjectOutput;

/**
 * @author Franck WOLFF
 */
public interface ExtendedObjectCodec {

	boolean canEncode(ExtendedObjectOutput out, Object v);
	
	String getEncodedClassName(ExtendedObjectOutput out, Object v);
	
	void encode(ExtendedObjectOutput out, Object v) throws IOException, IllegalAccessException;

	
	boolean canDecode(ExtendedObjectInput in, Class<?> cls);

	Object newInstance(ExtendedObjectInput in, Class<?> cls)
		throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException,
		SecurityException, NoSuchMethodException;
	
	void decode(ExtendedObjectInput in, Object v)
		throws IOException, ClassNotFoundException, IllegalAccessException;
}
