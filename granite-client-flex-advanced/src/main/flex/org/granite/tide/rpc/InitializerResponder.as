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
