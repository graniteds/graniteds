package org.granite.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class StringCache {
	
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

	private static final int MAXIMUM_CAPACITY = 1 << 30;
	private static final int DEFAULT_INITIAL_CAPACITY = 1 << 4;
	private static final float DEFAULT_LOAD_FACTOR = 0.75f;
	private static final Entry[] EMPTY_TABLE = {};
	
	private final float loadFactor;

	private Entry[] table = EMPTY_TABLE;
	private int threshold;
	private int size;

	public StringCache() {
		this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR);
	}

    public StringCache(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }
	
    public StringCache(int initialCapacity, float loadFactor) {
        if (initialCapacity < 0)
            throw new IllegalArgumentException("Illegal initial capacity: " + initialCapacity);
        if (initialCapacity > MAXIMUM_CAPACITY)
            initialCapacity = MAXIMUM_CAPACITY;
        if (loadFactor <= 0 || Float.isNaN(loadFactor))
            throw new IllegalArgumentException("Illegal load factor: " + loadFactor);

        this.loadFactor = loadFactor;
        this.threshold = initialCapacity;
	}
	
	public int putIfAbsent(String s) {
        if (table == EMPTY_TABLE)
            inflateTable(threshold);

        int hash = hash(s);
        int index = indexFor(hash, table.length);
        
        for (Entry e = table[index]; e != null; e = e.next) {
            String k;
            if (e.hash == hash && ((k = e.s) == s || s.equals(k)))
                return e.index;
        }
        
        addEntry(hash, s, index);

        return -1;
	}
    
	private void addEntry(int hash, String s, int index) {
        if ((size >= threshold) && (null != table[index])) {
            resize(2 * table.length);
            hash = (null != s) ? hash(s) : 0;
            index = indexFor(hash, table.length);
        }

        createEntry(hash, s, index);
    }

    private void createEntry(int hash, String s, int index) {
        Entry e = table[index];
        table[index] = new Entry(hash, s, size, e);
        size++;
    }
    
    private void inflateTable(int toSize) {
        int capacity = roundUpToPowerOf2(toSize);

        threshold = (int)Math.min(capacity * loadFactor, MAXIMUM_CAPACITY + 1);
        table = new Entry[capacity];
    }
    
    private void resize(int newCapacity) {
        Entry[] oldTable = table;
        int oldCapacity = oldTable.length;
        if (oldCapacity == MAXIMUM_CAPACITY) {
            threshold = Integer.MAX_VALUE;
            return;
        }

        Entry[] newTable = new Entry[newCapacity];
        transfer(newTable);
        table = newTable;
        threshold = (int)Math.min(newCapacity * loadFactor, MAXIMUM_CAPACITY + 1);
    }

    private void transfer(Entry[] newTable) {
        int newCapacity = newTable.length;
        for (Entry e : table) {
            while (e != null) {
                Entry next = e.next;
                int i = indexFor(e.hash, newCapacity);
                e.next = newTable[i];
                newTable[i] = e;
                e = next;
            }
        }
    }

    private static int roundUpToPowerOf2(int number) {
    	if (number >= MAXIMUM_CAPACITY)
    		return MAXIMUM_CAPACITY;
    	int rounded = Integer.highestOneBit(number);
    	if (rounded == 0)
    		return 1;
    	if (Integer.bitCount(number) > 1)
    		return rounded << 1;
    	return rounded;
    }
    
    private static int indexFor(int h, int length) {
        return h & (length-1);
    }
	
	private static int hash(String s) {
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
	
	private static class Entry {
		
		public final String s;
		public final int index;
		
		public int hash;
		public Entry next;
		
		public Entry(int hash, String s, int index, Entry next) {
			this.hash = hash;
			this.s = s;
			this.index = index;
			this.next = next;
		}
	}
}
