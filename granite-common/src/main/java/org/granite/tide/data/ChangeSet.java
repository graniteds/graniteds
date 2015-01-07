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
public class ChangeSet implements Externalizable {

    private static final long serialVersionUID = 1L;

	private Change[] changes = new Change[0];
    private final boolean local;
	
	
	public ChangeSet() {
        this.local = false;
	}
	
	public ChangeSet(Change... changes) {
		this.changes = changes;
        this.local = false;
	}

    public ChangeSet(Change[] changes, boolean local) {
        this.changes = changes;
        this.local = local;
    }

    public Change[] getChanges() {
		return changes;
	}
	
	public void setChanges(Change[] changes) {
		this.changes = changes;
	}

    public boolean isLocal() {
        return local;
    }
    
    public int size() {
    	return changes.length;
    }
    
    public Change getChange(int index) {
    	return changes[index];
    }
    
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(changes);
	}
	
	@Override
	public String toString() {
		return getChanges() != null ? getChanges().toString() : "[]";
	}
	
//	public static valueToString(Object object) {
//		if (object == null)
//			return "null";
//		
//        ClassGetter classGetter = GraniteContext.getCurrentInstance().getGraniteConfig().getClassGetter();
//		if (!classGetter.isEntity(object))
//			return object.toString();
//		
//		StringBuilder sb = new StringBuilder("{ ");
//		List<Object[]> values = classGetter.getFieldValues(object);
//		for (Object[] value : values) {
//			if (classGetter.isEntity(value[1]))
//				
//		}
//		sb.append(" }");
//	}

	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		Object[] cs = (Object[])in.readObject();
		changes = new Change[cs.length];
		for (int i = 0; i < cs.length; i++)
			changes[i] = (Change)cs[i];
	}
}
