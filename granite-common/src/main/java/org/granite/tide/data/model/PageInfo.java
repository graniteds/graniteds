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


public class PageInfo implements Externalizable {
	
	private static final long serialVersionUID = 1L;
	
	private int firstResult;
	private int maxResults;
	private SortInfo sortInfo;
	
	
	public PageInfo() {		
	}
	
    public PageInfo(int firstResult, int maxResults) {
    	this.firstResult = firstResult;
    	this.maxResults = maxResults;
    	this.sortInfo = null;
    }
    
	public PageInfo(int firstResult, int maxResults, String[] order, boolean[] desc) {
		this.firstResult = firstResult;
		this.maxResults = maxResults;
		this.sortInfo = order != null && desc != null && order.length > 0 && desc.length > 0 ? new SortInfo(order, desc) : null;
	}
	
	
	public int getFirstResult() {
		return firstResult;
	}
	public void setFirstResult(int firstResult) {
		this.firstResult = firstResult;
	}
	
	public int getMaxResults() {
		return maxResults;
	}
	public void setMaxResults(int maxResults) {
		this.maxResults = maxResults;
	}
	
	public SortInfo getSortInfo() {
		return sortInfo;
	}
	public void setSortInfo(SortInfo sortInfo) {
		this.sortInfo = sortInfo;
	}
	

	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(firstResult);
		out.writeObject(maxResults);
		out.writeObject(sortInfo != null ? sortInfo.getOrder() : null);
		out.writeObject(sortInfo != null ? sortInfo.getDesc() : null);
	}

	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		firstResult = (Integer)in.readObject();
		maxResults = (Integer)in.readObject();
		Object[] oorder = (Object[])in.readObject();
		Object[] odesc = (Object[])in.readObject();
		if (oorder == null || odesc == null) {
			sortInfo = null;
			return;
		}
		String[] order = new String[oorder.length];
		boolean[] desc = new boolean[oorder.length];
		int i = 0;
		for (Object o : oorder)
			order[i++] = (String)o;
		i = 0;
		for (Object d : odesc)
			desc[i++] = (Boolean)d;
		sortInfo = new SortInfo(order, desc);
	}

}
