package org.granite.util;

public class ObjectIndexedCache extends AbstractIndexedCache {

	public ObjectIndexedCache() {
		super();
	}

	public ObjectIndexedCache(int initialCapacity) {
		super(initialCapacity);
	}

	public ObjectIndexedCache(int initialCapacity, float loadFactor) {
		super(initialCapacity, loadFactor);
	}

	@Override
	public final int hash(Object o) {
        int h = System.identityHashCode(o);
        return ((h << 1) - (h << 8));
	}

	@Override
	public final boolean equals(Entry e, int hash, Object o) {
		return e.o == o;
	}
}
