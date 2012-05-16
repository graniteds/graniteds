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

	import flash.display.DisplayObject;
	import flash.events.Event;
	import flash.events.FocusEvent;
	import flash.events.IEventDispatcher;
	import flash.utils.getDefinitionByName;
	
	import mx.binding.Binding;
	import mx.core.Container;
	import mx.core.UIComponent;
	import mx.core.mx_internal;
	import mx.events.FlexEvent;
	import mx.events.ValidationResultEvent;
	import mx.validators.IValidatorListener;
	import mx.validators.ValidationResult;
	import mx.validators.Validator;
	
	import org.granite.reflect.Type;
	
	/**
	 * Dispatched when validation is done, with a (possibly empty) array 
	 * of <code>ValidationResult</code> for any <code>ConstraintViolation</code>
	 * that couldn't be associated with any input.
	 *
	 * @eventType org.granite.validation.FormValidator.UNHANDLED_VIOLATIONS
	 * 
	 * @see mx.events.ValidationResultEvent
	 * @see mx.events.ValidationResult
	 */
	[Event(name="unhandledViolations", type="mx.events.ValidationResultEvent")]
	
	/**
	 * The <code>FormValidator</code> class is Flex utility that simplifies
	 * the validation framework usage with input components. It performs
	 * validation on the fly whenever the user enters data into graphical
	 * inputs and automatically displays error messages when these data are
	 * incorrect, based on constraint annotations placed on bean properties.
	 * 
	 * @example The following code excerpt illustrates a typical usage (Flex 4
	 * with bidirectional bindings):
	 * 
	 * <listing>
	 * 
	 * [Bindable]
	 * public class Person {
	 * 
	 *     [Size(max="10")]
	 *     public var firstname:String;
	 * 
	 *     [NotNull] [Size(min="2", max="10")]
	 *     public var lastname:String;
	 * }
	 * <hr />
	 * &lt;fx:Declarations&gt;
	 *     &lt;v:FormValidator id="fValidator" form="{personForm}" entity="{person}"/&gt;
	 * &lt;/fx:Declarations&gt;
	 * 
	 * &lt;fx:Script&gt;
	 * 
	 *     [Bindable]
	 *     protected var person:Person = new Person();
	 * 
	 *     protected function savePerson():void {
	 *         if (fValidator.validateEntity()) {
	 *             // actually save the validated person entity...
	 *         }
	 *     }
	 * 
	 *     protected function resetPerson():void {
	 *         person = new Person();
	 *     }
	 * 
	 * &lt;/fx:Script&gt;
	 * 
	 * &lt;mx:Form id="personForm"&gt;
	 *     &lt;mx:FormItem label="Firstname" required="true"&gt;
	 *         &lt;s:TextInput text="&#64;{person.firstname}"/&gt;
	 *     &lt;/mx:FormItem&gt;
	 *     &lt;mx:FormItem label="Lastname" required="true"&gt;
	 *         &lt;s:TextInput text="&#64;{person.lastname}"/&gt;
	 *     &lt;/mx:FormItem&gt;
	 * &lt;/mx:Form&gt;
	 * 
	 * &lt;s:Button label="Save" click="savePerson()"/&gt;
	 * &lt;s:Button label="Cancel" click="resetPerson()"/&gt;
	 * </listing>
	 * 
	 * @author William DRAI
	 * @author Franck WOLFF
	 */
	public class FormValidator extends Validator {

		public static const UNHANDLED_VIOLATIONS:String = "unhandledViolations";
		
		/** @private */
		protected var _form:Object;
		/** @private */
		protected var _entity:Object;

		/** @private */
		protected var _created:Boolean = false;
		/** @private */
		protected var _focusedOutOnce:Object = new Object();
		
		/** @private */
		protected var _inputs:Array = null;
		/** @private */
		protected var _inputProperties:Object = new Object();
		/** @private */
		protected var _validatedInput:Object = null;

		/** @private */
		protected var _violations:Array = [];
		/** @private */
		protected var _unhandledViolations:Array = [];
		
		/**
		 * The <code>ValidatorFactory</code> to be used in the validation
		 * process (initialized with the the default instance).
		 */
		public var validatorFactory:ValidatorFactory = ValidatorFactory.getInstance();
		
		/**
		 * Should validation be done on the fly? Otherwise, validation will be
		 * only done when an input loses focus. Default is true.
		 */
		public var validateOnChange:Boolean = true;
		
		/**
		 * The validation groups to be used, as an array of <code>Class</code>
		 * names. Default is null, meaning that the <code>Default</code> group
		 * will be used.
		 */
		public var groups:Array = null;
		
		
		public static var prioritaryComponentProperties:Array = [ "text", "selected", "selectedDate", "selectedItem", "selectedIndex" ];
		
		/**
		 * The form component that contains inputs bound to the entity properties
		 * (may be a <code>Form</code> or any other <code>Container</code>
		 * subclass).
		 */
		[Bindable]
		public function get form():Object {
			return _form;
		}
		public function set form(form:Object):void {
			if (form != _form) {	
				if (_form) {
					_form.removeEventListener(FlexEvent.CREATION_COMPLETE, creationCompleteHandler);
					_form.removeEventListener(Event.ADDED, childChangeHandler);
					_form.removeEventListener(Event.REMOVED, childChangeHandler);
				}
				
				_form = form;
				
				if (_form) {
					_form.addEventListener(FlexEvent.CREATION_COMPLETE, creationCompleteHandler, false, 0, true);
					_form.addEventListener(Event.ADDED, childChangeHandler, false, 0, true);
					_form.addEventListener(Event.REMOVED, childChangeHandler, false, 0, true);
				}
			}
		}

		
		/**
		 * The entity, with constraint annotations, to be validated. This entity
		 * must implement the <code>IEventDispatcher</code> interface, explicitly
		 * or not (ie: annotated with [Bindable]).
		 */
        [Bindable]
        public function get entity():Object {
        	return _entity;
        }
        public function set entity(entity:Object):void {
			if (entity != _entity) {
				validate([]);
				
				if (_entity && _entity is IEventDispatcher) {
					_entity.removeEventListener(ValidationEvent.START_VALIDATION, startValidationHandler);
					_entity.removeEventListener(ConstraintViolationEvent.CONSTRAINT_VIOLATION, constraintViolationHandler);
					_entity.removeEventListener(ValidationEvent.END_VALIDATION, endValidationHandler);
				}
				
	        	_entity = entity;
				_focusedOutOnce = new Object();
	
	        	if (_entity && _entity is IEventDispatcher) {
					_entity.addEventListener(ValidationEvent.START_VALIDATION, startValidationHandler, false, 0, true);
					_entity.addEventListener(ConstraintViolationEvent.CONSTRAINT_VIOLATION, constraintViolationHandler, false, 0, true);
					_entity.addEventListener(ValidationEvent.END_VALIDATION, endValidationHandler, false, 0, true);
	
	        		setupForm();
				}
			}
        }
		
		/**
		 * Validate the underlying entity instance for the currently set groups.
		 * 
		 * @return <code>true</code> if validation succeeds, <code>false</code>
		 * 		otherwise.
		 */
		public function validateEntity():Boolean {
			getValidator().validate(_entity, groups);
			return _violations.length == 0;
		}

		/**
		 * Returns the result of the last global validation as an array of
		 * <code>ConstraintViolation</code>s.
		 * 
		 * @return the result of the last global validation as an array of
		 * 		<code>ConstraintViolation</code>s.
		 */
		public function getViolations():Array {
			return _violations;
		}

		/**
		 * Returns the <i>unhandled</i> violations of the last global validation
		 * as an array of <code>ConstraintViolation</code>s. Unhandled violations
		 * are violations that couldn't be associated to any input during the
		 * last global validation (thus, they couldn't be displayed anywhere).
		 * 
		 * @return the <i>unhandled</i> violations of the last global validation
		 * 		as an array of <code>ConstraintViolation</code>s.
		 */
		public function getUnhandledViolations():Array {
			return _unhandledViolations;
		}
		
		[Bindable("unhandledViolationsMessageChange")]
		/**
		 * A message containing all unhandled violations messages, separated by
		 * new lines.
		 */
		public function get unhandledViolationsMessage():String {
			var message:String = "";
			for each (var violation:ConstraintViolation in _unhandledViolations) {
				if (message.length > 0)
					message += '\n';
				message += violation.message;
			}
			return message;
		}
		
		/**
		 * @private
		 */
		protected function startValidationHandler(event:ValidationEvent):void {
			_violations = [];
		}
		
		/**
		 * @private
		 */
		protected function constraintViolationHandler(event:ConstraintViolationEvent):void {
			_violations.push(event.violation);
		}
		
		/**
		 * @private
		 */
		protected function endValidationHandler(event:ValidationEvent):void {
			validate(_violations);
		}

		/**
		 * @inheritDoc
		 */
		override protected function doValidation(value:Object):Array {
			if (value is String)
				return super.doValidation(value);
			
			_unhandledViolations = [];

			var results:Array = [];
			
			if (value is Array) {
				var violations:Array = value as Array;
				
				var input:IValidatorListener = null;
				if (violations.length > 0 && violations[0] is IValidatorListener) {
					input = IValidatorListener(violations[0]);
					violations = violations[1] as Array;
				}
				
				for each (var violation:ConstraintViolation in violations) {
					var properties:Array = (violation.constraint.properties != null ? violation.constraint.properties : []),
						constraintHandled:Boolean = false;
					
					if (input != null) {
						results.push(new ValidationResult(true, input.validationSubField, "constraintViolation", violation.message));
						constraintHandled = true;
					}
					else {
						for each (var i:IValidatorListener in _inputs) { 
							if (violation.propertyPath.toString() == i.validationSubField || properties.indexOf(i.validationSubField) != -1) {
								var result:ValidationResult = new ValidationResult(true, i.validationSubField, "constraintViolation", violation.message);								
								results.push(result);
								constraintHandled = true;
								if (properties.length == 0)
									break;
							}
						}
					}
					
					if (!constraintHandled)
						_unhandledViolations.push(violation);
						
				}
			}
			
			dispatchEvent(new Event("unhandledViolationsMessageChange"));
			
			var unhandledResults:Array = [];
			for each (violation in _unhandledViolations)
				unhandledResults.push(new ValidationResult(true, null, "constraintViolation", violation.message));
			dispatchEvent(new ValidationResultEvent(UNHANDLED_VIOLATIONS, false, false, null, unhandledResults));
			
			return results;
		}
		
		/**
		 * @inheritDoc
		 */
		override protected function handleResults(errorResults:Array):ValidationResultEvent {
			var resultEvent:ValidationResultEvent;
	        
	        if (errorResults.length > 0) {
	            resultEvent = new ValidationResultEvent(ValidationResultEvent.INVALID);
	            resultEvent.results = errorResults;
	            
	            if (_validatedInput == null && subFields.length > 0) {
	                var errorFields:Object = {};
	                var subField:String;
	                
	                // Now we need to send valid results
	                // for every subfield that didn't fail.
	                var n:int;
	                var i:int;
	                
	                n = errorResults.length;
	                for (i = 0; i < n; i++) {
	                    subField = errorResults[i].subField;
	                    if (subField)
	                        errorFields[subField] = true;
	                }
	                
	                n = subFields.length;
	                for (i = 0; i < n; i++) {
	                    if (!errorFields[subFields[i]])
	                        errorResults.push(new ValidationResult(false, subFields[i]));
	                }
	            }
	        }
	        else if (_validatedInput != null) {
	            resultEvent = new ValidationResultEvent(ValidationResultEvent.INVALID);
				resultEvent.results = [new ValidationResult(false, _validatedInput.validationSubField)];
			}
			else
				resultEvent = new ValidationResultEvent(ValidationResultEvent.VALID);
	        
	        return resultEvent;
		}
		
		/**
		 * @private
		 */
        protected function getValidator():IValidator {
        	return validatorFactory.getValidator(Type.forInstance(_entity))
        }
		
		/**
		 * @private
		 */
		protected function creationCompleteHandler(event:FlexEvent):void {
			_form.removeEventListener(FlexEvent.CREATION_COMPLETE, creationCompleteHandler);
			
			setupForm();
			_created = true;
		}
				
		/**
		 * @private
		 */
		private function childChangeHandler(event:Event):void {
			if (!_created || !(event.target is UIComponent))
				return;
			if (event.type == Event.ADDED)
				setupAddedChild(UIComponent(event.target));
			else if (event.type == Event.REMOVED)
				setupRemovedChild(UIComponent(event.target));
		}
		
		/**
		 * @private
		 */
		protected function lookupBindings():Array {
			var bindings:Array = null;
			try {
				try {
					bindings = Object(_form).mx_internal::_bindings;
				}
				catch (f:Error) {
					if (_form.parentDocument)
						bindings = Object(_form.parentDocument).mx_internal::_bindings;
				}
			}
			catch (e:Error) {
				// Ignore: could not determine bindings
			}
			return bindings;
		} 
		
		/**
		 * @private
		 */
		protected function setupAddedChild(child:UIComponent):void {
			if (!_form || !_entity)
				return;
			
			var bindings:Array = lookupBindings();
			if (bindings == null || bindings.length == 0)
				return;
			
			removeListenerHandler();
			setupComponent(child, bindings);
			
			subFields = [];
			for each (var input:IValidatorListener in _inputs) {
				if (input.validationSubField)
					subFields.push(input.validationSubField);
			}
			
			addListenerHandler();
			
			detectFlex4();
			var container:Object = child;
			if (IVISUALELEMENTCONTAINER_CLASS != null && container is IVISUALELEMENTCONTAINER_CLASS) {
				for (var idx:int = 0; idx < container.numElements; idx++) {
					if (container.getElementAt(idx) is UIComponent)
						setupAddedChild(UIComponent(container.getElementAt(idx)));
				}
			}
			else if (container is Container) {
				for each (var c:DisplayObject in container.getChildren()) {
					if (c is UIComponent)
						setupAddedChild(UIComponent(c));
				}
			}
		}
		
		/**
		 * @private
		 */
		protected function setupRemovedChild(child:UIComponent):void {
			if (!_form || !_entity)
				return;
			
			var idx:int = _inputs.indexOf(child);
			if (idx >= 0) {				
				child.removeEventListener(Event.CHANGE, valueChangeHandler);
				child.removeEventListener(FlexEvent.VALUE_COMMIT, valueChangeHandler);
				child.removeEventListener(FocusEvent.FOCUS_OUT, inputFocusOutHandler);
				
				delete _inputProperties[child.id];
				_inputs.splice(idx, 1);				
				removeListenerHandler();
				
				subFields = [];
				for each (var input:IValidatorListener in _inputs) {
					if (input.validationSubField)
						subFields.push(input.validationSubField);
				}
				
				addListenerHandler();
			}
			
			detectFlex4();
			var container:Object = child;
			if (IVISUALELEMENTCONTAINER_CLASS != null && container is IVISUALELEMENTCONTAINER_CLASS) {
				for (idx = 0; idx < container.numElements; idx++) {
					if (container.getElementAt(idx) is UIComponent)
						setupRemovedChild(UIComponent(container.getElementAt(idx)));
				}
			}
			else if (container is Container) {
				for each (var c:DisplayObject in container.getChildren()) {
					if (c is UIComponent)
						setupRemovedChild(UIComponent(c));
				}
			}
		}
		
		/**
		 * @private
		 */
		protected function setupForm():void {
			if (!_form || !_entity)
				return;
			
			if (_inputs != null) {
				for each (var component:Object in _inputs) {
					component.removeEventListener(Event.CHANGE, valueChangeHandler);
					component.removeEventListener(FlexEvent.VALUE_COMMIT, valueChangeHandler);
					component.removeEventListener(FocusEvent.FOCUS_OUT, inputFocusOutHandler);
				}
			}
			
			var bindings:Array = lookupBindings();
			if (bindings == null || bindings.length == 0)
				return;

			_inputs = [];
			_inputProperties = new Object();

			removeListenerHandler();
			setup(_form, bindings);
			
			subFields = [];
			for each (var input:IValidatorListener in _inputs) {
				if (input.validationSubField)
					subFields.push(input.validationSubField);
			}
			
			addListenerHandler();
		}
		
		
		private static var flex4:Boolean = true;
		private static var IVISUALELEMENTCONTAINER_CLASS:Class = null;
		
		/**
		 * @private
		 */
		private function detectFlex4():void {
			if (IVISUALELEMENTCONTAINER_CLASS == null && flex4) {
				try {
					IVISUALELEMENTCONTAINER_CLASS = getDefinitionByName("mx.core.IVisualElementContainer") as Class;
				}
				catch (e:ReferenceError) {
					flex4 = false;
				}
			}
		}
		
		/**
		 * @private
		 */
		protected function setup(container:Object, bindings:Array):void {
			detectFlex4();
			if (IVISUALELEMENTCONTAINER_CLASS != null && container is IVISUALELEMENTCONTAINER_CLASS) {
				for (var idx:int = 0; idx < container.numElements; idx++)
					setupChild(container.getElementAt(idx), bindings);
			}
			else if (container is Container) {
				for each (var c:DisplayObject in container.getChildren())
					setupChild(c, bindings);
			}
		}
		
		/**
		 * @private
		 */
		private function setupChild(c:Object, bindings:Array):void {
			if (c is Container || (IVISUALELEMENTCONTAINER_CLASS != null && c is IVISUALELEMENTCONTAINER_CLASS))
				setup(c, bindings);
			
			if (c is UIComponent)
				setupComponent(UIComponent(c), bindings);
		}
		
		/**
		 * @private
		 */
		private function setupComponent(component:UIComponent, bindings:Array):void {
			var inputProperty:String = null;
			
			if (bindings) {
				var property:String = null;
				var matchLevel:int = 0;
				
				// Lookup in existing bindings
				for each (var binding:Binding in bindings) {
					var destString:String = binding.mx_internal::destString;
					
					var inputProp:String = null;
					var modelProp:String = null;
					
					var level:int = 0;
					
					if (destString.indexOf(".") > 0 && destString.substring(0, destString.indexOf(".")) == component.id) {
						inputProp = destString.substring(destString.indexOf(".")+1);
						level++;
						
						if (binding.twoWayCounterpart) {
							var twoWayCounterpartDestString:String = binding.twoWayCounterpart.mx_internal::destString;
							if (twoWayCounterpartDestString.indexOf(".") > 0) {
								modelProp = twoWayCounterpartDestString.substring(twoWayCounterpartDestString.indexOf(".") + 1);
								if (component.validationSubField && modelProp != component.validationSubField)
									level = 0;
								else
									level++;
							}
							else
								level = 0;
						}
					}
					
					if (binding.twoWayCounterpart) {
						var twoWayCounterpartDestString:String = binding.twoWayCounterpart.mx_internal::destString;
						if (twoWayCounterpartDestString.indexOf(".") > 0 && twoWayCounterpartDestString.substring(0, twoWayCounterpartDestString.indexOf(".")) == component.id) {
							inputProp = twoWayCounterpartDestString.substring(twoWayCounterpartDestString.indexOf(".") + 1);							
							modelProp = destString.substring(destString.indexOf(".") + 1);
							
							if (component.validationSubField && modelProp != component.validationSubField)
								level = 0;
							else
								level++;
						}
					}
					
					if (inputProp && prioritaryComponentProperties.indexOf(inputProp) >= 0)
						level++;
					
					if (level > matchLevel) {
						inputProperty = inputProp;
						property = modelProp;
						matchLevel = level;
					}
				}
				
				if (property)
					component.validationSubField = property;
			}
			
			if (component.validationSubField && inputProperty != null && component.hasOwnProperty(inputProperty)) {
				_inputs.push(component);
				_inputProperties[component.id] = inputProperty;
				
				component.addEventListener(Event.CHANGE, valueChangeHandler, false, int.MAX_VALUE, true);
				component.addEventListener(FlexEvent.VALUE_COMMIT, valueChangeHandler, false, 0, true);
				component.addEventListener(FocusEvent.FOCUS_OUT, inputFocusOutHandler, false, 0, true);
			}
		}
		
		/**
		 * @inheritDoc
		 */
		override protected function get actualListeners():Array {
			return _inputs;
		}
		
		/**
		 * @private
		 */
		protected function inputFocusOutHandler(event:FocusEvent):void {
			validateValue(event.currentTarget, true);
		}
		
		/**
		 * @private
		 */
		protected function valueChangeHandler(event:Event):void {
			if (event.type == Event.CHANGE && !validateOnChange)
				return;
			validateValue(event.currentTarget, false);
		}	
		
		/**
		 * @private
		 */
		protected function validateValue(component:Object, focusOut:Boolean = false):Boolean {
			if (_entity == null)
				return true;
			
			if (focusOut)
				_focusedOutOnce[component.validationSubField] = true;
			
			var nulled:Boolean = false;
			var value:Object = component[_inputProperties[component.id]];
			if (value == "") {
				value = null;
				nulled = true;
			}

			var leafTypeProperty:Array = getLeafTypeProperty(Type.forInstance(entity), component.validationSubField);
			
			var violations:Array = ValidatorFactory.getInstance().validateValue(
				leafTypeProperty[0] as Type,
				leafTypeProperty[1] as String,
				value,
				groups
			);
			if (violations == null)
				violations = [];
			if (violations.length == 0 && !nulled)
				_focusedOutOnce[component.validationSubField] = true;
			else if (!_focusedOutOnce[component.validationSubField])
				return true;
			
			_validatedInput = component;
			validate([component, violations]);
			_validatedInput = null;
			
			return violations.length == 0;
		}
		
		/**
		 * @private
		 */
		protected function getLeafTypeProperty(type:Type, path:String):Array {
			if (path.indexOf('.') == -1)
				return [type, path];
			
			var properties:Array = path.split("\.");
			var last:String = properties.pop();
			for each (var property:String in properties)
				type = type.getProperty(property).type;
			return [type, last];
		}
	}
}