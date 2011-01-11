package org.granite.tide.test.data
{
    import flexunit.framework.TestCase;
    
    import mx.collections.ArrayCollection;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.tide.test.Contact;
    import org.granite.tide.test.Person;
    
    
    public class TestResetEntityEnum extends TestCase
    {
        public function TestResetEntityEnum() {
            super("testResetEntityEnum");
        }
        
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        public override function setUp():void {
            super.setUp();
            
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
        }
        
        
        public function testResetEntityEnum():void {
        	var person:Person6 = new Person6();
        	person.version = 0;
        	person.salutation = Salutation.Mr;
        	person.salutations = new ArrayCollection([ Salutation.Dr ]);
        	_ctx.person = _ctx.meta_mergeExternalData(person);
			person = _ctx.person;
			
        	person.salutation = null;
        	_ctx.meta_resetEntity(person);
        	
        	assertStrictlyEquals("Person reset", Salutation.Mr, person.salutation);
			
			person.salutation = Salutation.Dr;
			_ctx.meta_resetEntity(person);
			
			assertStrictlyEquals("Person reset 2", Salutation.Mr, person.salutation);
			
			person.salutations.removeItemAt(0);
			person.salutations.addItem(Salutation.Mr);
			_ctx.meta_resetEntity(person);
			
			assertEquals("Person reset coll", 1, person.salutations.length);
			assertStrictlyEquals("Person reset coll", Salutation.Dr, person.salutations.getItemAt(0));
        }
    }
}
