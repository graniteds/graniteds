/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2015 GRANITE DATA SERVICES S.A.S.
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
package org.granite.client.test.javafx.tide;

import org.granite.client.javafx.tide.JavaFXApplication;
import org.granite.client.test.tide.MockInstanceStoreFactory;
import org.granite.client.tide.Context;
import org.granite.client.tide.data.EntityManager;
import org.granite.client.tide.data.spi.DataManager;
import org.granite.client.tide.impl.SimpleContextManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class TestResetEntity2 {

    private SimpleContextManager contextManager;
    private Context ctx;
	private DataManager dataManager;
    private EntityManager entityManager;
    
    @Before
    public void setup() throws Exception {
        contextManager = new SimpleContextManager(new JavaFXApplication());
        contextManager.setInstanceStoreFactory(new MockInstanceStoreFactory());
        ctx = contextManager.getContext("");
        entityManager = ctx.getEntityManager();
        dataManager = ctx.getDataManager();
    }
    
    @Test
    public void testResetEntityEmbedded() {
        PersonEmbed person = new PersonEmbed(1L, 0L, "P1", null, null);
        person.setAddress(new EmbeddedAddress("toto"));
        
        person = (PersonEmbed)entityManager.mergeExternalData(person);
        
        person.getAddress().setAddress("tutu");
        
        Assert.assertTrue("Context dirty", dataManager.isDirty());
        Assert.assertTrue("Person dirty", dataManager.isDirtyEntity(person));
        
        entityManager.resetEntity(person);
        
        Assert.assertEquals("Address reset", "toto", person.getAddress().getAddress());
        Assert.assertFalse("Context dirty", dataManager.isDirty());
        Assert.assertFalse("Person not dirty", dataManager.isDirtyEntity(person));
    }
    
    @Test
    public void testResetEntityEmbeddedNested() {
        PersonEmbedNested person = new PersonEmbedNested(1L, 0L, "P1", null, "Toto");
        person.setAddress(new EmbeddedAddress2("toto"));
        person.getAddress().setLocation(new EmbeddedLocation("PARIS", "75020"));
        
        person = (PersonEmbedNested)entityManager.mergeExternalData(person);
        
        person.getAddress().getLocation().setZipcode("75019");
        
        Assert.assertTrue("Context dirty", dataManager.isDirty());
        Assert.assertTrue("Person dirty", dataManager.isDirtyEntity(person));
        
        entityManager.resetEntity(person);
        
        Assert.assertEquals("Location reset", "75020", person.getAddress().getLocation().getZipcode());
        Assert.assertFalse("Context dirty", dataManager.isDirty());
        Assert.assertFalse("Person not dirty", dataManager.isDirtyEntity(person));
        
        person.setLastName("Truc");
        person.getAddress().setAddress("Bla");
        person.getAddress().getLocation().setCity("LONDON");
        
        Assert.assertTrue("Context dirty", dataManager.isDirty());
        Assert.assertTrue("Person dirty", dataManager.isDirtyEntity(person));
        
        entityManager.resetEntity(person);
        
        Assert.assertEquals("Location reset", "75020", person.getAddress().getLocation().getZipcode());
        Assert.assertEquals("Address reset", "toto", person.getAddress().getAddress());
        Assert.assertEquals("Person reset", "Toto", person.getLastName());
        Assert.assertFalse("Context dirty", dataManager.isDirty());
        Assert.assertFalse("Person not dirty", dataManager.isDirtyEntity(person));
    }
    
}
