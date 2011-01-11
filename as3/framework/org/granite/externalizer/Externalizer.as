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

package org.granite.externalizer {

    import flash.utils.Dictionary;
    import flash.utils.IDataInput;
    import flash.utils.IDataOutput;
    import flash.utils.IExternalizable;
    
    import mx.collections.ArrayCollection;
    import mx.collections.Sort;
    import mx.collections.SortField;
    
    import org.granite.reflect.Field;
    import org.granite.reflect.Type;

    /**
     * Static functions for GDS externalization (roughly based on original code from the author).
     *
     * @author Philippe CHAURANT
     */
    public class Externalizer {

        private static var _cache:Dictionary = new Dictionary(true);

        private static function getOrderedFields(object:Object):ArrayCollection {
			var type:Type = Type.forInstance(object);
			
            var fields:ArrayCollection = _cache[type];

            if (fields === null) {
                fields = new ArrayCollection();

				for each (var f:Field in type.properties) {
                    var field:_Field = new _Field();
                    field.fieldName = f.name;
                    field.fieldClass = f.type.getClass();
                    fields.addItem(field)
                 }

                var dataSortField:SortField = new SortField("fieldName");
                var dataSort:Sort = new Sort();
                dataSort.fields = [dataSortField];
                fields.sort = dataSort;
                fields.refresh();

                _cache[type] = fields;
            }

            return fields;
        }

        public static function readExternal(object:IExternalizable, input:IDataInput):void {
            var fields:ArrayCollection = getOrderedFields(object);
            for each(var field:_Field in fields)
                object[field.fieldName] = input.readObject() as field.fieldClass;
        }

        public static function writeExternal(object:IExternalizable, output:IDataOutput):void {
            var fields:ArrayCollection = getOrderedFields(object);
            for each(var field:_Field in fields)
                output.writeObject(object[field.fieldName]);
        }
    }
}

class _Field {
    public var fieldName:String;
    public var fieldClass:Class;
}
