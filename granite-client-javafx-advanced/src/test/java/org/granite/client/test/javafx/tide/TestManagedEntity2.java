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
package org.granite.client.test.javafx.tide;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.collections.FXCollections;

import org.granite.client.javafx.tide.JavaFXDataManager;
import org.granite.client.tide.data.EntityManager;
import org.granite.client.tide.data.impl.EntityManagerImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class TestManagedEntity2 {
    
    private EntityManager entityManager;
    private JavaFXDataManager dataManager;
    
    @Before
    public void setup() throws Exception {
    	dataManager = new JavaFXDataManager();
        entityManager = new EntityManagerImpl("", dataManager);
    }
    
    
    @Test
    public void testMergeMap4() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("resultList", FXCollections.observableArrayList());
        
        Map<String, Object> map2 = new HashMap<String, Object>();
        map2.put("resultCount", 100);
        map2.put("firstResult", 200);
        map2.put("maxResults", 100);
        map2.put("resultList", FXCollections.observableArrayList(new Person(1L, 0L, "P1", "A1", "B1"), new Person(3L, 0L, "P3", "A3", "B3")));
        
        entityManager.mergeExternalData(map2, map, null, null, null);
        
        Assert.assertEquals("Size", 4, map.size());
        Assert.assertEquals("Result count", 100, map.get("resultCount"));
        Assert.assertEquals("Result list", 2, ((List<?>)map.get("resultList")).size());
    }
    
    @Test
    public void testMergeEntityEmbedded() {
        PersonEmbed person = new PersonEmbed(1L, 0L, "P1", null, null);
        person.setAddress(new EmbeddedAddress("toto"));
        
        person = (PersonEmbed)entityManager.mergeExternalData(person);
        
        person.getAddress().setAddress("tutu");
        
        Assert.assertTrue("Context dirty", dataManager.isDirty());
        Assert.assertTrue("Person dirty", dataManager.isDirtyEntity(person));
        
        PersonEmbed person2 = new PersonEmbed(1L, 1L, "P1", null, null);
        person2.setAddress(new EmbeddedAddress("tutu"));
        
        person = (PersonEmbed)entityManager.mergeExternalData(person2);
        
        Assert.assertEquals("Address reset", "tutu", person.getAddress().getAddress());
        Assert.assertFalse("Person not dirty", dataManager.isDirtyEntity(person));
        Assert.assertFalse("Context dirty", entityManager.isDirty());
    }
}
