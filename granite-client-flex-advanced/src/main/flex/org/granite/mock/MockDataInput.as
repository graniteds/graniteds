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