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

package org.granite.mock {

    import flash.utils.IDataInput;
    import flash.utils.ByteArray;
    import flash.errors.IllegalOperationError;

    /**
     * @author Franck WOLFF
     */
    public class MockDataInput implements IDataInput {

        private static const NOT_IMPLEMENTED:String = "Not implemented";

        private var _fields:Array = null;
        private var _index:int = 0;

        public function MockDataInput(fields:Array) {
            _fields = fields;
        }

        public function readObject():* {
            return _fields[_index++];
        }

        public function get bytesAvailable():uint {
            throw new IllegalOperationError(NOT_IMPLEMENTED);
        }
        public function get endian():String{
            throw new IllegalOperationError(NOT_IMPLEMENTED);
        }
        public function set endian(value:String):void {
            throw new IllegalOperationError(NOT_IMPLEMENTED);
        }
        public function get objectEncoding():uint {
            throw new IllegalOperationError(NOT_IMPLEMENTED);
        }
        public function set objectEncoding(value:uint):void {
            throw new IllegalOperationError(NOT_IMPLEMENTED);
        }

        public function readBoolean():Boolean {
            throw new IllegalOperationError(NOT_IMPLEMENTED);
        }
        public function readByte():int {
            throw new IllegalOperationError(NOT_IMPLEMENTED);
        }
        public function readBytes(bytes:ByteArray, offset:uint = 0, length:uint = 0):void {
            throw new IllegalOperationError(NOT_IMPLEMENTED);
        }
        public function readDouble():Number {
            throw new IllegalOperationError(NOT_IMPLEMENTED);
        }
        public function readFloat():Number {
            throw new IllegalOperationError(NOT_IMPLEMENTED);
        }
        public function readInt():int {
            throw new IllegalOperationError(NOT_IMPLEMENTED);
        }
        public function readMultiByte(length:uint, charSet:String):String {
            throw new IllegalOperationError(NOT_IMPLEMENTED);
        }
        public function readShort():int {
            throw new IllegalOperationError(NOT_IMPLEMENTED);
        }
        public function readUnsignedByte():uint {
            throw new IllegalOperationError(NOT_IMPLEMENTED);
        }
        public function readUnsignedInt():uint {
            throw new IllegalOperationError(NOT_IMPLEMENTED);
        }
        public function readUnsignedShort():uint {
            throw new IllegalOperationError(NOT_IMPLEMENTED);
        }
        public function readUTF():String {
            throw new IllegalOperationError(NOT_IMPLEMENTED);
        }
        public function readUTFBytes(length:uint):String {
            throw new IllegalOperationError(NOT_IMPLEMENTED);
        }
    }
}