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
    
    import mx.data.utils.Managed;
    import mx.logging.ILogger;
    import mx.logging.Log;
    import mx.validators.ValidationResult;
    import mx.validators.Validator;
    
    import org.granite.tide.IEntity;
    import org.granite.tide.IEntityManager;
    import org.granite.tide.Tide;
    import org.granite.tide.events.TideValidatorEvent;
    import org.granite.tide.service.ServerSession;

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
        private var _serverSession:ServerSession = null;
        
        private var _valueChecking:Object = null;
        private var _lastCheckedValue:Object = null;
        private var _lastCheckedResults:Array = [];
        
        
        public function TideInputValidator():void {
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

        public function set serverSession(serverSession:ServerSession):void {
            _serverSession = serverSession;
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
            
			var results:Array = value is String ? super.doValidation(value) : [];
			if (results.length > 0)
            	return results;
            
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
                        var serverSession:ServerSession = _serverSession != null ? _serverSession : Tide.getInstance().mainServerSession;
	                	em.meta_validateObject(serverSession, _entity, _entityProperty, value);
	                }
                	return results;
                }
            }
            
            return _lastCheckedResults;
        }
    }
}
