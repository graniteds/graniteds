/*
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
package org.granite.test.tide.data {
	
	import mx.collections.ArrayCollection;
	
	import org.granite.tide.BaseContext;
	import org.granite.tide.events.TideUIConversationEvent;
	import org.granite.test.tide.Contact;
	import org.granite.test.tide.Person;
	

	[Name("baseEntityConversation", scope="conversation")]
	[Bindable]
    public class BaseEntityConversation {
    	
    	[Inject]
    	public var context:BaseContext;
    	
    	[Observer("start")]
    	public function start(person:Person):void {
    		context.person = person;
    		dispatchEvent(new TideUIConversationEvent(context.contextId + "." + person.id, "startConversation", person));
    	}
    	
    	[Observer("endMerge")]
    	public function endMerge():void {
    		dispatchEvent(new TideUIConversationEvent(context.contextId + "." + context.person.id, "endMergeConversation"));
    		context.meta_end(true);
    	}
    	
    	[Observer("endMerge2")]
    	public function endMerge2():void {
    		dispatchEvent(new TideUIConversationEvent(context.contextId + "." + context.person.id, "endMergeConversation2"));
    		context.meta_end(true);
    	}
    }
}
