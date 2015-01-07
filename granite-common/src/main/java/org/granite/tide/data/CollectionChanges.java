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


/**
 * @author William DRAI
 */
public class CollectionChanges implements Externalizable {

    private static final long serialVersionUID = 1L;

    private CollectionChange[] changes;
    
    public CollectionChanges() { 
    }
    
    public CollectionChanges(CollectionChange... changes) {
    	this.changes = changes;
    }

	public CollectionChange[] getChanges() {
		return changes;
	}
	
	public void setChanges(CollectionChange... changes) {
		this.changes = changes;
	}
	
	public int size() {
		return changes.length;
	}
	
	public CollectionChange getChange(int index) {
		return changes[index];
	}
	
	public int getChangeType(int index) {
		return changes[index].getType();
	}
	
	public Object getChangeKey(int index) {
		return changes[index].getKey();
	}
	
	@SuppressWarnings("unchecked")
	public <K> K getChangeKey(int index, Class<K> cast) {
		return (K)changes[index].getKey();
	}
	
	public Object getChangeValue(int index) {
		return changes[index].getValue();
	}
	
	@SuppressWarnings("unchecked")
	public <V> V getChangeValue(int index, Class<V> cast) {
		return (V)changes[index].getValue();
	}
	
	public void writeExternal(ObjectOutput out) throws IOException {
		Object[] cs = new Object[changes.length];
		for (int i = 0; i < cs.length; i++)
			cs[i] = changes[i];
		out.writeObject(cs);
	}

	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		Object[] cs = (Object[])in.readObject();
		changes = new CollectionChange[cs.length];
		for (int i = 0; i < cs.length; i++)
			changes[i] = (CollectionChange)cs[i];
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("CollectionChanges[");
		boolean first = true;
		for (CollectionChange cc : changes) {
			if (first)
				first = false;
			else
				sb.append(", ");
			sb.append(cc.toString());
		}
		sb.append("]");
		return sb.toString();
	}
}
