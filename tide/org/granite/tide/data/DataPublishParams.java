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
import java.util.Map;

import flex.messaging.messages.AsyncMessage;



public class DataPublishParams {
	
	private Map<String, String> params = new HashMap<String, String>();
	private Map<String, Integer> priorities = new HashMap<String, Integer>(); 
	
	
	public void setValue(String paramName, String value) {
		setValue(paramName, value, 0);
	}
	public void setValue(String paramName, String value, int priority) {
		if (paramName == null || value == null)
			throw new NullPointerException("paramName and value cannot be null");
		
		Integer p = priorities.get(paramName);
		if (p == null || p < priority) {
			params.put(paramName, value);
			priorities.put(paramName, priority);
		}
	}
	
	
	public void setHeaders(AsyncMessage message) {
		for (Map.Entry<String, String> me : params.entrySet())
			message.setHeader(me.getKey(), me.getValue());
	}
}
