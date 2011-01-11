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

package test.granite.components {

    import mx.controls.dataGridClasses.DataGridColumn;

    public class DataGridColumn extends mx.controls.dataGridClasses.DataGridColumn {

        public function DataGridColumn(columnName:String = null) {
            super(columnName);
            this.sortCompareFunction = compareProperties;
            this.labelFunction = labelize;
        }

        private function labelize(item:Object, column:mx.controls.dataGridClasses.DataGridColumn):String {
            return getPropertyValue(item);
        }

        private function getPropertyValue(data:Object):String {
            var properties:Array = dataField.split(/\./);
            var label:Object = data;
            for (var i:uint = 0; i < properties.length && label != null; i++)
                label = label[properties[i]];
            return (label != null ? String(label) : ' ');
        }

        public function compareProperties(obj1:Object, obj2:Object):int {
            var s1:String = getPropertyValue(obj1);
            var s2:String = getPropertyValue(obj2);
            return (s1 < s2 ? -1 : (s1 == s2 ? 0 : 1));
        }
    }
}