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

package org.granite.messaging.amf.io.util.externalizer;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author Igor SAZHNEV
 */
public class MapExternalizer extends DefaultExternalizer {

    @Override
    public int accept(Class<?> clazz) {
        return Map.class.isAssignableFrom(clazz) ? 1 : -1;
    }

    @Override
    public void readExternal(Object o, ObjectInput in) throws IOException, ClassNotFoundException, IllegalAccessException {
    	@SuppressWarnings("unchecked")
		Map<Object, Object> map = (Map<Object, Object>)o;
    	int size = in.readInt();
    	for (int i = 0; i < size; i++) {
    		Object key = in.readObject();
    		Object value = in.readObject();
    		map.put(key, value);
    	}
    }

    @Override
    public void writeExternal(Object o, ObjectOutput out) throws IOException, IllegalAccessException {
    	@SuppressWarnings("unchecked")
		Map<Object, Object> map = (Map<Object, Object>)o;
        out.writeInt(map.size());
    	for (Entry<Object, Object> entry : map.entrySet()) {
    		out.writeObject(entry.getKey());
    		out.writeObject(entry.getValue());
    	}
    }
}
