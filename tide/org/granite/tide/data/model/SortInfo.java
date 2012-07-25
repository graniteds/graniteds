package org.granite.tide.data.model;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;



public class SortInfo implements Externalizable {
	
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
