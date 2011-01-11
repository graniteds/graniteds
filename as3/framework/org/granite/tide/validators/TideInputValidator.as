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
    
    import mx.logging.Log;
    import mx.logging.ILogger;

    import mx.validators.Validator;
    import mx.validators.ValidationResult;
    import mx.data.utils.Managed;

    import org.granite.tide.IEntity;
    import org.granite.tide.IEntityManager;
    import org.granite.tide.events.TideValidatorEvent;


    /**
     * 	Remote validator for managed entities	
     * 
     * 	@author William DRAI
     */
    public class TideInputValidator extends Validator {
        
        private static var log:ILogger = Log.getLogger("org.granite.tide.validators.TideInputValidator");
        
        
        private var _entity:IEntity;
        private var _entityProperty:String;
        private var _entityManager:IEntityManager;
        
        private var _valueChecking:Object = null;
        private var _lastCheckedValue:Object = null;
        private var _lastCheckedResults:Array = [];
        
        
        public function TideInputValidator() {
            super();
        }

        public function set entity(entity:IEntity):void {
            if (_entity) {
                _entity.removeEventListener(TideValidatorEvent.VALID, validHandler);
                _entity.removeEventListener(TideValidatorEvent.INVALID, invalidHandler);
            }

            _entity = entity;

            _lastCheckedValue = null;
            _lastCheckedResults = [];
            _valueChecking = null;
            
            if (_entity != null) {
	            _entity.addEventListener(TideValidatorEvent.INVALID, invalidHandler, false, 0, true);
	            _entity.addEventListener(TideValidatorEvent.VALID, validHandler, false, 0, true);
	        }
        }
        
        public function set entityProperty(entityProperty:String):void {
            _entityProperty = entityProperty;
        }
        
        public function set entityManager(entityManager:IEntityManager):void {
            _entityManager = entityManager;
        }


        private function validHandler(event:TideValidatorEvent):void {
            if (_entity && event.entityProperty == _entityProperty)
                validate(event);
        }

        private function invalidHandler(event:TideValidatorEvent):void {
            if (_entity && event.entityProperty == _entityProperty)
                validate(event);
        }


        override protected function doValidation(value:Object):Array {
            var em:IEntityManager = _entity ? Managed.getEntityManager(_entity) : null;
            if (em == null)
                em = _entityManager;
            
			if (value is String)
				return super.doValidation(value);
			
            var results:Array = [];
            
            if (value is TideValidatorEvent) {
                if (TideValidatorEvent(value).type == TideValidatorEvent.INVALID 
                    && TideValidatorEvent(value).invalidValues != null) {
                    var invalidValues:Array = TideValidatorEvent(value).invalidValues;
                    for each (var iv:Object in invalidValues) {
                        var invalidValue:InvalidValue = iv as InvalidValue;
                        if (_entityProperty == invalidValue.path) {
                            var vr:ValidationResult = new ValidationResult(true, invalidValue.path, "invalidValue", invalidValue.message);
                            results.push(vr);
                        }
                    }
                }
                
                _lastCheckedValue = _valueChecking;
                _lastCheckedResults = results;
                _valueChecking = null;
                
                return results;
            }
            else {
                var value:Object = getValueFromSource();
                if (value != _lastCheckedValue) {
                	if (_valueChecking == null && em != null) {
	                	_valueChecking = value;
	                	em.meta_validateObject(_entity, _entityProperty, value);
	                }
                	return results;
                }
            }
            
            return _lastCheckedResults;
        }
    }
}
