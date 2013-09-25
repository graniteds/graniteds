/*
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
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
package org.granite.test.tide.data
{
    import org.flexunit.Assert;
    
    import mx.collections.ArrayCollection;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.tide.events.TideUIConversationEvent;
    import org.granite.test.tide.Person;
    
    
    public class TestMergeEntityConversation4 
    {
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();            
            _ctx = Tide.getInstance().getContext();
            Tide.getInstance().initApplication();
            Tide.getInstance().addComponents([BaseEntityConversation, EntityConversation]);
        }
        
        
        [Test]
        public function testMergeEntityConversation4():void {
        	var person:Person = new Person();
        	person.id = 1;
        	person.version = 0;
        	person.uid = "P1";
        	person.lastName = "Jojo";
        	person.contacts = new ArrayCollection();
        	_ctx.person = _ctx.meta_mergeExternalData(person, null);
        	
        	_ctx.dispatchEvent(new TideUIConversationEvent("1", "start", person));
        	
        	_ctx.dispatchEvent(new TideUIConversationEvent("2", "start", person));
        	
        	_ctx.dispatchEvent(new TideUIConversationEvent("3", "start", person));
        	
        	_ctx.dispatchEvent(new TideUIConversationEvent("1", "endMerge"));
        	
        	Assert.assertEquals("global person not updated", "Jojo", person.lastName);
        	
        	_ctx.dispatchEvent(new TideUIConversationEvent("2", "endMerge2"));
        	
        	Assert.assertEquals("global person updated", "Juju", person.lastName);
        	Assert.assertEquals("intermediate 3 person updated", "Juju", Tide.getInstance().getContext("3").person.lastName);
        	Assert.assertEquals("conversation 3.1 person updated", "Juju", Tide.getInstance().getContext("3.1").person.lastName);
        }
    }
}
