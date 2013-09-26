/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of the Granite Data Services Platform.
 *
 *                               ***
 *
 *   Community License: GPL 3.0
 *
 *   This file is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published
 *   by the Free Software Foundation, either version 3 of the License,
 *   or (at your option) any later version.
 *
 *   This file is distributed in the hope that it will be useful, but
 *   WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *                               ***
 *
 *   Available Commercial License: GraniteDS SLA 1.0
 *
 *   This is the appropriate option if you are creating proprietary
 *   applications and you are not prepared to distribute and share the
 *   source code of your application under the GPL v3 license.
 *
 *   Please visit http://www.granitedataservices.com/license for more
 *   details.
 */
package org.granite.client.javafx.persistence.collection;

import javafx.beans.property.ReadOnlyListWrapper;
import javafx.beans.property.ReadOnlyMapWrapper;
import javafx.beans.property.ReadOnlySetWrapper;

import org.granite.client.persistence.collection.PersistentBag;
import org.granite.client.persistence.collection.PersistentList;
import org.granite.client.persistence.collection.PersistentMap;
import org.granite.client.persistence.collection.PersistentSet;
import org.granite.client.persistence.collection.PersistentSortedMap;
import org.granite.client.persistence.collection.PersistentSortedSet;

/**
 * @author Franck WOLFF
 */
public abstract class FXPersistentCollections {
	
	///////////////////////////////////////////////////////////////////////////
	// Observable Persistent Lists 
	
	public static <E> ObservablePersistentList<E> observablePersistentList() {
		return new ObservablePersistentList<E>(new PersistentList<E>(true));
	}
	
	public static <E> ObservablePersistentList<E> observablePersistentList(PersistentList<E> persistentList) {
		return new ObservablePersistentList<E>(persistentList);
	}
	
	public static <E> ReadOnlyListWrapper<E> readOnlyObservablePersistentList(Object bean, String name) {
		return new ReadOnlyListWrapper<E>(bean, name, new ObservablePersistentList<E>(new PersistentList<E>(true)));
	}
	
	public static <E> ReadOnlyListWrapper<E> readOnlyObservablePersistentList(Object bean, String name, PersistentList<E> persistentList) {
		return new ReadOnlyListWrapper<E>(bean, name, new ObservablePersistentList<E>(persistentList));
	}
	
	///////////////////////////////////////////////////////////////////////////
	// Observable Persistent Bags 
	
	public static <E> ObservablePersistentBag<E> observablePersistentBag() {
		return new ObservablePersistentBag<E>(new PersistentBag<E>(true));
	}
	
	public static <E> ObservablePersistentBag<E> observablePersistentBag(PersistentBag<E> persistentBag) {
		return new ObservablePersistentBag<E>(persistentBag);
	}
	
	public static <E> ReadOnlyListWrapper<E> readOnlyObservablePersistentBag(Object bean, String name) {
		return new ReadOnlyListWrapper<E>(bean, name, new ObservablePersistentBag<E>(new PersistentBag<E>()));
	}
	
	public static <E> ReadOnlyListWrapper<E> readOnlyObservablePersistentBag(Object bean, String name, PersistentBag<E> persistentBag) {
		return new ReadOnlyListWrapper<E>(bean, name, new ObservablePersistentBag<E>(persistentBag));
	}
	
	///////////////////////////////////////////////////////////////////////////
	// Observable Persistent Sets 
	
	public static <E> ObservablePersistentSet<E> observablePersistentSet() {
		return new ObservablePersistentSet<E>(new PersistentSet<E>(true));
	}
	
	public static <E> ObservablePersistentSet<E> observablePersistentSet(PersistentSet<E> persistentSet) {
		return new ObservablePersistentSet<E>(persistentSet);
	}
	
	public static <E> ReadOnlySetWrapper<E> readOnlyObservablePersistentSet(Object bean, String name) {
		return new ReadOnlySetWrapper<E>(bean, name, new ObservablePersistentSet<E>(new PersistentSet<E>(true)));
	}
	
	public static <E> ReadOnlySetWrapper<E> readOnlyObservablePersistentSet(Object bean, String name, PersistentSet<E> persistentSet) {
		return new ReadOnlySetWrapper<E>(bean, name, new ObservablePersistentSet<E>(persistentSet));
	}
	
	///////////////////////////////////////////////////////////////////////////
	// Observable Persistent Sorted Sets 
	
	public static <E> ObservablePersistentSortedSet<E> observablePersistentSortedSet() {
		return new ObservablePersistentSortedSet<E>(new PersistentSortedSet<E>(true));
	}
	
	public static <E> ObservablePersistentSortedSet<E> observablePersistentSortedSet(PersistentSortedSet<E> persistentSortedSet) {
		return new ObservablePersistentSortedSet<E>(persistentSortedSet);
	}
	
	public static <E> ReadOnlySetWrapper<E> readOnlyObservablePersistentSortedSet(Object bean, String name) {
		return new ReadOnlySetWrapper<E>(bean, name, new ObservablePersistentSortedSet<E>(new PersistentSortedSet<E>(true)));
	}
	
	public static <E> ReadOnlySetWrapper<E> readOnlyObservablePersistentSortedSet(Object bean, String name, PersistentSortedSet<E> persistentSortedSet) {
		return new ReadOnlySetWrapper<E>(bean, name, new ObservablePersistentSortedSet<E>(persistentSortedSet));
	}
	
	///////////////////////////////////////////////////////////////////////////
	// Observable Persistent Maps 
	
	public static <K, V> ObservablePersistentMap<K, V> observablePersistentMap() {
		return new ObservablePersistentMap<K, V>(new PersistentMap<K, V>(true));
	}
	
	public static <K, V> ObservablePersistentMap<K, V> observablePersistentMap(PersistentMap<K, V> persistentMap) {
		return new ObservablePersistentMap<K, V>(persistentMap);
	}
	
	public static <K, V> ReadOnlyMapWrapper<K, V> readOnlyObservablePersistentMap(Object bean, String name) {
		return new ReadOnlyMapWrapper<K, V>(bean, name, new ObservablePersistentMap<K, V>(new PersistentMap<K, V>(true)));
	}
	
	public static <K, V> ReadOnlyMapWrapper<K, V> readOnlyObservablePersistentMap(Object bean, String name, PersistentMap<K, V> persistentMap) {
		return new ReadOnlyMapWrapper<K, V>(bean, name, new ObservablePersistentMap<K, V>(persistentMap));
	}
	
	///////////////////////////////////////////////////////////////////////////
	// Observable Persistent Sorted Maps 
	
	public static <K, V> ObservablePersistentSortedMap<K, V> observablePersistentSortedMap() {
		return new ObservablePersistentSortedMap<K, V>(new PersistentSortedMap<K, V>(true));
	}
	
	public static <K, V> ObservablePersistentSortedMap<K, V> observablePersistentSortedMap(PersistentSortedMap<K, V> persistentSortedMap) {
		return new ObservablePersistentSortedMap<K, V>(persistentSortedMap);
	}
	
	public static <K, V> ReadOnlyMapWrapper<K, V> readOnlyObservablePersistentSortedMap(Object bean, String name) {
		return new ReadOnlyMapWrapper<K, V>(bean, name, new ObservablePersistentSortedMap<K, V>(new PersistentSortedMap<K, V>(true)));
	}
	
	public static <K, V> ReadOnlyMapWrapper<K, V> readOnlyObservablePersistentSortedMap(Object bean, String name, PersistentSortedMap<K, V> persistentSortedMap) {
		return new ReadOnlyMapWrapper<K, V>(bean, name, new ObservablePersistentSortedMap<K, V>(persistentSortedMap));
	}
}
