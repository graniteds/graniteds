/*
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2015 GRANITE DATA SERVICES S.A.S.
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
package org.granite.tide.impl {
	
	import org.granite.tide.IExpression;
	

    /**
     * @author William DRAI
     */
	[ExcludeClass]
    public class TypedContextExpression implements IExpression {
        
        private var _componentName:String;
		private var _expression:String;
        
        
		public function TypedContextExpression(componentName:String, expression:String = null) {
            super();
			_componentName = componentName;
			_expression = expression;
        }
        
        public function get componentName():String {
            return _componentName;
        }
        public function set componentName(componentName:String):void {
            _componentName = componentName;
        }
        
        public function get expression():String {
			return _expression;
        }
        
        public function get path():String {
            return _componentName;
        }
        
        public function toString():String {
            return path;
        }
    }
}