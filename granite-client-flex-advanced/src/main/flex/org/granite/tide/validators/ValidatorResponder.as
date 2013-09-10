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

    import flash.utils.Dictionary;
    
    import mx.rpc.IResponder;
    import mx.messaging.messages.ErrorMessage;
    import mx.rpc.events.ResultEvent;
    import mx.rpc.events.FaultEvent;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.IEntity;
    import org.granite.tide.IInvocationResult;
    import org.granite.tide.events.TideValidatorEvent;
    

    /**
     * 	@author William DRAI
     */
    [ExcludeClass]
    public class ValidatorResponder implements IResponder {
        
        private var _sourceContext:BaseContext;
        private var _entity:IEntity;
        private var _entityProperty:String;
        
        
        public function ValidatorResponder(sourceContext:BaseContext, entity:IEntity, entityProperty:String):void {
            _sourceContext = sourceContext;
            _entity = entity;
            _entityProperty = entityProperty;
        }
        
        
	    public function result(data:Object):void {
	        var revent:ResultEvent = ResultEvent(data);
	        var ires:IInvocationResult = IInvocationResult(revent.result);
            var invalidValues:Array = ires.result as Array;
            
            var event:TideValidatorEvent;
            if (invalidValues.length == 0)
                event = new TideValidatorEvent(TideValidatorEvent.VALID, _sourceContext, _entityProperty, false, false, null);
            else
                event = new TideValidatorEvent(TideValidatorEvent.INVALID, _sourceContext, _entityProperty, false, false, invalidValues);
            
	        _entity.dispatchEvent(event);
	    }
	    
	    public function fault(info:Object):void {
            var faultEvent:FaultEvent = info as FaultEvent;
            var emsg:ErrorMessage = faultEvent.message as ErrorMessage;
            
            var invalidValues:Array = emsg && emsg.extendedData ? emsg.extendedData.invalidValues as Array : null;
            if (invalidValues) {
                var event:TideValidatorEvent = new TideValidatorEvent(TideValidatorEvent.INVALID, _sourceContext, _entityProperty, false, false, invalidValues);
                _sourceContext.dispatchEvent(event);
            }
	    }
    }
}
