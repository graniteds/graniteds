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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;



public class DataObserveParams {
	
	private Map<String, Set<String>> params = new HashMap<String, Set<String>>();
	
	
	public boolean isEmpty() {
		return params.isEmpty();
	}
	
	public Set<String> getParamNames() {
		return params.keySet();
	}
	
	public boolean addValue(String paramName, String value) {
		if (paramName == null || value == null)
			throw new NullPointerException("paramName and value cannot be null");
		Set<String> values = params.get(paramName);
		if (values == null) {
			values = new HashSet<String>();
			params.put(paramName, values);
		}
		return values.add(value);
	}
	
	
	public void append(StringBuilder sb) {
		boolean f = true;
		for (Map.Entry<String, Set<String>> me : params.entrySet()) {
			if (f)
				f = false;
			else
				sb.append(" AND ");
			
			Set<String> values = me.getValue();
			if (values.size() == 1)
				sb.append(me.getKey()).append(" = '").append(values.iterator().next()).append("'");
			else {
				sb.append(me.getKey()).append(" IN (");
				boolean ff = true;
				for (String value : values) {
					if (ff)
						ff = false;
					else
						sb.append(",");
					sb.append("'").append(value).append("'");
				}
				sb.append(")");
			}
		}
	}
    
    
    public static boolean containsParams(List<DataObserveParams> selectors, DataObserveParams params) {
    	for (DataObserveParams selector : selectors) {
    		if (selector.containsParams(params))
    			return true;
    	}
    	return false;
    }	
    
    private boolean containsParams(DataObserveParams params) {
    	for (Map.Entry<String, Set<String>> me : params.params.entrySet()) {
    		if (!this.params.get(me.getKey()).containsAll(me.getValue()))
    			return false;
    	}
    	return true;
    }
}
