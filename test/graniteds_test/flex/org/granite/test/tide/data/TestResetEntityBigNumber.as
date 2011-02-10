package org.granite.test.tide.data
{
    import org.flexunit.Assert;
    
    import mx.collections.ArrayCollection;
    
    import org.granite.math.BigInteger;
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    
    
    public class TestResetEntityBigNumber 
    {
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
        }
        
        
        [Test]
        public function testResetEntityBigNumber():void {
        	var person:Person7 = new Person7();
        	person.version = 0;
        	person.bigInt = new BigInteger(100);
        	person.bigInts = new ArrayCollection([ new BigInteger(200) ]);
        	_ctx.person = _ctx.meta_mergeExternalData(person);
			person = _ctx.person;
			
			person.bigInt = null;
        	_ctx.meta_resetEntity(person);
        	
        	Assert.assertTrue("Person reset", new BigInteger(100).equals(person.bigInt));
        	
        	person.bigInt = new BigInteger(300);
        	_ctx.meta_resetEntity(person);
        	
        	Assert.assertTrue("Person reset 2", new BigInteger(100).equals(person.bigInt));
			
			person.bigInts.setItemAt(new BigInteger(300), 0);
			_ctx.meta_resetEntity(person);
			
			Assert.assertEquals("Person reset coll", 1, person.bigInts.length);
			Assert.assertTrue("Person reset coll", new BigInteger(200).equals(person.bigInts.getItemAt(0)));
        }
    }
}
