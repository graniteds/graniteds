/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2014 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of the Granite Data Services Platform.
 *
 *                               ***
 *
 *   Community License: GPL 3.0
 *
 *   This file is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published
 *   by the Free Software Foundation, either version 3 of the License,
 *   or (at your option) any later version.
 *
 *   This file is distributed in the hope that it will be useful, but
 *   WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *                               ***
 *
 *   Available Commercial License: GraniteDS SLA 1.0
 *
 *   This is the appropriate option if you are creating proprietary
 *   applications and you are not prepared to distribute and share the
 *   source code of your application under the GPL v3 license.
 *
 *   Please visit http://www.granitedataservices.com/license for more
 *   details.
 */
package org.granite.client.tide;


/**
 * @author William DRAI
 */
public interface ViewScope {
	
	public String getViewId();
	
	public void ensureViewId(String viewId);
	
	public Object get(String name);
	
	public void put(String name, Object instance);
	
	public Object remove(String name);
	
	public void reset(Class<?> type);
	
	public void reset();

	public void addResetter(String name, BeanResetter resetter);
	
	public void setResetter(GlobalResetter resetter);
	
	public static interface BeanResetter {
		
		public void reset(Object instance);
	}
	
	public static interface GlobalResetter {
		
		public void reset(String name, Object instance);
	}
	
}
