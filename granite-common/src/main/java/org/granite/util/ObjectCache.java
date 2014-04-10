package org.granite.util;

public class ObjectCache {

	private static final int DEFAULT_CAPACITY = 32;
    private static final int MINIMUM_CAPACITY = 4;
    private static final int MAXIMUM_CAPACITY = 1 << 29;
	
	private Object[] table;
	private int threshold;
    private int size;
    
    public ObjectCache() {
        init(DEFAULT_CAPACITY);
    }

    public ObjectCache(int expectedMaxSize) {
        if (expectedMaxSize < 0)
            throw new IllegalArgumentException("expectedMaxSize is negative: " + expectedMaxSize);
        init(capacity(expectedMaxSize));
    }

    private int capacity(int expectedMaxSize) {
        int minCapacity = (3 * expectedMaxSize) / 2;
        if (minCapacity > MAXIMUM_CAPACITY || minCapacity < 0)
        	return MAXIMUM_CAPACITY;
        
        int capacity = MINIMUM_CAPACITY;
        while (capacity < minCapacity)
        	capacity <<= 1;
        return capacity;
    }

    private void init(int initCapacity) {
        threshold = (initCapacity * 2)/3;
        table = new Object[2 * initCapacity];
    }
	
	public int putIfAbsent(Object o) {
        Object[] tab = table;
        int len = tab.length;
        int i = hash(o, len);

        Object item;
        while ((item = tab[i]) != null) {
            if (item == o)
            	return ((Integer)tab[i + 1]).intValue();
            i = nextKeyIndex(i, len);
        }

        tab[i] = o;
        tab[i + 1] = Integer.valueOf(size);
        if (++size >= threshold)
            resize(len);
		
        return -1;
	}
    
	private static int hash(Object x, int length) {
        int h = System.identityHashCode(x);
        return ((h << 1) - (h << 8)) & (length - 1);
    }

	private static int nextKeyIndex(int i, int len) {
        return (i + 2 < len ? i + 2 : 0);
    }
    
	private void resize(int newCapacity) {
        int newLength = newCapacity * 2;

        Object[] oldTable = table;
        int oldLength = oldTable.length;
        if (oldLength == 2 * MAXIMUM_CAPACITY) {
            if (threshold == MAXIMUM_CAPACITY - 1)
                throw new IllegalStateException("Capacity exhausted.");
            threshold = MAXIMUM_CAPACITY-1;
            return;
        }
        if (oldLength >= newLength)
            return;

        Object[] newTable = new Object[newLength];
        threshold = newLength / 3;

        for (int j = 0; j < oldLength; j += 2) {
            Object key = oldTable[j];
            if (key != null) {
                Object value = oldTable[j+1];
                oldTable[j] = null;
                oldTable[j+1] = null;
                int i = hash(key, newLength);
                while (newTable[i] != null)
                    i = nextKeyIndex(i, newLength);
                newTable[i] = key;
                newTable[i + 1] = value;
            }
        }
        table = newTable;
    }
}
