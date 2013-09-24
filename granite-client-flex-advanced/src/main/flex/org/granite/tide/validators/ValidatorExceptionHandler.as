/*
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
package org.granite.tide.validators {

    import flash.utils.Dictionary;
    import flash.utils.flash_proxy;
    
    import mx.collections.IList;
    import mx.collections.errors.ItemPendingError;
    import mx.messaging.messages.ErrorMessage;
    import mx.rpc.events.ResultEvent;
    import mx.utils.ObjectProxy;
    import mx.utils.ObjectUtil;
    import mx.utils.object_proxy;
    
    import org.granite.collections.IPersistentCollection;
    import org.granite.meta;
    import org.granite.tide.BaseContext;
    import org.granite.tide.IComponent;
    import org.granite.tide.IEntity;
    import org.granite.tide.IEntityManager;
    import org.granite.tide.IExceptionHandler;
    import org.granite.tide.IPropertyHolder;
    import org.granite.tide.events.TideValidatorEvent;
    import org.granite.validation.ConstraintViolationEvent;
    import org.granite.validation.ValidationEvent;

    use namespace flash_proxy;
    use namespace object_proxy;
    

    /**
     * 	Validation exception handler that accepts Validation.Failed faults (server-side Tide converts Hibernate validator errors to this fault code)
     * 	and dispatches them on the context
     * 
     * 	@author William DRAI
     */
    public class ValidatorExceptionHandler implements IExceptionHandler {
        
        public static const VALIDATION_FAILED:String = "Validation.Failed"; 
        
        
        public function accepts(emsg:ErrorMessage):Boolean {
            return emsg.faultCode == VALIDATION_FAILED;
        }

        public function handle(context:BaseContext, emsg:ErrorMessage):void {
			var invalidValues:Array = emsg.extendedData.invalidValues as Array;
			if (invalidValues) {
				var bean:Object, rootBean:Object;
				var violationsMap:Dictionary = new Dictionary();
				for each (var iv:InvalidValue in invalidValues) {
					rootBean = context.meta_getCachedObject(iv.rootBean);
					bean = iv.bean ? context.meta_getCachedObject(iv.bean) : null;
					
					var violations:Array = violationsMap[rootBean];
					if (violations == null) {
						violations = [];
						violationsMap[rootBean] = violations;
					}
					var violation:ServerConstraintViolation = new ServerConstraintViolation(iv, rootBean, bean);
					violations.push(violation);
				}
				
				for (rootBean in violationsMap) {
					rootBean.dispatchEvent(new ValidationEvent(ValidationEvent.START_VALIDATION));
					
					violations = violationsMap[bean];
					for each (violation in violations)
						rootBean.dispatchEvent(new ConstraintViolationEvent(violation));
					
					rootBean.dispatchEvent(new ValidationEvent(ValidationEvent.END_VALIDATION));
				}

				if (context.hasEventListener(TideValidatorEvent.INVALID)) {
					var event:TideValidatorEvent = new TideValidatorEvent(TideValidatorEvent.INVALID, context, null, false, false, invalidValues);
					context.dispatchEvent(event);
				}
			}
        }
    }
}
