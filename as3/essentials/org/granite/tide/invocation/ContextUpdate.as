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

package org.granite.tide.invocation {
    
    import flash.utils.IDataInput;
    import flash.utils.IDataOutput;
    import flash.utils.IExternalizable;
    
    import org.granite.tide.IExpression;


    [ExcludeClass]
    [RemoteClass(alias="org.granite.tide.invocation.ContextUpdate")]
    /**
     * @author William DRAI
     */
    public class ContextUpdate implements IExpression {
        
        private var _componentName:String;
		private var _componentClassName:String;
        private var _expression:String;
        private var _value:Object;
        private var _scope:int;
        private var _restrict:Boolean;
        
        
        public function ContextUpdate(componentName:String = null, expr:String = null, value:Object = null, scope:int = 0, restrict:Boolean = false) {
            super();
            _componentName = componentName;
            _expression = expr;
            _value = value;
            _scope = scope;
            _restrict = restrict;
        }
        
        
        public function get componentName():String {
            return _componentName;
        }
        public function set componentName(componentName:String):void {
            _componentName = componentName;
        }
        
		public function get componentClassName():String {
			return _componentClassName;
		}
		public function set componentClassName(componentClassName:String):void {
			_componentClassName = componentClassName;
		}
		
        public function get expression():String {
            return _expression;
        }
        public function set expression(expression:String):void {
            _expression = expression;
        }
        
        public function get value():Object {
            return _value;
        } 
        public function set value(value:Object):void {
            _value = value;
        }
        
        public function get scope():int {
            return _scope;
        }
        public function set scope(scope:int):void {
            _scope = scope;
        }
        
        public function get restrict():Boolean {
            return _restrict;
        }
        public function set restrict(restrict:Boolean):void {
            _restrict = restrict;
        }
        
        public function get path():String {
			return _componentName + (_expression ? "." + _expression : "");
        }
        
        
        public function toString():String {
			return _componentName 
				+ (_componentClassName != null ? "(" + _componentClassName + ")" : "")			
				+ (_expression != null ? "." + _expression : "")
				+ (_scope == 1 ? " (SESSION)" : (_scope == 2 ? " (CONVERSATION)" : "")) 
				+ (_restrict ? " (restricted)" : "");
        }
    }
}