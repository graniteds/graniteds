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
