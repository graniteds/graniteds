package org.granite.tide.test.data
{
    import flexunit.framework.TestCase;
    
    import mx.binding.utils.BindingUtils;
    
    import org.granite.math.BigInteger;
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    
    
    public class TestDirtyCheckEntityBigNumber extends TestCase
    {
        public function TestDirtyCheckEntityBigNumber() {
            super("testDirtyCheckEntityBigNumber");
        }
        
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        public override function setUp():void {
            super.setUp();
            
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
        }
        
        
        public var ctxDirty:Boolean;
        public var personDirty:Boolean;
        
        public function testDirtyCheckEntityBigNumber():void {
        	var person:Person7 = new Person7();
        	person.version = 0;
        	person.bigInt = new BigInteger(100);
        	
        	BindingUtils.bindProperty(this, "ctxDirty", _ctx, "meta_dirty");
        	BindingUtils.bindProperty(this, "personDirty", person, "meta_dirty");
        	
        	_ctx.person = _ctx.meta_mergeExternalData(person);
        	
        	assertFalse("Person not dirty", _ctx.meta_isEntityChanged(person));
        	
        	person.bigInt = new BigInteger(200);
        	
        	assertTrue("Person dirty", _ctx.meta_isEntityChanged(person));
        	assertTrue("Person dirty 2", personDirty);
        	assertTrue("Context dirty", ctxDirty);
        	
        	person.bigInt = new BigInteger(100);
        	
        	assertFalse("Person not dirty", _ctx.meta_isEntityChanged(person));
        	assertFalse("Person not dirty 2", personDirty);
        	assertFalse("Context not dirty", ctxDirty);
        	
        	person.bigInt = null;
        	
        	assertTrue("Person dirty", _ctx.meta_isEntityChanged(person));
        	assertTrue("Person dirty 2", personDirty);
        	assertTrue("Context dirty", ctxDirty);
        	
        	person.bigInt = new BigInteger(100);
        	
        	assertFalse("Person not dirty", _ctx.meta_isEntityChanged(person));
        	assertFalse("Person not dirty 2", personDirty);
        	assertFalse("Context not dirty", ctxDirty);
        }
    }
}
