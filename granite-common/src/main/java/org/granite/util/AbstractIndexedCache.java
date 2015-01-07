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
package org.granite.util;

/**
 * @author Franck WOLFF
 */
public abstract class AbstractIndexedCache<T> {
	
	protected static final int MAXIMUM_CAPACITY = 1 << 30;
	protected static final int DEFAULT_INITIAL_CAPACITY = 1 << 6;
	protected static final float DEFAULT_LOAD_FACTOR = 0.75f;

	protected int initialCapacity;
	protected Entry[] entries;
	protected int threshold;
	protected int size;
	
	protected final void init(int capacity) {
		this.initialCapacity = capacity;
        this.entries = new Entry[capacity];
        this.threshold = (int)Math.min(capacity * DEFAULT_LOAD_FACTOR, MAXIMUM_CAPACITY + 1);
        this.size = 0;
	}
	
	public abstract int putIfAbsent(T key);
	
	public final int size() {
		return size;
	}
	
	public final void clear() {
		init(initialCapacity);
	}
    
	protected final int resize(int newCapacity) {
        Entry[] oldEntries = entries;

        if (oldEntries.length == MAXIMUM_CAPACITY) {
            threshold = Integer.MAX_VALUE;
            return (MAXIMUM_CAPACITY - 1);
        }

		final int indexMask = newCapacity - 1;
        
		Entry[] newEntries = new Entry[newCapacity];
        for (Entry entry : oldEntries) {
            while (entry != null) {
                int i = entry.hash & indexMask;
                Entry next = entry.next;
                entry.next = newEntries[i];
                newEntries[i] = entry;
                entry = next;
            }
        }
        
        entries = newEntries;
        threshold = (int)Math.min(newCapacity * DEFAULT_LOAD_FACTOR, MAXIMUM_CAPACITY + 1);
        
        return indexMask;
    }
    
	protected static int roundUpToPowerOf2(int number) {
    	if (number >= MAXIMUM_CAPACITY)
    		return MAXIMUM_CAPACITY;
    	int rounded = Integer.highestOneBit(number);
    	if (rounded == 0)
    		return 1;
    	if (Integer.bitCount(number) > 1)
    		return rounded << 1;
    	return rounded;
    }
	
	protected final static class Entry {
		
		public final Object key;
		public final int hash;
		public final int index;
		
		public Entry next;
		
		public Entry(Object key, int hash, int index, Entry next) {
			this.key = key;
			this.hash = hash;
			this.index = index;
			this.next = next;
		}
	}
}
