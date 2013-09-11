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

package org.granite.tide.data;

import java.util.List;

import org.granite.tide.data.DataContext.EntityUpdate;


/**
 *  Interface for data update postprocessors.
 *  Allows processing of updated entities before the updates are dispatched or returned to the clients.
 *  Can be attached to the <code>DataContext</code> or lookup up by injection when using a DI framework.
 * 
 *  @see DataContext
 * 
 *  @author William Drai
 */
public interface DataUpdatePostprocessor {
	
	public List<EntityUpdate> process(List<EntityUpdate> updates);
}
