/*
  GRANITE DATA SERVICES
  Copyright (C) 2011 GRANITE DATA SERVICES S.A.S.

  This file is part of Granite Data Services.

  Granite Data Services is free software; you can redistribute it and/or modify
  it under the terms of the GNU Library General Public License as published by
  the Free Software Foundation; either version 2 of the License, or (at your
  option) any later version.

  Granite Data Services is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
  for more details.

  You should have received a copy of the GNU Library General Public License
  along with this library; if not, see <http://www.gnu.org/licenses/>.
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
public class Change implements Externalizable {
    
    private String className;
    private String uid;
    private Serializable id;
    private Number version;
    private Map<String, Object> changes;
	
    
    public Change() {
    }
    
    public Change(String className, Serializable id, Number version, String uid) {
    	this.className = className;
    	this.id = id;
    	this.version = version;
    	this.uid = uid;
    	this.changes = new HashMap<String, Object>();
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

	public Map<String, Object> getChanges() {
		return changes;
	}

	public void addCollectionChanges(String propertyName, CollectionChange[] collChanges) {
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
		sb.append(getClassName()).append(':').append(getUid()).append(":").append(getId()).append(':').append(getVersion()).append("={");
		for (Entry<String, Object> change : getChanges().entrySet()) {
			sb.append(change.getKey()).append(": ").append(change.getValue() != null ? change.getValue().toString() : "null");
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
}
