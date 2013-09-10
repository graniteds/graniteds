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

    import flash.utils.Dictionary;
    
    import mx.rpc.IResponder;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.collections.PersistentCollection;
    

    /**
     * @author William DRAI
     */
    [ExcludeClass] 
    public class InitializerResponder implements IResponder {
        
        private var _sourceContext:BaseContext;
        private var _object:Object;
        private var _propertyNames:Array;
        private var _resultHandler:Function;
        private var _faultHandler:Function;
        
        
        public function InitializerResponder(sourceContext:BaseContext, resultHandler:Function, faultHandler:Function, 
            object:Object, propertyNames:Array):void {
            _sourceContext = sourceContext;
            _object = object;
            _propertyNames = propertyNames;
            _resultHandler = resultHandler;
            _faultHandler = faultHandler;
        }
        
        public function get context():BaseContext {
            return _sourceContext;
        }
        
        
	    public function result(data:Object):void {
		    _resultHandler(_sourceContext, data, _object, _propertyNames);
	    }
	    
	    public function fault(info:Object):void {
		    _faultHandler(_sourceContext, info, _object, _propertyNames);
	    }
    }
}
