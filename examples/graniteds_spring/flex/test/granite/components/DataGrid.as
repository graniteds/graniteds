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

    import mx.collections.Sort;
    import mx.collections.SortField;
    import mx.controls.DataGrid;
    import mx.core.EventPriority;
    import mx.events.DataGridEvent;
    import mx.events.FlexEvent;
    import mx.collections.IViewCursor;
    import mx.controls.Alert;

    public class DataGrid extends mx.controls.DataGrid {

        public function DataGrid() {
            super();

            super.addEventListener(FlexEvent.DATA_CHANGE, dataChangeHandler, false, EventPriority.DEFAULT_HANDLER);
        }

        override public function addEventListener(
            type:String, listener:Function, useCapture:Boolean = false,
            priority:int = 0, useWeakReference:Boolean = false):void  {

            if (type === DataGridEvent.HEADER_RELEASE && !hasEventListener(type))
                super.addEventListener(type, preHeaderReleaseHandler, useCapture, priority, useWeakReference);

            super.addEventListener(type, listener, useCapture, priority, useWeakReference);
        }

        private function dataChangeHandler(event:FlexEvent):void {
            Alert.show(String(event.currentTarget));
        }

        private function preHeaderReleaseHandler(event:DataGridEvent):void {
            if (!event.isDefaultPrevented()) {
                var column:DataGridColumn = columns[event.columnIndex];
                if (column.sortable && column.dataField.indexOf('.') != -1) {
                    var s:Sort = collection.sort;
                    collection.sort = new PropertySort(column.sortCompareFunction);
                    if (s)
                        collection.sort.fields = s.fields;
                }
            }
        }
    }
}
