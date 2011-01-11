package test.granite.ctl
{
    import mx.controls.DataGrid;
    import mx.events.CollectionEvent;
    import mx.events.FlexEvent;
    
    import org.granite.tide.spring.PagedQuery;
    
    import test.granite.spring.entity.Person;
    
    
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
