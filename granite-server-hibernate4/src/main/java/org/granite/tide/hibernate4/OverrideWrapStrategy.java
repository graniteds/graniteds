/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2015 GRANITE DATA SERVICES S.A.S.
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
package org.granite.tide.hibernate4;

import org.hibernate.event.service.spi.DuplicationStrategy;

public class OverrideWrapStrategy<E> implements DuplicationStrategy {

	@SuppressWarnings("unchecked")
	public boolean areMatch(Object listener, Object original) {
		Class<?> clazz = original.getClass();
		while (clazz != null && !clazz.equals(Object.class)) {
			for (Class<?> spiInterface : clazz.getInterfaces()) {
				if (spiInterface.getName().startsWith("org.hibernate.event.spi.")) {
					if (listener instanceof EventListenerWrapper<?> && spiInterface.isInstance(listener)) {
						((EventListenerWrapper<E>)listener).setWrappedListener((E)original);
						return true;
					}
				}				
			}
			clazz = clazz.getSuperclass();
		}
		return false;
	}
	
	public Action getAction() {
		return Action.REPLACE_ORIGINAL;
	}
	
}