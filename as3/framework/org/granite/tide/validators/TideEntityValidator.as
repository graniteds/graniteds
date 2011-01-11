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