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
package org.granite.test.tide.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.granite.tide.data.DataContext.EntityUpdate;
import org.granite.tide.data.DataContext.EntityUpdateType;
import org.junit.Assert;
import org.junit.Test;

public class TestEntityUpdate {
	
	@Test
	public void testSortUpdates() {
		List<EntityUpdate> updates = new ArrayList<EntityUpdate>();
		updates.add(new EntityUpdate(EntityUpdateType.UPDATE, new Entree(), 0));
		updates.add(new EntityUpdate(EntityUpdateType.UPDATE, new EntreeObs(), 0));
		updates.add(new EntityUpdate(EntityUpdateType.PERSIST, new EntreeObs(), 0));
		updates.add(new EntityUpdate(EntityUpdateType.PERSIST, new Entree(), 0));
		
		Collections.sort(updates);
		
		Assert.assertEquals("PERSIST first", EntityUpdateType.PERSIST, updates.get(0).type);
		Assert.assertEquals("PERSIST second", EntityUpdateType.PERSIST, updates.get(1).type);
	}

}
