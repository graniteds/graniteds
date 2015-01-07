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
package org.granite.messaging.jmf.codec.std.impl;

import java.lang.reflect.Array;

/**
 * @author Franck WOLFF
 */
public abstract class AbstractArrayCodec extends AbstractStandardCodec<Object> {
	
	protected int getArrayDimensions(Object v) {
		int dimensions = 0;
		for (Class<?> componentType = v.getClass().getComponentType();
			componentType.isArray();
			componentType = componentType.getComponentType()) {
			dimensions++;
		}
		return dimensions;
	}
	
	protected Class<?> getComponentType(Object v) {
		Class<?> componentType = v.getClass().getComponentType();
		while (componentType.isArray())
			componentType = componentType.getComponentType();
		return componentType;
	}
	
	protected Object newArray(Class<?> type, int length, int dimensions) {
		int[] ld = new int[dimensions + 1];
		ld[0] = length;
		return Array.newInstance(type, ld);
	}
	
	protected static class ArrayStructure {
		
		public final Class<?> componentType;
		public final int dimensions;
		
		public ArrayStructure(Object array) {
			this(array.getClass());
		}

		public ArrayStructure(Class<?> arrayClass) {
			Class<?> componentType = arrayClass.getComponentType();
			int dimensions = 0;
			while (componentType.isArray()) {
				componentType = componentType.getComponentType();
				dimensions++;
			}
			this.componentType = componentType;
			this.dimensions = dimensions;
		}

		@Override
		public String toString() {
			return componentType.getName() + "^" + dimensions;
		}
	}
}
