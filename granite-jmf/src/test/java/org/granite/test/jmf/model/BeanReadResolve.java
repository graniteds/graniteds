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
package org.granite.test.jmf.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.LinkedHashMap;

public class BeanReadResolve implements Serializable {

	private static final long serialVersionUID = 1L;

	private int value = 0;
	
	// Just to make sure that the cache of stored objects isn't messed up...
	@SuppressWarnings("unused")
	private Object[] dummies;
	
	public BeanReadResolve() {
	}
	
	public BeanReadResolve(long value) {
		this.value = (int)value;
		
		Dummy dummy = new Dummy();
		this.dummies = new Object[] {
			dummy,
			new BigDecimal(4),
			new Dummy(),
			dummy,
			new LinkedHashMap<Object, Object>()
		};
	}
	
	private Object readResolve() {
		return new BeanWriteReplace(value);
	}
}
