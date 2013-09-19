/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import static org.granite.tide.data.DataDispatcher.TIDE_DATA_TYPE_KEY;
import static org.granite.tide.data.DataDispatcher.TIDE_DATA_TYPE_VALUE;



public class DataObserveParams implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	
	private Map<String, Set<String>> params = new HashMap<String, Set<String>>();
	private String selector = null;
	
	
	public DataObserveParams() {
	}
	
	private DataObserveParams(Map<String, Set<String>> params, String selector) {
		this.params = params;
		this.selector = selector;
	}
	
	public boolean isEmpty() {
		return selector == null && params.isEmpty();
	}
	
	public boolean addValue(String paramName, String value) {
		if (paramName == null || value == null || paramName.trim().length() == 0 || value.trim().length() == 0)
			throw new NullPointerException("paramName and value cannot be null or empty");
		if (this.selector != null)
			throw new IllegalArgumentException("Cannot mix manual and automatic selectors");
		
		Set<String> values = params.get(paramName);
		if (values == null) {
			values = new HashSet<String>();
			params.put(paramName, values);
		}
		return values.add(value);
	}
	
	public void setSelector(String selector) {
		if (selector == null || selector.trim().length() == 0)
			throw new NullPointerException("selector cannot be null or empty");
		if (!this.params.isEmpty())
			throw new IllegalArgumentException("Cannot mix manual and automatic selectors");
		
		this.selector = selector;
	}
	
	
	public void append(StringBuilder sb) {

		if (selector != null) {
			sb.append("(").append(selector).append(")");
			return;
		}

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
						sb.append(", ");
					sb.append("'").append(value).append("'");
				}
				sb.append(")");
			}
		}
	}
    
    
    private static boolean containsParams(List<DataObserveParams> selectors, DataObserveParams params) {
    	for (DataObserveParams selector : selectors) {
    		if (selector.containsParams(params))
    			return true;
    	}
    	return false;
    }	
    
    private boolean containsParams(DataObserveParams params) {
    	if (this.selector != null && !this.selector.equals(params.selector))
    		return false;
    	
    	if (this.params == null)
    		return params.params == null;
    	if (params.params == null)
    		return true;
    	
    	for (Map.Entry<String, Set<String>> me : params.params.entrySet()) {
    		Set<String> values = this.params.get(me.getKey());
    		if (values == null || !values.containsAll(me.getValue()))
    			return false;
    	}
    	
    	return params.params.keySet().containsAll(this.params.keySet());
    }
    
    public static boolean containsSame(List<DataObserveParams> selectors1, List<DataObserveParams> selectors2) {
    	for (DataObserveParams selector : selectors2) {
    		if (!containsParams(selectors1, selector))
    			return false;
    	}
    	for (DataObserveParams selector : selectors1) {
    		if (!containsParams(selectors2, selector))
    			return false;
    	}
    	return true;
    }

	public String updateDataSelector(String dataSelector, List<DataObserveParams> selectors) {
		if (!containsParams(selectors, this)) {
			if (!isEmpty()) {
				List<DataObserveParams> sels = new ArrayList<DataObserveParams>(selectors);
				selectors.clear();
				for (DataObserveParams s : sels) {
					if (!this.containsParams(s))
						selectors.add(s);
				}
				selectors.add(this);
			}
			
			return buildSelectorString(selectors);
		}
		else if (dataSelector == null) {
			return TIDE_DATA_TYPE_KEY + " = 'UNINITIALIZED'";
		}
		return dataSelector;
	}

	private String buildSelectorString(List<DataObserveParams> selectors) {
		StringBuilder sb = new StringBuilder(TIDE_DATA_TYPE_KEY + " = '" + TIDE_DATA_TYPE_VALUE + "'");
		
		if (!selectors.isEmpty()) {
			sb.append(" AND (");
			boolean first = true;
			for (DataObserveParams selector : selectors) {
				if (first)
					first = false;
				else
					sb.append(" OR ");
				sb.append("(");
				selector.append(sb);
				sb.append(")");
			}
			sb.append(")");
		}
		
		return sb.toString();
	}
	
	public static Object[] toSerializableForm(List<DataObserveParams> selectors) {
		Object[] array = new Object[selectors.size()];
		for (int i = 0; i < selectors.size(); i++) {
			DataObserveParams params = selectors.get(i);
			array[i] = params.selector != null ? params.selector : params.params;
		}
		return array;
	}
	
	@SuppressWarnings("unchecked")
	public static List<DataObserveParams> fromSerializableForm(Object[] array) {
		List<DataObserveParams> selectors = new ArrayList<DataObserveParams>(array != null ? array.length : 5);
		if (array != null) {
			for (int i = 0; i < array.length; i++) {
				selectors.add(array[i] instanceof String 
						? new DataObserveParams(null, (String)array[i])
						: new DataObserveParams((Map<String, Set<String>>)array[i], null));
			}
		}
		return selectors;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		append(sb);
		return sb.toString();
	}
}
