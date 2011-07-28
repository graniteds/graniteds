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

package org.granite.example.addressbook.seam.ctl {
	
    import mx.collections.ListCollectionView;
    import mx.controls.DataGrid;
    import mx.events.CollectionEvent;
    import mx.events.FlexEvent;
    
    import org.granite.example.addressbook.entity.*;
    import org.granite.example.addressbook.events.*;
    
    
    [Bindable]
    [Name("searchCtl", restrict="true")]
    public class SearchCtl {
        
        public var persons:DataGrid;
        
        public var personHome:Object;
        
        private var _people:ListCollectionView;
        
        public function set people(people:ListCollectionView):void {
        	_people = people;
            _people.addEventListener(CollectionEvent.COLLECTION_CHANGE, refreshCollHandler);
        }
        
        [In]
        public var examplePerson:Person;
        
        
        [Observer]
        public function search(event:SearchEvent):void {
            examplePerson.lastName = event.searchString;
            _people.refresh();
        }
        
        [Observer("org.granite.tide.data.refresh.Person")]
        public function refreshHandler():void {
            // Force wake up this component before the first time the collection of persons is about to be refreshed
        }
        
        private function refreshCollHandler(event:CollectionEvent):void {
            if (_people.getItemIndex(personHome.instance) < 0)
                personHome.instance = null;
            if (personHome.instance != null)
                persons.selectedItems = [personHome.instance];
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
