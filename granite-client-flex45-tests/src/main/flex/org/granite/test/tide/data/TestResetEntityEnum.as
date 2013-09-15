package org.granite.test.tide.data
{
    import org.flexunit.Assert;
    
    import mx.collections.ArrayCollection;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.test.tide.Contact;
    import org.granite.test.tide.Person;
    
    
    public class TestResetEntityEnum 
    {
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
        }
        
        
        [Test]
        public function testResetEntityEnum():void {
        	var person:Person6 = new Person6();
        	person.version = 0;
        	person.salutation = Salutation.Mr;
        	person.salutations = new ArrayCollection([ Salutation.Dr ]);
        	_ctx.person = _ctx.meta_mergeExternalData(person);
			person = _ctx.person;
			
        	person.salutation = null;
        	_ctx.meta_resetEntity(person);
        	
        	Assert.assertStrictlyEquals("Person reset", Salutation.Mr, person.salutation);
			
			person.salutation = Salutation.Dr;
			_ctx.meta_resetEntity(person);
			
			Assert.assertStrictlyEquals("Person reset 2", Salutation.Mr, person.salutation);
			
			person.salutations.removeItemAt(0);
			person.salutations.addItem(Salutation.Mr);
			_ctx.meta_resetEntity(person);
			
			Assert.assertEquals("Person reset coll", 1, person.salutations.length);
			Assert.assertStrictlyEquals("Person reset coll", Salutation.Dr, person.salutations.getItemAt(0));
        }
    }
}
