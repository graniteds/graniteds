package test.granite.ctl
{
    import mx.controls.DataGrid;
    import mx.events.CollectionEvent;
    import mx.events.CollectionEventKind;
    import mx.events.FlexEvent;
    
    import org.granite.tide.collections.PagedQuery;
    import org.granite.tide.events.TideContextEvent;
    
    import test.granite.ejb3.entity.Person;
    
    
    [Bindable]
    [Name("searchCtl")]
    public class SearchCtl {
        
        public var persons:DataGrid;
        
        private var _peopleService:PagedQuery;
        
        public function set peopleService(peopleService:PagedQuery):void {
        	_peopleService = peopleService;
            _peopleService.addEventListener(CollectionEvent.COLLECTION_CHANGE, refreshCollHandler);
        }
        
        [In] [Out]
        public var person:Person;
        
        
        [Observer("search")]
        public function search(text:String):void {
            _peopleService.filter.lastName = text;
            _peopleService.refresh();
        }
        
        [Observer("org.granite.tide.data.refresh.Person")]
        public function refreshHandler():void {
            // Force wake up this component before the first time the collection of persons is about to be refreshed
        }
        
        private function refreshCollHandler(event:CollectionEvent):void {
            var idx:int = _peopleService.getItemIndex(person);
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
