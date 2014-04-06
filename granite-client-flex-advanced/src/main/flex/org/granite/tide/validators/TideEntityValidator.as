/*
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2014 GRANITE DATA SERVICES S.A.S.
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
package org.granite.tide.validators {
    
	import flash.events.IEventDispatcher;
	
    import mx.logging.Log;
    import mx.logging.ILogger;
    
    import mx.validators.Validator;
    import mx.validators.ValidationResult;
	
	import org.granite.validation.ConstraintViolation;
	import org.granite.validation.ConstraintViolationEvent;
    import org.granite.validation.ValidationEvent;


    /**
     * 	Validator component that listens to ConstraintViolationEvent coming from server validation 
     *  and dispatches them as client validator events
     * 
     * 	@author William DRAI
     */
    public class TideEntityValidator extends Validator {
        
        private static var log:ILogger = Log.getLogger("org.granite.tide.validators.TideEntityValidator");
		
		/** @private */
		protected var _entity:Object;
		
		/** @private */
		protected var _violations:Array = [];
        
        
		/**
		 * 	Entity that is tracked for validation errors
		 *  The property is defined by the inherited 'property' of the Validator class
		 *  
		 *  @param entity tracked entity
		 */ 
		[Bindable]
		public function get entity():Object {
			return _entity;
		}
		public function set entity(entity:Object):void {
			
			if (entity != null && !(entity is IEventDispatcher))
				throw new ArgumentError("FormValidator entity must implement IEventDispatcher");
			
			if (entity != _entity) {
				validate([]);
				
				if (_entity) {
					_entity.removeEventListener(ValidationEvent.START_VALIDATION, startValidationHandler);
					_entity.removeEventListener(ConstraintViolationEvent.CONSTRAINT_VIOLATION, constraintViolationHandler);
					_entity.removeEventListener(ValidationEvent.END_VALIDATION, endValidationHandler);
				}
				
				_entity = entity;
				
				if (_entity) {
					_entity.addEventListener(ValidationEvent.START_VALIDATION, startValidationHandler, false, 0, true);
					_entity.addEventListener(ConstraintViolationEvent.CONSTRAINT_VIOLATION, constraintViolationHandler, false, 0, true);
					_entity.addEventListener(ValidationEvent.END_VALIDATION, endValidationHandler, false, 0, true);
				}
			}
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
			
			var results:Array = [];
			
			if (value is Array) {
				var violations:Array = value as Array;
				
				for each (var violation:ConstraintViolation in violations) {
					if (violation.propertyPath.toString() == property) {
						var result:ValidationResult = new ValidationResult(true, property, "constraintViolation", violation.message);								
						results.push(result);
					}
				}
			}
			
			return results;
		}
    }
}