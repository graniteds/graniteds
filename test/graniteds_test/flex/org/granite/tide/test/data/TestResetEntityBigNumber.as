package org.granite.tide.test.data
{
    import flexunit.framework.TestCase;
    
    import mx.collections.ArrayCollection;
    
    import org.granite.math.BigInteger;
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    
    
    public class TestResetEntityBigNumber extends TestCase
    {
        public function TestResetEntityBigNumber() {
            super("testResetEntityBigNumber");
        }
        
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        public override function setUp():void {
            super.setUp();
            
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
        }
        
        
        public function testResetEntityBigNumber():void {
        	var person:Person7 = new Person7();
        	person.version = 0;
        	person.bigInt = new BigInteger(100);
        	person.bigInts = new ArrayCollection([ new BigInteger(200) ]);
        	_ctx.person = _ctx.meta_mergeExternalData(person);
			person = _ctx.person;
			
			person.bigInt = null;
        	_ctx.meta_resetEntity(person);
        	
        	assertTrue("Person reset", new BigInteger(100).equals(person.bigInt));
        	
        	person.bigInt = new BigInteger(300);
        	_ctx.meta_resetEntity(person);
        	
        	assertTrue("Person reset 2", new BigInteger(100).equals(person.bigInt));
			
			person.bigInts.setItemAt(new BigInteger(300), 0);
			_ctx.meta_resetEntity(person);
			
			assertEquals("Person reset coll", 1, person.bigInts.length);
			assertTrue("Person reset coll", new BigInteger(200).equals(person.bigInts.getItemAt(0)));
        }
    }
}
