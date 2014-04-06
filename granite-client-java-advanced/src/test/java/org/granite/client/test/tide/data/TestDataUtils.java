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
package org.granite.client.test.tide.data;

import java.util.ArrayList;
import java.util.List;

import org.granite.client.messaging.ClientAliasRegistry;
import org.granite.client.tide.data.EntityManager;
import org.granite.client.tide.data.impl.ChangeEntityRef;
import org.granite.client.tide.data.impl.ObjectUtil;
import org.granite.tide.data.ChangeRef;
import org.granite.tide.data.CollectionChange;
import org.granite.tide.data.CollectionChanges;
import org.junit.Assert;

public class TestDataUtils {
	
    public static void checkListChangeSet(EntityManager entityManager, ClientAliasRegistry aliasRegistry, List<?> coll, CollectionChanges collChanges, List<?> collSnapshot) {
        List<Object> checkList = new ArrayList<Object>(collSnapshot);
        for (CollectionChange collChange : collChanges.getChanges()) {
        	Object value = collChange.getValue();
        	if (value instanceof ChangeRef)
        		value = entityManager.getCachedObject(new ChangeEntityRef(value, aliasRegistry), true);
            if (collChange.getType() == -1)
                checkList.remove(((Integer)collChange.getKey()).intValue());
            else if (collChange.getType() == 1)
                checkList.add(((Integer)collChange.getKey()).intValue(), value);
            else if (collChange.getType() == 0)
                checkList.set(((Integer)collChange.getKey()).intValue(), value);
        }
        
        Assert.assertEquals("Expected list length", coll.size(), checkList.size());
        for (int i = 0; i < coll.size(); i++)
            Assert.assertTrue("Element " + i, ObjectUtil.objectEquals(entityManager.getDataManager(), coll.get(i), checkList.get(i)));
    }

}
