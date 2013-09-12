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
package org.granite.test.tide;

import java.util.List;

import javax.enterprise.inject.Alternative;

import org.granite.tide.data.DataContext.EntityUpdate;
import org.granite.tide.data.DataUpdatePostprocessor;


@Alternative
public class TestDataUpdatePostprocessor implements DataUpdatePostprocessor {

	@Override
	public List<EntityUpdate> process(List<EntityUpdate> updates) {
		for (EntityUpdate update : updates)
			update.entity = new WrappedUpdate(update.entity);
		return updates;
	}

	public static class WrappedUpdate {
		
		private Object entity;
		
		public WrappedUpdate(Object entity) {
			this.entity = entity;
		}
		
		public Object getEntity() {
			return entity;
		}
	}
}
