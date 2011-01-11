package org.granite.tide.test.data
{
    import flexunit.framework.TestCase;
    
    import mx.binding.utils.BindingUtils;
    import mx.collections.ArrayCollection;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    
    
    public class TestDirtyCheckEntityEnum extends TestCase
    {
        public function TestDirtyCheckEntityEnum() {
            super("testDirtyCheckEntityEnum");
        }
        
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        public override function setUp():void {
            super.setUp();
            
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
        }
        
        
        public var ctxDirty:Boolean;
        public var personDirty:Boolean;
        
        public function testDirtyCheckEntityEnum():void {
        	var person:Person3 = new Person3();
        	person.version = 0;
        	person.salutation = Salutation.Dr;
        	
        	BindingUtils.bindProperty(this, "ctxDirty", _ctx, "meta_dirty");
        	BindingUtils.bindProperty(this, "personDirty", person, "meta_dirty");
        	
        	_ctx.person = _ctx.meta_mergeExternalData(person);
        	
        	assertFalse("Person not dirty", _ctx.meta_isEntityChanged(person));
        	
        	person.salutation = Salutation.Mr;
        	
        	assertTrue("Person dirty", _ctx.meta_isEntityChanged(person));
        	assertTrue("Person dirty 2", personDirty);
        	assertTrue("Context dirty", ctxDirty);
        	
        	person.salutation = Salutation.Dr;
        	
        	assertFalse("Person not dirty", _ctx.meta_isEntityChanged(person));
        	assertFalse("Person not dirty 2", personDirty);
        	assertFalse("Context not dirty", ctxDirty);
        	
        	person.salutation = null;
        	
        	assertTrue("Person dirty", _ctx.meta_isEntityChanged(person));
        	assertTrue("Person dirty 2", personDirty);
        	assertTrue("Context dirty", ctxDirty);
        	
        	person.salutation = Salutation.Dr;
        	
        	assertFalse("Person not dirty", _ctx.meta_isEntityChanged(person));
        	assertFalse("Person not dirty 2", personDirty);
        	assertFalse("Context not dirty", ctxDirty);
        }
    }
}
