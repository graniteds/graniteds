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
package org.granite.client.persistence.collection;

import java.io.Externalizable;

import org.granite.client.persistence.Loader;


/**
 * Base interface for all persistent collections and maps
 * A loader can be defined to execute a particular behaviour when a lazy collection is accessed
 *
 * @author Franck WOLFF
 */
public interface PersistentCollection extends Externalizable {

    /**
     * Was the collection was initialized
     * @return true if collection initialized
     */
	boolean wasInitialized();

    /**
     * Unload the collection and unset its initialized state
     */
	void uninitialize();

    /**
     * Mark the collection as initialized, called after the content is loaded
     */
	void initialize();

    /**
     * Mark the collection as currently initializing so next access do not trigger loading
     */
	void initializing();

    /**
     * Clone a persistent collection
     * @param uninitialize true to get an uninitialized clone of a lazy collection
     * @return cloned collection
     */
	PersistentCollection clone(boolean uninitialize);

    /**
     * Loader for the collection
     * @return loader or null of none defined
     */
	Loader<PersistentCollection> getLoader();

    /**
     * Set a loader for the collection
     * @param loader loader that will be called when a lazy collection is accessed
     */
	void setLoader(Loader<PersistentCollection> loader);

    /**
     * Is the collection dirty ?
     * @return true if the collection has been modified since last server sync
     */
	boolean isDirty();

    /**
     * Mark the collection as dirty
     */
	void dirty();

    /**
     * Clear the dirty state of the collection
     */
	void clearDirty();

    /**
     * Basic callback interface for change tracking on the collection
     */
    public interface ChangeListener {

        /**
         * Callback called when the collection is modified
         * @param collection collection
         */
        public void changed(PersistentCollection collection);
    }

    /**
     * Register a change listener
     * @param listener listener
     */
    public void addListener(ChangeListener listener);

    /**
     * Unregister a change listener
     * @param listener listener
     */
    public void removeListener(ChangeListener listener);

    /**
     * Basic callback interface for loading/unloading
     */
    public interface InitializationListener {

        /**
         * Callback called when the collection has been loaded
         * @param collection collection
         */
        public void initialized(PersistentCollection collection);

        /**
         * Callback called when the collection has been unloaded
         * @param collection collection
         */
        public void uninitialized(PersistentCollection collection);
    }

    /**
     * Register a initialization listener
     * @param listener listener
     */
    public void addListener(InitializationListener listener);

    /**
     * Unregister a initialization listener
     * @param listener listener
     */
    public void removeListener(InitializationListener listener);

    /**
     * Execute the specified callback after ensuring the collection has been loaded
     * If it was not loaded, the callack is called asynchronously after loading is complete
     * @param callback callback method to execute
     */
    public void withInitialized(InitializationCallback callback);

    /**
     * Callback interface for ensuring working on loaded collections/maps
     */
    public interface InitializationCallback {

        /**
         * Called once the collection has been loaded
         * @param collection collection
         */
        public void call(PersistentCollection collection);
    }

}
