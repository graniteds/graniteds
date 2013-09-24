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
package org.granite.tide.impl {

	import flash.utils.Dictionary;
	
	import org.granite.reflect.Annotation;
	import org.granite.reflect.Type;

    /**
     * 	Cache used with DescribeTypeCahe for component descriptions
     *  
     * 	@author William DRAI
     */
    [ExcludeClass]
    public class ComponentInfo {
    	
    	private var _name:String;
    	private var _create:String;
    	private var _module:String;
    	private var _scope:String;
    	private var _restrict:String;
    	private var _events:Array;
    	
    	
		public function ComponentInfo(desc:XML):void {
			var names:XMLList = desc..metadata.(@name == 'Name');
			if (names.length() > 0) {
				_name = names[0]..arg.(@key == '').@value.toXMLString();
				_create = names[0]..arg.(@key == 'create').@value.toXMLString();
				_module = names[0]..arg.(@key == 'module').@value.toXMLString();
				_scope = names[0]..arg.(@key == 'scope').@value.toXMLString();
				_restrict = names[0]..arg.(@key == 'restrict').@value.toXMLString();
			}
		}
    	
    	public function get name():String {
    		return _name;
    	}
    	
    	public function get create():String {
    		return _create;
    	}
    	
    	public function get module():String {
    		return _module;
    	}
    	
    	public function get scope():String {
    		return _scope;
    	}
    	
    	public function get restrict():String {
    		return _restrict;
    	}
    }
}
