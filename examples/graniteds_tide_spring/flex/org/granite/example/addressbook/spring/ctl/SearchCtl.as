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

package org.granite.example.addressbook.spring.ctl {

	import mx.controls.DataGrid;
    import mx.events.CollectionEvent;
    import mx.events.FlexEvent;
    
    import org.granite.tide.spring.PagedQuery;
    
    import org.granite.example.addressbook.entity.Person;
    
    
    [Bindable]
    [Name("searchCtl")]
    public class SearchCtl {
        
        public var persons:DataGrid;
        
        private var _people:PagedQuery;
        
        public function set people(people:PagedQuery):void {
        	_people = people;
            _people.addEventListener(CollectionEvent.COLLECTION_CHANGE, refreshCollHandler);
        }
        
        [In] [Out]
        public var person:Person;
        
        
        [Observer("search")]
        public function search(text:String):void {
            _people.filter.lastName = text;
            _people.refresh();
        }
        
        [Observer("org.granite.tide.data.refresh.Person")]
        public function refreshHandler():void {
            // Force wake up this component before the first time the collection of persons is about to be refreshed
        }
        
        private function refreshCollHandler(event:CollectionEvent):void {
            var idx:int = _people.getItemIndex(person);
            if (idx < 0)
                person = null;
            if (person != null) {
                persons.selectedItems = [person];
                persons.selectedItem = person;
                persons.selectedIndex = idx;
                persons.dispatchEvent(new FlexEvent(FlexEvent.VALUE_COMMIT));
            }
            else {
                persons.selectedItems = [];
                persons.selectedItem = null;
                persons.selectedIndex = -1;
                // Workaround for DataGrid strange behaviour when deleting last element
                persons.dispatchEvent(new FlexEvent(FlexEvent.VALUE_COMMIT));
            }
        }
    }
}
