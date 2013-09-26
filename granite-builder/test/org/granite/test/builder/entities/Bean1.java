package org.granite.test.builder.entities;

import java.util.ArrayList;
import java.util.List;



public class Bean1 {
	
	private String name;
	private List<String> list = new ArrayList<String>();
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public List<String> getList() {
		return list;
	}
	public void setList(List<String> list) {
		this.list = list;
	}
}
