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

    import flash.utils.IDataInput;
    import flash.utils.IDataOutput;
    import flash.utils.IExternalizable;


    [RemoteClass(alias="org.granite.tide.validators.InvalidValue")]
    /**
     * 	Container class for validation errors on entities.<br/>
     *  Equivalent to Hibernate Validator InvalidValue class. 
     *  
     * 	@author William DRAI
     */
    public class InvalidValue implements IExternalizable {

        private var _rootBean:Object;
        private var _bean:Object;
        private var _beanClass:String;
        private var _path:String;
        private var _value:Object;
        private var _message:String;

        public function InvalidValue(rootBean:Object = null, bean:Object = null, beanClass:String = null, path:String = null, value:Object = null, message:String = null) {
            super();
			_rootBean = rootBean;
			_bean = bean;
			_beanClass = beanClass;
			_path = path;
			_value = value;
			_message = message;
        }

		public function get rootBean():Object {
			return _rootBean;
		}
		
        public function get bean():Object {
            return _bean;
        }

        public function get beanClass():String {
            return _beanClass;
        }

        public function get path():String {
            return _path;
        }

        public function get value():Object {
            return _value;
        }

        public function get message():String {
            return _message;
        }


		/**
		 * 	@private
		 */
        public function readExternal(input:IDataInput):void {
        	_rootBean = input.readObject();
            _bean = input.readObject();
            _beanClass = input.readObject() as String;
            _path = input.readObject() as String;
            _value = input.readObject();
            _message = input.readObject() as String;
        }

		/**
		 * 	@private
		 */
        public function writeExternal(output:IDataOutput):void {
            // read only bean...
        }
    }
}
