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
package org.granite.tide.data.model;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;



public class SortInfo implements Externalizable {
	
	private static final long serialVersionUID = 1L;
	
	private String[] order;
	private boolean[] desc;
	
	
	public SortInfo() {		
	}
	
	public SortInfo(String[] order, boolean[] desc) {
		this.order = order;
		this.desc = desc;
	}
	
	
	public String[] getOrder() {
		return order;
	}
	public void setOrder(String[] order) {
		this.order = order;
	}
	
	public boolean[] getDesc() {
		return desc;
	}
	public void setDesc(boolean[] desc) {
		this.desc = desc;
	} 

	
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(order);
		out.writeObject(desc);
	}

	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		Object[] oorder = (Object[])in.readObject();
		Object[] odesc = (Object[])in.readObject();
		order = new String[oorder.length];
		desc = new boolean[oorder.length];
		int i = 0;
		for (Object o : oorder)
			order[i++] = (String)o;
		i = 0;
		for (Object d : odesc)
			desc[i++] = (Boolean)d;
	}

}
