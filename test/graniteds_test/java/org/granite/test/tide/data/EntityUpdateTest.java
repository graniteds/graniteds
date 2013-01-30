package org.granite.test.tide.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import junit.framework.Assert;

import org.granite.tide.data.DataContext.EntityUpdate;
import org.granite.tide.data.DataContext.EntityUpdateType;
import org.junit.Test;

public class EntityUpdateTest {
	
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
