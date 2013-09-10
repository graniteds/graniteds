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

	import flash.utils.Dictionary;
	import flash.utils.getQualifiedClassName;
	
	import org.granite.reflect.Annotation;
	import org.granite.reflect.Type;
	import org.granite.validation.groups.Default;
	import org.granite.validation.helper.ConstraintHelper;
	import org.granite.validation.helper.ParameterDefinition;

	/**
	 * @author Franck WOLFF
	 * 
	 * @private
	 */
	public class GroupChain {

		///////////////////////////////////////////////////////////////////////
		// Static constants.

		public static const GROUP_SEQUENCE:String = "GroupSequence";
		
		private static const GROUP_SEQUENCE_VALUE_PARAMETER:Array = [new ParameterDefinition(
			"value",
			Class,
			[],
			true,
			true,
			["javax.validation.groups.Default", "org.granite.validation.groups.Default"]
		)];
		
		private static const GROUPS_CACHE:Dictionary = new Dictionary(true);
		{
			GROUPS_CACHE[Group.DEFAULT_GROUP.groupType] = Group.DEFAULT_GROUP;
		}

		///////////////////////////////////////////////////////////////////////
		// Instance fields.

		private var _factory:ValidatorFactory = null;

		private const _groups:Dictionary = new Dictionary();
		private const _sequences:Dictionary = new Dictionary();
		
		private var _containsDefaultGroup:int = -1;

		///////////////////////////////////////////////////////////////////////
		// Constructor & static factory methods.

		function GroupChain(factory:ValidatorFactory) {
			_factory = factory;
		}
		
		public static function createGroupChain(factory:ValidatorFactory, groups:Array):GroupChain {
			
			var groupChain:GroupChain = new GroupChain(factory),
				groupType:Type,
				sequenceAnnotation:Annotation = null;

			if (groups == null || groups.length == 0 || (groups.length == 1 && groups[0] === Default))
				groupChain._groups[Group.DEFAULT_GROUP.groupType.getClass()] = getGroup(Group.DEFAULT_GROUP.groupType);
			else {
				for each (var group:Class in groups) {
					groupType = Type.forClass(group);
					
					if (!groupType.isInterface())
						throw new ValidationError("Group class '" + groupType.name + "' should be an interface");
					
					if (groupType.isAnnotationPresent(GROUP_SEQUENCE))
						groupChain.addSequence(groupType);
					else
						groupChain.addGroup(groupType);
				}
			}

			return groupChain;
		}
		
		public static function getRedefinedDefaultGroup(factory:ValidatorFactory, type:Type):GroupChain {
			var groupChain:GroupChain = new GroupChain(factory),
				sequence:Array = groupChain.resolveSequence(type, [], true);
			
			groupChain._sequences[type.getClass()] = sequence;
			
			if (!groupChain.containsDefaultGroup())
				throw new GroupDefinitionError("Redefined default group sequence must contain the declaring type: " + type.name);
			
			return groupChain;
		}

		///////////////////////////////////////////////////////////////////////
		// Properties.
		
		public function get groups():Dictionary {
			return _groups;
		}
		
		public function get sequences():Dictionary {
			return _sequences;
		}
		
		public function get uniqueSequence():Array {
			for each (var groups:Array in _sequences)
				return groups;
			return null;
		}

		///////////////////////////////////////////////////////////////////////
		// Group chain methods.
		
		public function getMatchingGroupClasses(groupClasses:Array):Array {
			var matchingGroups:Array = new Array();
			for each (var group:Group in _groups) {
				if (group.containsOneOf(groupClasses) && matchingGroups.indexOf(group.groupClass) == -1)
					matchingGroups.push(group.groupClass);
			}
			for each (var groups:Array in _sequences) {
				for each (group in groups) {
					if (group.containsOneOf(groupClasses) && matchingGroups.indexOf(group.groupClass) == -1)
						matchingGroups.push(group.groupClass);
				}
			}
			return matchingGroups;
		}
		
		public function containsDefaultGroup():Boolean {
			if (_containsDefaultGroup == -1)
				_containsDefaultGroup = getMatchingGroupClasses([Default]).length > 0 ? 1 : 0;
			return _containsDefaultGroup == 0 ? false : true;
		}
		
		protected function addGroup(groupType:Type):void {
			if (!_groups.hasOwnProperty(groupType.getClass()))
				_groups[groupType.getClass()] = getGroup(groupType);
		}
		
		protected function addSequence(groupType:Type):void {
			if (!_sequences.hasOwnProperty(groupType.getClass())) {
				var sequence:Array = resolveSequence(groupType, []);
				_sequences[groupType.getClass()] = sequence;
			}
		}

		///////////////////////////////////////////////////////////////////////
		// Utilities.
		
		public function toString():String {
			var first:Boolean = true,
				s:String = "{groups={";
			
			for (var group:* in _groups) {
				if (first)
					first = false;
				else
					s += ", ";
				s += getQualifiedClassName(group);
			}
			
			s += "}, sequences={";
			first = true;
			for (var sequence:* in _sequences) {
				if (!first)
					s += ", ";
				s += getQualifiedClassName(sequence) + "=[";
				first = true;
				for each (group in _sequences[sequence]) {
					if (first)
						first = false;
					else
						s += ", ";
					s += getQualifiedClassName(Group(group).groupClass);
				}
				first = false;
				s += "]";
			}

			s += "}}";
			return s;
		}
		
		private function resolveSequence(groupSequenceType:Type, processedSequences:Array, redefinedDefault:Boolean = false):Array {
			var annotation:Annotation = groupSequenceType.getAnnotation(GROUP_SEQUENCE);
			if (annotation == null)
				throw new ArgumentError("Not annotated with a " + GROUP_SEQUENCE + ": " + groupSequenceType);
			
			if (processedSequences.indexOf(groupSequenceType) != -1)
				throw new GroupDefinitionError("Cyclic dependency in groups definition");
			
			processedSequences.push(groupSequenceType);

			var groupType:Type,
				resolvedSequences:Array = [],
				newSequence:Array,
				value:Array = ConstraintHelper.parseParameters(
					new GroupSequence(_factory),
					annotation.args,
					GROUP_SEQUENCE_VALUE_PARAMETER
				)[GROUP_SEQUENCE_VALUE_PARAMETER[0].name];

			for each (var group:Class in value) {

				if (redefinedDefault) {
					if (group === Default)
						throw new GroupDefinitionError("Default group cannot be used in a redefined default group sequence");
					else if (group === groupSequenceType.getClass())
						group = Default;
				}

				groupType = Type.forClass(group);
				
				if (!groupType.isInterface())
					throw new ValidationError("Group class '" + groupType.name + "' should be an interface");
				
				if (groupType.isAnnotationPresent(GROUP_SEQUENCE))
					concatSequence(resolvedSequences, resolveSequence(groupType, processedSequences));
				else
					resolvedSequences.push(getGroup(groupType));
			}
			
			return resolvedSequences;
		}
		
		private static function concatSequence(resolvedSequences:Array, newSequence:Array):void {
			for each (var group:Group in newSequence) {

				const
					lastIndex:int = resolvedSequences.length - 1,
					invalid:Boolean = resolvedSequences.some(function(resolvedGroup:Group, index:int, a:Array):Boolean {
						return resolvedGroup.equals(group) && index < lastIndex;
					});
				
				if (invalid)
					throw new GroupDefinitionError("Unable to expand group sequence");
				
				if (resolvedSequences[lastIndex] !== group)
					resolvedSequences.push(group);
			}
		}
		
		private static function getGroup(groupType:Type):Group {
			var group:Group = GROUPS_CACHE[groupType];
			if (group == null) {
				group = new Group(groupType);
				GROUPS_CACHE[groupType] = group;
			}
			return group;
		}
	}
}

import mx.validators.Validator;

import org.granite.reflect.Annotation;
import org.granite.validation.IConstraint;
import org.granite.validation.ValidatorFactory;

class GroupSequence implements IConstraint {
	
	private var _factory:ValidatorFactory = null;
	
	function GroupSequence(factory:ValidatorFactory) {
		_factory = factory;
	}

	public function get factory():ValidatorFactory {
		return _factory;
	}

	public function get message():String { return null; }
	public function get groups():Array { return null; }
	public function get payload():Array { return null; }
	public function get properties():Array { return null; }
	public function initialize(annotation:Annotation, factory:ValidatorFactory):void {}
	public function validate(value:*):String { return null; }
}