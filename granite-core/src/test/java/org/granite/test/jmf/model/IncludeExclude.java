/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of Granite Data Services.
 *
 *   Granite Data Services is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU Library General Public License as published by
 *   the Free Software Foundation; either version 2 of the License, or (at your
 *   option) any later version.
 *
 *   Granite Data Services is distributed in the hope that it will be useful, but
 *   WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *   FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
 *   for more details.
 *
 *   You should have received a copy of the GNU Library General Public License
 *   along with this library; if not, see <http://www.gnu.org/licenses/>.
 */
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
