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

/**
 * @author Franck WOLFF
 */
public abstract class AbstractIndexedCache {

	private static final int MAXIMUM_CAPACITY = 1 << 30;
	private static final int DEFAULT_INITIAL_CAPACITY = 1 << 4;
	private static final float DEFAULT_LOAD_FACTOR = 0.75f;
	
	private final float loadFactor;

	private Entry[] table;
	private int threshold;
	private int size;

	public AbstractIndexedCache() {
		this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR);
	}

    public AbstractIndexedCache(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }
	
	public AbstractIndexedCache(int initialCapacity, float loadFactor) {
        if (initialCapacity < 0)
            throw new IllegalArgumentException("Illegal initial capacity: " + initialCapacity);
        if (initialCapacity > MAXIMUM_CAPACITY)
            initialCapacity = MAXIMUM_CAPACITY;
        if (loadFactor <= 0 || Float.isNaN(loadFactor))
            throw new IllegalArgumentException("Illegal load factor: " + loadFactor);

        this.loadFactor = loadFactor;
        
        int capacity = roundUpToPowerOf2(initialCapacity);
        this.threshold = (int)Math.min(capacity * loadFactor, MAXIMUM_CAPACITY + 1);
        this.table = new Entry[capacity];
	}
    
    public abstract int hash(Object o);
    public abstract int find(Entry head, int hash, Object o);
	
	public final int putIfAbsent(Object o) {
        int hash = hash(o);
        int index = indexFor(hash, table.length);
        
        Entry head = table[index];
        if (head != null) {
        	int found = find(head, hash, o);
        	if (found != -1)
        		return found;
        }
        
        addEntry(hash, o, index);

        return -1;
	}
    
	private void addEntry(int hash, Object o, int index) {
		if (size >= threshold && table[index] != null) {
            resize(2 * table.length);
            index = indexFor(hash, table.length);
        }

        table[index] = new Entry(hash, o, size, table[index]);
        size++;
    }
    
	private void resize(int newCapacity) {
        Entry[] oldTable = table;

        if (oldTable.length == MAXIMUM_CAPACITY) {
            threshold = Integer.MAX_VALUE;
            return;
        }

        Entry[] newTable = new Entry[newCapacity];
        for (Entry e : oldTable) {
            while (e != null) {
                Entry next = e.next;
                int i = indexFor(e.hash, newCapacity);
                e.next = newTable[i];
                newTable[i] = e;
                e = next;
            }
        }
        
        table = newTable;
        threshold = (int)Math.min(newCapacity * loadFactor, MAXIMUM_CAPACITY + 1);
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
	
	protected final static class Entry {
		
		public final int hash;
		public final Object o;
		public final int index;
		
		public Entry next;
		
		public Entry(int hash, Object o, int index, Entry next) {
			this.hash = hash;
			this.o = o;
			this.index = index;
			this.next = next;
		}
	}
}
