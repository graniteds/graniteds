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

package org.granite.tide.rpc {

    import mx.rpc.IResponder;
    import mx.rpc.AbstractOperation;
    
    import org.granite.tide.BaseContext;
	import org.granite.tide.IComponent;
    import org.granite.tide.ITideResponder;
    

    /**
     * @author William DRAI
     */
    [ExcludeClass]
    public class ComponentResponder implements IResponder {
        
        private var _sourceContext:BaseContext;
        private var _sourceModulePrefix:String;
        private var _component:IComponent;
		private var _componentName:String;
		private var _op:String;
		private var _args:Array;
        private var _operation:AbstractOperation;
        private var _clearOp:Boolean;
        private var _resultHandler:Function;
        private var _faultHandler:Function;
        private var _tideResponder:ITideResponder;
        private var _info:Object;
        
        
        public function ComponentResponder(sourceContext:BaseContext, resultHandler:Function, faultHandler:Function, 
            component:IComponent = null, op:String = null, args:Array = null, operation:AbstractOperation = null, clearOp:Boolean = false, tideResponder:ITideResponder = null, info:Object = null):void {
            _sourceContext = sourceContext;
            _sourceModulePrefix = _sourceContext.meta_tide.currentModulePrefix;
            _component = component;
			_componentName = component != null ? component.meta_name : null;
			_op = op;
			_args = args;
            _operation = operation;
            _clearOp = clearOp;
            _resultHandler = resultHandler;
            _faultHandler = faultHandler;
            _tideResponder = tideResponder;
            _info = info;   
        }
		
		public function set operation(operation:AbstractOperation):void {
			_operation = operation;
		}
		
		public function get op():String {
			return _op;
		}
		
		public function get args():Array {
			return _args;
		}
		
		public function get sourceContext():BaseContext {
			return _sourceContext;
		}
		
		public function get component():IComponent {
			return _component;
		}
        
        
	    public function result(data:Object):void {
	        if (_info != null)
		        _resultHandler(_sourceContext, _sourceModulePrefix, data, _info, _componentName, _op, _tideResponder, this);
		    else
		        _resultHandler(_sourceContext, _sourceModulePrefix, data, _componentName, _op, _tideResponder, this);
		    
		    if (_clearOp)
		        _operation.clearResult();
	    }
	    
	    public function fault(info:Object):void {
	        if (_info != null)
		        _faultHandler(_sourceContext, _sourceModulePrefix, info, _info, _componentName, _op, _tideResponder, this);
	        else
		        _faultHandler(_sourceContext, _sourceModulePrefix, info, _componentName, _op, _tideResponder, this);
		    
		    if (_clearOp)
				_operation.clearResult();
	    }
    }
}
