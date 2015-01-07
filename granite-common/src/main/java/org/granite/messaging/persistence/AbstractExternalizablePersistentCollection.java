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
import java.io.ObjectOutput;
import java.util.Collection;

/**
 * @author Franck WOLFF
 */
public abstract class AbstractExternalizablePersistentCollection implements Externalizable {

    private static final long serialVersionUID = 1L;
    
    private static final Object[] OBJECT_0 = new Object[0]; 

    protected boolean initialized;
    protected String metadata;
    protected boolean dirty;
    protected Object[] content;

    public AbstractExternalizablePersistentCollection() {
    	this(OBJECT_0, true, false);
    }
    
    public AbstractExternalizablePersistentCollection(Object[] content, boolean initialized, boolean dirty) {
    	this.content = content;
        this.initialized = initialized;
        this.dirty = dirty;
    }

	public boolean isInitialized() {
        return initialized;
    }
    protected void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

    public String getMetadata() {
		return metadata;
	}

	public void setMetadata(String metadata) {
		this.metadata = metadata;
	}

	public boolean isDirty() {
        return dirty;
    }
    public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}

    public Object[] getContent() {
		return content;
	}
	public void setContent(Object[] content) {
		this.content = content;
	}

	public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(Boolean.valueOf(initialized));
        out.writeObject(metadata);
        if (initialized) {
            out.writeObject(Boolean.valueOf(dirty));
            out.writeObject(content);
        }
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        initialized = ((Boolean)in.readObject()).booleanValue();
        metadata = (String)in.readObject();
        if (initialized) {
            dirty = ((Boolean)in.readObject()).booleanValue();
            
            Object o = in.readObject();
            if (o != null) {
	            if (o instanceof Collection<?>)
	            	content = ((Collection<?>)o).toArray();
	            else if (o.getClass().isArray())
	            	content = (Object[])o;
	            else
	            	content = new Object[]{o}; // should never happened...
            }
        }
    }
}
