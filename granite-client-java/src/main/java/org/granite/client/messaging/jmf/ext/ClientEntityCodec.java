/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2014 GRANITE DATA SERVICES S.A.S.
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
package org.granite.client.messaging.jmf.ext;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.granite.client.persistence.Entity;
import org.granite.client.persistence.Persistence;
import org.granite.client.platform.Platform;
import org.granite.messaging.jmf.ExtendedObjectInput;
import org.granite.messaging.jmf.ExtendedObjectOutput;
import org.granite.messaging.jmf.codec.ExtendedObjectCodec;
import org.granite.messaging.reflect.Property;

/**
 * @author Franck WOLFF
 */
public class ClientEntityCodec implements ExtendedObjectCodec {

	public boolean canEncode(ExtendedObjectOutput out, Object v) {
		return v.getClass().isAnnotationPresent(Entity.class);
	}

	public String getEncodedClassName(ExtendedObjectOutput out, Object v) {
        return out.getAlias(v.getClass().getName());
	}

	public void encode(ExtendedObjectOutput out, Object v) throws IOException, IllegalAccessException, InvocationTargetException {
		
		Persistence persistence = Platform.persistence();
		
		boolean initialized = persistence.isInitialized(v);
		
		out.writeBoolean(initialized);
		out.writeUTF(persistence.getDetachedState(v));
		
		if (!initialized)
			out.writeObject(persistence.getId(v));
		else {
			List<Property> properties = new ArrayList<Property>(out.getReflection().findSerializableProperties(v.getClass()));
			properties.remove(persistence.getInitializedProperty(v.getClass()));
			properties.remove(persistence.getDetachedStateProperty(v.getClass()));

			for (Property property : properties)
				out.getAndWriteProperty(v, property);
		}
	}

	public boolean canDecode(ExtendedObjectInput in, String className) throws ClassNotFoundException {
		String alias = in.getAlias(className);
		Class<?> cls = in.getReflection().loadClass(alias);
		return cls.isAnnotationPresent(Entity.class);
	}

	public String getDecodedClassName(ExtendedObjectInput in, String className) {
		return in.getAlias(className);
	}

	public Object newInstance(ExtendedObjectInput in, String className)
		throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException,
		InvocationTargetException, SecurityException, NoSuchMethodException, IOException {
		
		Class<?> cls = in.getReflection().loadClass(className);
		return in.getReflection().newInstance(cls);
	}

	public void decode(ExtendedObjectInput in, Object v) throws IOException, ClassNotFoundException, IllegalAccessException, InvocationTargetException {
		
		Persistence persistence = Platform.persistence();
		
		boolean initialized = in.readBoolean();
		String detachedState = in.readUTF();
		
		persistence.setInitialized(v, initialized);
		persistence.setDetachedState(v, detachedState);
		
		if (!initialized)
			persistence.setId(v, in.readObject());
		else {
			List<Property> properties = new ArrayList<Property>(in.getReflection().findSerializableProperties(v.getClass()));
			properties.remove(persistence.getInitializedProperty(v.getClass()));
			properties.remove(persistence.getDetachedStateProperty(v.getClass()));

			for (Property property : properties)
				in.readAndSetProperty(v, property);
		}
	}
}
