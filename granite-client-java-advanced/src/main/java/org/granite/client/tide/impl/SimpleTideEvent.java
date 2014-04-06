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
package org.granite.client.tide.impl;

import java.util.EventObject;

import org.granite.client.tide.Context;
import org.granite.client.tide.events.TideEvent;

/**
 * @author William DRAI
 */
public class SimpleTideEvent extends EventObject implements TideEvent {

	private static final long serialVersionUID = 1L;

	private final String type;
	private final Object[] args;
	
	
	public SimpleTideEvent(Context context, String type, Object... args) {
		super(context);
		this.type = type;
		this.args = args;
	}
	
	public Context getContext() {
		return (Context)getSource();
	}
	
	public String getType() {
		return type;
	}
	
	public Object[] getArgs() {
		return args;
	}
}
