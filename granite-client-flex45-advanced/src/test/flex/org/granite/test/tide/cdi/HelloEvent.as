/*
  GRANITE DATA SERVICES
  Copyright (C) 2007-2010 ADEQUATE SYSTEMS SARL

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

package org.granite.test.tide.cdi {

    import flash.utils.IDataInput;
    import flash.utils.IDataOutput;
    import flash.utils.IExternalizable;
    import flash.utils.flash_proxy;
    import mx.utils.object_proxy;

    use namespace flash_proxy;
    use namespace object_proxy;
    

    [Bindable]
	[RemoteClass(alias="org.granite.tide.test.cdi.HelloEvent")]
    public class HelloEvent implements IExternalizable {

        private var _message:String;

        public function HelloEvent(message:String = null):void {
            _message = message;
        }

        [Bindable(event="unused")]
        public function get message():String {
            return _message;
        }

        public function readExternal(input:IDataInput):void {
            _message = input.readObject() as String;
        }

        public function writeExternal(output:IDataOutput):void {
            output.writeObject(_message);
        }
    }
}
