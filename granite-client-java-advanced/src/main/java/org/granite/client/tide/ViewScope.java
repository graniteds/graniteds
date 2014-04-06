/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2014 GRANITE DATA SERVICES S.A.S.
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
package org.granite.client.tide;


/**
 * @author William DRAI
 */
public interface ViewScope {
	
	public String getViewId();
	
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
