/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
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

package org.granite.validation {

	import flash.events.IEventDispatcher;
	import flash.utils.Dictionary;
	
	import mx.utils.StringUtil;
	
	import org.granite.reflect.Annotation;
	import org.granite.reflect.Arg;
	import org.granite.reflect.Field;
	import org.granite.reflect.IAnnotatedElement;
	import org.granite.reflect.Type;
	import org.granite.reflect.visitor.Guide;
	import org.granite.reflect.visitor.IVisitor;
	import org.granite.reflect.visitor.Visitable;
	import org.granite.validation.groups.Default;
	
	/**
	 * A generic <code>IValidator</code> implementation. Instances of this
	 * class must only be used once.
	 * 
	 * @author Franck WOLFF
	 */
	public class GenericValidator implements IValidator, IVisitor {
		
		///////////////////////////////////////////////////////////////////////
		// Fields.
		
		private var _factory:ValidatorFactory;
		private var _cascadeProperties:Boolean = false;
		private var _groupChain:GroupChain = null;
		
		private var _class2Metadata:Dictionary = new Dictionary();
		private var _group2Constraints:Dictionary = new Dictionary();
		private var _annotation2Constraint:Dictionary = new Dictionary();
		private var _constraint2Visitable:Dictionary = new Dictionary();
		private var _defaultChains:Dictionary = new Dictionary();
		
		///////////////////////////////////////////////////////////////////////
		// Constructor.
		
		/**
		 * Constructs a new <code>GenericValidator</code> instance.
		 * 
		 * @param factory the <code>ValidatorFactory</code> to be used for
		 * 		the validation (may not be null).
		 */
		function GenericValidator(factory:ValidatorFactory) {
			if (factory == null)
				throw new ArgumentError("Parameter 'factory' cannot be null");

			_factory = factory;
		}
		
		///////////////////////////////////////////////////////////////////////
		// Properties.
		
		/**
		 * The <code>ValidatorFactory</code> instance used in this validator.
		 */
		public function get factory():ValidatorFactory {
			return _factory;
		}
		
		///////////////////////////////////////////////////////////////////////
		// IValidator implementation.
		
		/**
		 * @inheritDoc
		 */
		public function validate(bean:Object, groups:Array = null):Array {
			if (bean == null)
				throw new ArgumentError("Validation of a null object");
			
			var dispatcher:IEventDispatcher = (
				_factory.dispatchEvents && bean is IEventDispatcher ?
				IEventDispatcher(bean) :
				null
			);
			
			if (dispatcher != null)
				dispatcher.dispatchEvent(new ValidationEvent(ValidationEvent.START_VALIDATION));
			
			_cascadeProperties = true;
			
			_groupChain = GroupChain.createGroupChain(_factory, groups);
			new Guide(this, _factory.visitorHandlers).explore(bean);
			
			var violations:Array = internalValidate(_factory.dispatchEvents);
			
			if (dispatcher != null) {
				for each (var violation:ConstraintViolation in violations)
					dispatcher.dispatchEvent(new ConstraintViolationEvent(violation));
				
				dispatcher.dispatchEvent(new ValidationEvent(ValidationEvent.END_VALIDATION));
			}
			
			return violations;
		}
		
		/**
		 * @inheritDoc
		 */
		public function validateProperty(bean:Object, propertyName:String, groups:Array = null):Array {
			if (bean == null)
				throw new ArgumentError("Validated object cannot be null");

			const type:Type = Type.forInstance(bean),
				  parent:Visitable = new Visitable(null, type, bean),
				  field:Field = type.getProperty(propertyName),
				  groupChain:GroupChain = GroupChain.createGroupChain(_factory, groups);
			
			_groupChain = GroupChain.createGroupChain(_factory, groups);
			new Guide(this, _factory.visitorHandlers).exploreVisitable(new Visitable(parent, field, field.getValue(bean)));
			
			return internalValidate(_factory.dispatchEvents);
		}
		
		/**
		 * @inheritDoc
		 */
		public function validateValue(type:Type, propertyName:String, value:*, groups:Array = null):Array {
			if (type == null)
				throw new ArgumentError("The bean type cannot be null");

			const parent:Visitable = new Visitable(null, type, null),
				  field:Field = type.getProperty(propertyName);

			_groupChain = GroupChain.createGroupChain(_factory, groups);
			new Guide(this, _factory.visitorHandlers).exploreVisitable(new Visitable(parent, field, value));
			
			return internalValidate(false);
		}
		
		/**
		 * @private
		 */
		protected function internalValidate(dispatchEvents:Boolean):Array {
			
			var group:Group,
				constraints:Array,
				sequence:Array,
				defaultChain:*,
				group2Constraints:Dictionary,
				redefinedDefaultStatus:int = -1,
				violations:Array = new Array();
			
			for each (group in _groupChain.groups) {
				if (group.isDefaultGroup())
					redefinedDefaultStatus = 0;
				validateConstraints(_group2Constraints[group.groupClass], violations);
			}
			
			if (redefinedDefaultStatus == 0) {
				for (defaultChain in _defaultChains) {
					group2Constraints = _defaultChains[defaultChain];
					for each (group in GroupChain(defaultChain).uniqueSequence)
						validateConstraints(group2Constraints[group.groupClass], violations);
				}
				redefinedDefaultStatus = 1;
			}
			
			for each (sequence in _groupChain.sequences) {
				for each (group in sequence) {
					if (redefinedDefaultStatus == -1 && group.isDefaultGroup())
						redefinedDefaultStatus = 0;
					if (!validateConstraints(_group2Constraints[group.groupClass], violations))
						break;
				}
				if (redefinedDefaultStatus == 0) {
					for (defaultChain in _defaultChains) {
						group2Constraints = _defaultChains[defaultChain];
						for each (group in GroupChain(defaultChain).uniqueSequence)
							validateConstraints(group2Constraints[group.groupClass], violations);
					}
					redefinedDefaultStatus = 1;
				}
			}

			return violations;
		}
		
		/**
		 * @private
		 */
		protected function validateConstraints(constraints:Array, violations:Array):Boolean {
			var constraint:IConstraint,
				visitable:Visitable,
				violation:String,
				constraintViolation:ConstraintViolation,
				valid:Boolean = true;
			
			if (constraints != null) {
				for each (constraint in constraints) {
					visitable = _constraint2Visitable[constraint];
					if (visitable != null) {
						violation = constraint.validate(visitable.value);
						if (violation != null) {
							constraintViolation = createConstraintViolation(visitable, constraint, violation);
							violations.push(constraintViolation);
							valid = false;
						}
						delete _constraint2Visitable[constraint];
					}
				}
			}
			
			return valid;
		}
		
		///////////////////////////////////////////////////////////////////////
		// IVisitor implementation.
		
		/**
		 * Do validation constraint processing on the supplied visitable instance.
		 * This method first ask the current <code>TraversableResolver</code> if
		 * this visitable is reachable.
		 * 
		 * @param visitable the visitable instance to validate.
		 * 
		 * @return <code>true</code> if the supplied visitable is reachable,
		 * 		<code>false</code> otherwise.
		 * 
		 * @see ValidatorFactory#traversableResolver
		 */
		public function accept(visitable:Visitable):Boolean {
			if (visitable.element is IAnnotatedElement) {

				var type:Type,
					field:Field,
					metadata:ClassMetadata,
					annotation:Annotation,
					annotations:Array,
					group:Group,
					groupClass:Class,
					constraintClass:Class,
					constraint:IConstraint,
					constraintGroups:Array,
					constraints:Array,
					path:Path = new Path(visitable),
					annotatedElement:IAnnotatedElement = (visitable.element as IAnnotatedElement),
					reachable:Boolean = _factory.traversableResolver.isReachable(
						(visitable.isRoot() ? null : visitable.parent.value),
						path.lastNode,
						(visitable.root.element as Type),
						path,
						annotatedElement
					);
				
				if (reachable) {
					
					if (visitable.element is Type) {
							
						type = Type(visitable.element);
						metadata = _class2Metadata[type.getClass()];
						
						if (metadata == null) {
							metadata = new ClassMetadata();
							_class2Metadata[type.getClass()] = metadata;
							
							// implicit grouping detection.
							metadata.implicitGrouping = _groupChain.getMatchingGroupClasses(type.interfaces.map(
								function (item:Type, index:int, array:Array):Class {
									return item.getClass();
								}
							));
							
							// redefined default group detection.
							if (_groupChain.containsDefaultGroup() && type.isAnnotationPresent(GroupChain.GROUP_SEQUENCE)) {
								metadata.redefinedDefaultGroup = GroupChain.getRedefinedDefaultGroup(_factory, type);
								_defaultChains[metadata.redefinedDefaultGroup] = new Dictionary();
							}
						}
						
						visitable.data = metadata;
					}
					else if (!visitable.isRoot() && (visitable.element is Field) && (visitable.parent.data is ClassMetadata)) {
						
						metadata = ClassMetadata(visitable.parent.data);

						// implicit grouping association (collect constraints in super interfaces for this field).
						for each (groupClass in metadata.implicitGrouping) {
							field = Type.forClass(groupClass).getInstanceField(Field(visitable.element).name);
							if (field != null) {
								for each (annotation in field.getAnnotations(true)) {
									constraint = _annotation2Constraint[annotation];
									if (constraint == null) {
										constraintClass = _factory.getConstraintClass(annotation.name);
										if (constraintClass != null) {
											constraint = new constraintClass();
											constraint.initialize(annotation, _factory);
											_annotation2Constraint[annotation] = constraint;
										}
									}
					
									if (constraint != null) {
										constraints = _group2Constraints[groupClass];
										if (constraints == null) {
											constraints = new Array();
											_group2Constraints[groupClass] = constraints;
										}
										constraints.push(constraint);
										_constraint2Visitable[constraint] = visitable;
									}
								}
							}
						}
					}
					else
						metadata = new ClassMetadata();
					
					for each (annotation in annotatedElement.getAnnotations(true)) {
						constraint = _annotation2Constraint[annotation];
						if (constraint == null) {
							constraintClass = _factory.getConstraintClass(annotation.name);
							if (constraintClass != null) {
								constraint = new constraintClass();
								constraint.initialize(annotation, _factory);
								_annotation2Constraint[annotation] = constraint;
							}
						}
		
						if (constraint != null) {
							constraintGroups = constraint.groups;
							
							if (metadata != null && metadata.redefinedDefaultGroup != null) {
								for each (groupClass in metadata.redefinedDefaultGroup.getMatchingGroupClasses(constraintGroups)) {
									constraints = _defaultChains[metadata.redefinedDefaultGroup][groupClass];
									if (constraints == null) {
										constraints = new Array();
										_defaultChains[metadata.redefinedDefaultGroup][groupClass] = constraints;
									}
									constraints.push(constraint);
									_constraint2Visitable[constraint] = visitable;
								}
							}
							
							for each (groupClass in _groupChain.getMatchingGroupClasses(constraintGroups)) {
								if (groupClass !== Default || (metadata != null && metadata.redefinedDefaultGroup === null)) {
									constraints = _group2Constraints[groupClass];
									if (constraints == null) {
										constraints = new Array();
										_group2Constraints[groupClass] = constraints;
									}
									constraints.push(constraint);
									_constraint2Visitable[constraint] = visitable;
								}
							}
						}
					}
				}
				
				return reachable;
			}
			
			return true;
		}
		
		/**
		 * Ask the current <code>TraversableResolver</code> if the visitable is cascadable
		 * and test if it is bean or collection annotated with <code>[Valid]</code>.
		 * 
		 * @param visitable the visitable instance to be tested for cascading.
		 * 
		 * @return <code>true</code> if the supplied visitable should be cascaded,
		 * 		<code>false</code> otherwise.
		 * 
		 * @see ValidatorFactory#traversableResolver
		 */
		public function visit(visitable:Visitable):Boolean {

			if (!_cascadeProperties || visitable.value === null)
				return false;
			
			// cascadable?
			var path:Path = new Path(visitable);
			if ((visitable.element is IAnnotatedElement) &&
				!_factory.traversableResolver.isCascadable(
						(visitable.isRoot() ? null : visitable.parent.value),
						path.lastNode,
						(visitable.root.element as Type),
						path,
						(visitable.element as IAnnotatedElement)
				))
				return false;

			// the root bean is always cascaded.
			if (visitable.isRoot())
				return true;
			
			// visitable is a collection element (the collection itself, marked
			// [Valid], is currently cascaded).
			if (!(visitable.element is IAnnotatedElement))
				return true;
			
			// visitable is a bean or a collection annotated with [Valid].
			return (visitable.element as IAnnotatedElement).isAnnotationPresent("Valid");
		}
		
		///////////////////////////////////////////////////////////////////////
		// Utilities.
		
		/**
		 * @private.
		 */
		protected function createConstraintViolation(visitable:Visitable, constraint:IConstraint, violation:String):ConstraintViolation {
			var leaf:* = (visitable.isRoot() ? visitable.root.value : visitable.parent.value);
			return new ConstraintViolation(
				violation,
				constraint,
				visitable.root.value,
				visitable.root.element,
				new Path(visitable),
				leaf,
				visitable.value
			);
		}
	}
}
import org.granite.reflect.DynamicProperty;
import org.granite.reflect.IMember;
import org.granite.reflect.visitor.Visitable;
import org.granite.validation.GroupChain;
import org.granite.validation.INode;
import org.granite.validation.IPath;

class ClassMetadata {
	
	public var implicitGrouping:Array = [];
	public var redefinedDefaultGroup:GroupChain = null;
	
	public function toString():String {
		return "{implicitGrouping=[" + implicitGrouping + "], redefinedDefaultGroup=[" + redefinedDefaultGroup + "}}";
	}
}

class Path implements IPath {
	
	private var _visitable:Visitable;
	private var _nodes:Array = null;

	function Path(visitable:Visitable) {
		_visitable = visitable;
	}
	
	public function get nodes():Array {
		if (_nodes == null) {
			_nodes = new Array();
			
			var skipNext:Boolean = false;
			for (var parent:Visitable = _visitable; parent != null; parent = parent.parent) {
				if (skipNext) {
					skipNext = false;
					continue;
				}
				if (parent.element is IMember) {
					var node:Node = null, name:String = null;
					
					if (parent.element is DynamicProperty) {
						if (parent.parent != null && !(parent.parent is DynamicProperty)) {
							name = IMember(parent.parent.element).propertyKey;
							skipNext = true;
						}
						node = new Node(name, true, (parent.element as DynamicProperty).key);
					}
					else
						node = new Node(IMember(parent.element).propertyKey, false, null);
					
					_nodes.push(node);
				}
			}
			
			_nodes = _nodes.reverse();
		}
		return _nodes;
	}
	
	public function get lastNode():INode {
		return nodes[nodes.length - 1];
	}
	
	public function toString():String {
		var s:String = "";
		for each (var node:Node in nodes) {
			if (s.length > 0)
				s += ".";
			s += node.toString();
		}
		return s;
	}
}

class Node implements INode {

	private var _name:String;
	private var _isInIterable:Boolean;
	private var _keyOrIndex:*;
	
	function Node(name:String, isInIterable:Boolean, keyOrIndex:*) {
		_name = name;
		_isInIterable = isInIterable;
		_keyOrIndex = keyOrIndex;
	}
	
	public function get name():String {
		return _name;
	}
	
	public function get isInIterable():Boolean {
		return _isInIterable;
	}
	
	public function get keyOrIndex():* {
		return _keyOrIndex;
	}
	
	public function toString():String {
		var s:String = (_name != null ? _name : "");
		if (_isInIterable)
			s += "[" + String(_keyOrIndex) + "]";
		return s;
	}
}

