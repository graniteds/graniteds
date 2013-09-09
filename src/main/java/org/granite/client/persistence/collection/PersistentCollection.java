/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of Granite Data Services.
 *
 *   Granite Data Services is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU Library General Public License as published by
 *   the Free Software Foundation; either version 2 of the License, or (at your
 *   option) any later version.
 *
 *   Granite Data Services is distributed in the hope that it will be useful, but
 *   WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *   FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
 *   for more details.
 *
 *   You should have received a copy of the GNU Library General Public License
 *   along with this library; if not, see <http://www.gnu.org/licenses/>.
 */
package org.granite.client.persistence.collection;

import java.io.Externalizable;

import org.granite.client.persistence.Loader;


/**
 * @author Franck WOLFF
 */
public interface PersistentCollection extends Externalizable {
	
	boolean wasInitialized();
	void uninitialize();
	void initialize();
	void initializing();
	
	PersistentCollection clone(boolean uninitialize);
	
	Loader<PersistentCollection> getLoader();
	void setLoader(Loader<PersistentCollection> loader);
	
	boolean isDirty();
	void dirty();
	void clearDirty();
	
    public interface ChangeListener {
        
        public void changed(PersistentCollection collection);
    }
    
    public void addListener(ChangeListener listener);
    
    public void removeListener(ChangeListener listener);
    
    
    public interface InitializationListener {
        
        public void initialized(PersistentCollection collection);
        
        public void uninitialized(PersistentCollection collection);
    }
    
    public void addListener(InitializationListener listener);
    
    public void removeListener(InitializationListener listener);
    
    public void withInitialized(InitializationCallback callback);
    
    public interface InitializationCallback {
        
        public void call(PersistentCollection collection);
    }

}
