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
package org.granite.tide.data;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;


/**
 * @author William DRAI
 */
public class Change implements Externalizable, Comparable<Change> {

    private static final long serialVersionUID = 1L;
    
    private String className;
    private String uid;
    private Serializable id;
    private Number version;
    private Map<String, Object> changes;
    private boolean local;
	
    
    public Change() {
        this.local = false;
    }
    
    public Change(String className, Serializable id, Number version, String uid) {
    	this.className = className;
        this.id = id;
    	this.version = version;
    	this.uid = uid;
    	this.changes = new HashMap<String, Object>();
        this.local = false;
    }

    public Change(String className, Serializable id, Number version, String uid, boolean local) {
        this.className = className;
        this.id = id;
        this.version = version;
        this.uid = uid;
        this.changes = new HashMap<String, Object>();
        this.local = local;
    }

	public String getClassName() {
		return className;
	}

	public String getUid() {
		return uid;
	}

	public Serializable getId() {
		return id;
	}

	public Number getVersion() {
		return version;
	}
	
	public void updateVersion(Number version) {
		this.version = version;
	}

	public Map<String, Object> getChanges() {
		return changes;
	}
	
	public boolean isEmpty() {
		return changes.size() == 0;
	}
	
	public Object getChange(String key) {
		return changes.get(key);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getChange(String key, Class<T> cast) {
		return (T)changes.get(key);
	}
	
	public CollectionChanges getCollectionChange(String key) {
		return (CollectionChanges)changes.get(key);
	}
	
    public boolean isLocal() {
        return local;
    }

	public void addCollectionChanges(String propertyName, CollectionChange... collChanges) {
		CollectionChanges existingChanges = (CollectionChanges)changes.get(propertyName);
		if (existingChanges == null)
			changes.put(propertyName, new CollectionChanges(collChanges));
		else {
			CollectionChange[] newChanges = new CollectionChange[existingChanges.getChanges().length+collChanges.length];
		    System.arraycopy(existingChanges.getChanges(), 0, newChanges, 0, existingChanges.getChanges().length);
		    System.arraycopy(collChanges, 0, newChanges, existingChanges.getChanges().length, collChanges.length);
		    existingChanges.setChanges(newChanges);
		}
	}

    @Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (getClassName().indexOf(".") > 0)
			sb.append(getClassName().substring(getClassName().lastIndexOf(".")+1));
		else
			sb.append(getClassName());
		sb.append(':').append(getUid()).append(":").append(getId()).append(':').append(getVersion()).append("={");
		boolean first = true;
		for (Entry<String, Object> change : getChanges().entrySet()) {
			if (first)
				first = false;
			else
				sb.append(", ");
			sb.append(change.getKey()).append(": ").append(change.getValue());
		}
		sb.append("}");
		return sb.toString();
	}
	
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(className);
		out.writeObject(uid);
		out.writeObject(id);
		out.writeObject(version);
		out.writeObject(changes);
	}

	@SuppressWarnings("unchecked")
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		className = (String)in.readObject();
		uid = (String)in.readObject();
		id = (Serializable)in.readObject();
		version = (Number)in.readObject();
		changes = (Map<String, Object>)in.readObject();
	}

	@Override
	public int compareTo(Change o) {
		if (!className.equals(o.getClassName()))
			return className.compareTo(o.getClassName());
		if (!uid.equals(o.getUid()))
			return uid.compareTo(o.getUid());
		if (version == null)
			return o.getVersion() == null ? 0 : -1;
		else if (version.equals(o.getVersion()))
			return 0;
		else if (o.getVersion() == null)
			return 1;
		return version.longValue() > o.getVersion().longValue() ? 1 : -1;
	}
}
