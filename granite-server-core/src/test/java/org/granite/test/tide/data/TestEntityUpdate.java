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
package org.granite.test.tide.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.granite.tide.data.DataContext.EntityUpdate;
import org.granite.tide.data.DataContext.EntityUpdateType;
import org.granite.util.UUIDUtil;
import org.junit.Assert;
import org.junit.Test;

public class TestEntityUpdate {
	
	@Test
	public void testSortUpdates() {
        Random random = new Random();

        for (int count = 0; count < 20; count++) {
            List<EntityUpdate> updates = new ArrayList<EntityUpdate>();
            for (int i = 0; i < 10000; i++) {
                EntityUpdateType type = EntityUpdateType.UPDATE;
                Object entity = random.nextBoolean() ? new Patient(random.nextLong(), 0L, UUIDUtil.randomUUID()) : new Medication(random.nextLong(), 0L, UUIDUtil.randomUUID());
                updates.add(new EntityUpdate(type, entity, entity, 0));
            }

            try {
                Collections.sort(updates);
                // No violation contract!!
            }
            catch (IllegalArgumentException e) {
                Assert.fail(e.getMessage());
            }
        }
	}
}
