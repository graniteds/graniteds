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
package org.granite.messaging.persistence;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;

/**
 * @author Franck WOLFF
 */
public interface PersistentCollectionSnapshot extends Externalizable {

	boolean isInitialized();
	
	String getDetachedState();

	boolean isDirty();

	boolean isSorted();

	// String getComparatorClassName();

	<T> Comparator<T> newComparator(ObjectInput in)
		throws ClassNotFoundException, InstantiationException, IllegalAccessException,
		InvocationTargetException, SecurityException, NoSuchMethodException;

	<T> Collection<T> getElementsAsCollection();

	<K, V> Map<K, V> getElementsAsMap();

	void readInitializationData(ObjectInput in) throws IOException, ClassNotFoundException;

	void readCoreData(ObjectInput in) throws IOException, ClassNotFoundException;
}