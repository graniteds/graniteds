package org.granite.test.tide.data
{
    import org.flexunit.Assert;
    
    import mx.collections.ArrayCollection;
    import mx.events.CollectionEvent;
    import mx.events.CollectionEventKind;
	import mx.collections.Sort;
	import mx.collections.SortField;

import org.granite.test.tide.Person;

import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.test.tide.Person0;
    
    
    public class TestMergeCollectionSortFilter
    {
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
        }
        
        
        [Test]
        public function testMergeCollectionSort():void {
        	var coll:ArrayCollection = new ArrayCollection();
        	coll.addItem(new Person0(1, "A1", "B1"));
        	coll.addItem(new Person0(2, "A2", "B1"));
        	coll.addItem(new Person0(3, "A3", "B1"));
			coll.addItem(new Person0(4, "A4", "B1"));
			coll.sort = new Sort();
			coll.sort.fields = [ new SortField("lastName") ];
			coll.refresh();
        	coll = _ctx.meta_mergeExternalData(coll) as ArrayCollection;
        	
        	var coll2:ArrayCollection = new ArrayCollection();
        	coll2.addItem(new Person0(4, "A4", "B1"));
        	coll2.addItem(new Person0(1, "A1", "B1"));
        	coll2.addItem(new Person0(2, "A2", "B1"));
        	coll2.addItem(new Person0(3, "A3", "B1"));
        	_ctx.meta_mergeExternalData(coll2, coll);
        	
        	Assert.assertEquals("Element count", 4, coll.length);
        }

        [Test]
        public function testMergeCollectionFilter():void {
        	var coll:ArrayCollection = new ArrayCollection();
        	coll.addItem(new Person0(1, "A1", "B1"));
        	coll.addItem(new Person0(2, "A2", "B1"));
        	coll.addItem(new Person0(3, "A3", "B2"));
			coll.addItem(new Person0(4, "A4", "B2"));
			coll.filterFunction = function(item:Person0):Boolean { return item.lastName == "B1"; };
			coll.refresh();
        	coll = _ctx.meta_mergeExternalData(coll) as ArrayCollection;
            var p3:Person0 = Person0(coll.list.getItemAt(2));
            var p4:Person0 = Person0(coll.list.getItemAt(3));

        	var coll2:ArrayCollection = new ArrayCollection();
            coll2.addItem(new Person0(1, "Z1", "B1"));
            coll2.addItem(new Person0(2, "Z2", "B1"));
            coll2.addItem(new Person0(3, "Z3", "B2"));
            coll2.addItem(new Person0(4, "Z4", "B2"));
        	_ctx.meta_mergeExternalData(coll2, coll);

        	Assert.assertEquals("Element count", 4, coll.list.length);
            Assert.assertEquals("Element 3 value", "Z3", p3.firstName);
            Assert.assertEquals("Element 4 value", "Z4", p4.firstName);
        }
    }
}
