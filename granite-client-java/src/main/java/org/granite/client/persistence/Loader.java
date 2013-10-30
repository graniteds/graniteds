/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
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
package org.granite.client.persistence;

import org.granite.client.persistence.collection.PersistentCollection.InitializationCallback;

/**
 * Loader are used to trigger the loading of lazy collections/maps
 *
 * @author William DRAI
 */
public interface Loader<T> {

    /**
     * Called when a lazy collection should be loaded
     * @param object the collection/map
     * @param callback a callback to call when loading is finished
     */
    public void load(T object, InitializationCallback callback);

    /**
     * Called before starting loading
     */
    public void onInitializing();

    /**
     * Called once the loading is done
     */
    public void onInitialize();

    /**
     * Called once the unloading is done
     */
    public void onUninitialize();
}
