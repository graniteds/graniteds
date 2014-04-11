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
	public final int find(Entry head, int hash, Object o) {
        do {
        	if (head.o == o)
        		return head.index;
        }
        while ((head = head.next) != null);
        return -1;
	}
}
