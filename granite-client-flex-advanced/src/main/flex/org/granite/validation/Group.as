/*
  GRANITE DATA SERVICES
  Copyright (C) 2011 GRANITE DATA SERVICES S.A.S.

  This file is part of Granite Data Services.

  Granite Data Services is free software; you can redistribute it and/or modify
  it under the terms of the GNU Library General Public License as published by
  the Free Software Foundation; either version 2 of the License, or (at your
  option) any later version.

  Granite Data Services is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
  for more details.

  You should have received a copy of the GNU Library General Public License
  along with this library; if not, see <http://www.gnu.org/licenses/>.
*/

package org.granite.validation {

	import flash.utils.getQualifiedClassName;
	
	import org.granite.reflect.Type;
	import org.granite.validation.groups.Default;

	/**
	 * @author Franck WOLFF
	 * 
	 * @private
	 */
	public final class Group {

		public static const DEFAULT_GROUP:Group = new Group(Type.forClass(Default));
		
		private var _groupType:Type = null;
		private var _hierarchy:Array = null;
		
		function Group(groupType:Type) {
			if (groupType === null)
				throw new ArgumentError("groupType cannot be null");
			
			_groupType = groupType;
		}
		
		public function get groupType():Type {
			return _groupType;
		}
		
		public function get groupClass():Class {
			return _groupType.getClass();
		}
		
		public function isDefaultGroup():Boolean {
			return (this === DEFAULT_GROUP) || (_groupType.getClass() === Default);
		}
		
		public function contains(group:Class):Boolean {
			return getHierarchy().indexOf(group) != -1;
		}
		
		public function containsOneOf(groupClasses:Array):Boolean {
			const hierarchy:Array = getHierarchy();
			for each (var groupClass:Class in groupClasses) {
				if (hierarchy.indexOf(groupClass) != -1)
					return true;
			}
			return false;
		}
		
		public function equals(o:*):Boolean {
			if (o === this)
				return true;
			if (o === null || !(o is Group))
				return false;
			return _groupType.equals(Group(o)._groupType);
		}
		
		public function toString():String {
			return getQualifiedClassName(groupClass);
		}
		
		private function getHierarchy():Array {
			if (_hierarchy == null) {
				_hierarchy = [_groupType.getClass()];
				if (_groupType.getClass() !== Default) {
					for each (var type:Type in _groupType.interfaces)
						_hierarchy.push(type.getClass());
				}
			}
			return _hierarchy;
		}
	}
}