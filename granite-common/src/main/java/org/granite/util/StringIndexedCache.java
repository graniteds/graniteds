package org.granite.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

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
	public final boolean equals(Entry e, int hash, Object s) {
		if (e.hash == hash) {
			Object eo = e.o;
			return eo == s || eo.equals(s);
		}
		return false;
	}
}
