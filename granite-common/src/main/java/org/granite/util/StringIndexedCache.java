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
package org.granite.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @author Franck WOLFF
 */
public class StringIndexedCache extends AbstractIndexedCache {
	
	private final static Field hash32Field;
	private final static Method hash32Method;

	static {
		Field field = null;
		try {
			field = String.class.getDeclaredField("hash32");
			field.setAccessible(true);
		}
		catch (Throwable t) {
			field = null;
		}
		
		Method method = null;
		if (field != null) {
			try {
				method = String.class.getDeclaredMethod("hash32");
				method.setAccessible(true);
			}
			catch (Throwable t) {
				field = null;
				method = null;
			}
		}
		
		hash32Field = field;
		hash32Method = method;
	}

	public StringIndexedCache() {
		super();
	}

	public StringIndexedCache(int initialCapacity) {
		super(initialCapacity);
	}

	public StringIndexedCache(int initialCapacity, float loadFactor) {
		super(initialCapacity, loadFactor);
	}

	@Override
	public final int hash(Object s) {
		if (hash32Field != null) {
			try {
				int hash = hash32Field.getInt(s);
				if (hash == 0)
					hash = ((Integer)hash32Method.invoke(s)).intValue();
				return hash;
			}
			catch (Throwable t) {
				// fallback...
			}
		}

		int h = s.hashCode();
        h ^= (h >>> 20) ^ (h >>> 12);
        return h ^ (h >>> 7) ^ (h >>> 4);
	}

	@Override
	public final int find(Entry head, int hash, Object o) {
        do {
        	if (head.hash == hash) {
        		Object eo = head.o;
        		if (eo == o || eo.equals(o))
        			return head.index;
        	}
        }
        while ((head = head.next) != null);
        return -1;
	}
}
