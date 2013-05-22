package org.granite.test.jmf.model;

import java.io.Serializable;

import org.granite.messaging.annotations.Exclude;
import org.granite.messaging.annotations.Include;

public class IncludeExclude implements Serializable {

	private static final long serialVersionUID = 1L;

	private boolean normal;
	
	@Exclude
	private String exclude;
	
	@Include
	public String getComputedData() {
		return "included extra pseudo-field";
	}
	
	@Include
	public boolean isComputed() {
		return true;
	}

	public boolean isNormal() {
		return normal;
	}

	public void setNormal(boolean normal) {
		this.normal = normal;
	}

	public String getExclude() {
		return exclude;
	}

	public void setExclude(String exclude) {
		this.exclude = exclude;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof IncludeExclude))
			return false;
		
		IncludeExclude b = (IncludeExclude)obj;
		
		return normal == b.normal;
	}
}
