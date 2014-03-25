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
