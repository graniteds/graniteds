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
public final class StringIndexedCache extends AbstractIndexedCache<String> {
	
	public StringIndexedCache() {
		init(DEFAULT_INITIAL_CAPACITY);
	}
	
	public StringIndexedCache(int capacity) {
		init(roundUpToPowerOf2(capacity));
	}
	
	public int putIfAbsent(String key) {
		final int hash = key.hashCode();
        
		int index = hash & (entries.length - 1);
		Entry head = entries[index];
		
		if (head != null) {
			Entry entry = head;
			do {
				if (hash == entry.hash && key.equals(entry.key))
					return entry.index;
				entry = entry.next;
			}
			while (entry != null);
			
			if (size >= threshold) {
	            index = hash & resize(entries.length * 2);
	            head = entries[index];
			}
		}

        entries[index] = new Entry(key, hash, size, head);
        size++;
        
        return -1;
	}
}
