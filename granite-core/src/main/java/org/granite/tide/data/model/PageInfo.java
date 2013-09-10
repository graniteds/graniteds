package org.granite.tide.data.model;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;


public class PageInfo implements Externalizable {
	
	private int firstResult;
	private int maxResults;
	private SortInfo sortInfo;
	
	
	public PageInfo() {		
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
