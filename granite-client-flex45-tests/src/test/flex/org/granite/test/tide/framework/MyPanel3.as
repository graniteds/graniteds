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
package org.granite.test.tide.framework {
	
	import mx.collections.ArrayCollection;
	import mx.collections.IList;
	import mx.containers.Panel;
	import mx.events.PropertyChangeEvent;
	

	[Name]
    public class MyPanel3 extends Panel implements IList {
		
		private var list:ArrayCollection = new ArrayCollection();
    	
    	public var triggered:Boolean = false;
    	
    	[Observer("someEvent2")]
    	public function eventHandler():void {
    		triggered = true;
    	}
		
		public function toArray():Array {
			return list.toArray();
		}
		public function get length():int {
			return list.length;
		}
		public function getItemAt(index:int, prefetch:int = 0):Object {
			return list.getItemAt(index, prefetch);
		}
		public function getItemIndex(item:Object):int {
			return list.getItemIndex(item);
		}
		public function addItem(item:Object):void {
			list.addItem(item);
		}
		public function addItemAt(item:Object, index:int):void {
			list.addItemAt(item, index);
		}
		public function setItemAt(item:Object, index:int):Object {
			return list.setItemAt(item, index);
		}
		public function removeItemAt(index:int):Object {
			return list.removeItemAt(index);
		}
		public function removeAll():void {
			list.removeAll();
		}
		public function itemUpdated(item:Object, property:Object = null, oldValue:Object = null, newValue:Object = null):void {
			list.itemUpdated(item, property, oldValue, newValue);
		}
    }
}
