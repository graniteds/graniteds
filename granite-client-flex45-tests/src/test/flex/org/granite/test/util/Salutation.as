/*
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2014 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of the Granite Data Services Platform.
 *
 *   Granite Data Services is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Lesser General Public
 *   License as published by the Free Software Foundation; either
 *   version 2.1 of the License, or (at your option) any later version.
 *
 *   Granite Data Services is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 *   General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public
 *   License along with this library; if not, write to the Free Software
 *   Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301,
 *   USA, or see <http://www.gnu.org/licenses/>.
 */
package org.granite.test.util {

    import org.granite.util.Enum;

    [Bindable]
    [RemoteClass(alias="org.granite.test.Salutation")]
    public class Salutation extends Enum {

        public static const Mr:Salutation = new Salutation("Mr", _);
        public static const Ms:Salutation = new Salutation("Ms", _);
        public static const Dr:Salutation = new Salutation("Dr", _);

        function Salutation(value:String = null, restrictor:* = null) {
            super((value || Mr.name), restrictor);
        }

        override protected function getConstants():Array {
            return constants;
        }

        public static function get constants():Array {
            return [Mr, Ms, Dr];
        }

        public static function valueOf(name:String):Salutation {
            return Salutation(Mr.constantOf(name));
        }
    }
}