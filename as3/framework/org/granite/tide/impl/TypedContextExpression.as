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

package org.granite.tide.impl {
	
	import org.granite.tide.IExpression;
	

    /**
     * @author William DRAI
     */
	[ExcludeClass]
    public class TypedContextExpression implements IExpression {
        
        private var _componentName:String;
        
        
        public function TypedContextExpression(componentName:String) {
            super();
			_componentName = componentName;
        }
        
        public function get componentName():String {
            return _componentName;
        }
        public function set componentName(componentName:String):void {
            _componentName = componentName;
        }
        
        public function get expression():String {
            return null;
        }
        
        public function get path():String {
            return _componentName;
        }
        
        public function toString():String {
            return path;
        }
    }
}