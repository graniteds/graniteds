package org.granite.tide.test.data
{
    import flexunit.framework.TestCase;
    
    import mx.collections.ArrayCollection;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.tide.test.Contact;
    import org.granite.tide.test.Person;
    
    
    public class TestResetEntityEnum2 extends TestCase
    {
        public function TestResetEntityEnum2() {
            super("testResetEntityEnum2");
        }
        
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        public override function setUp():void {
            super.setUp();
            
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
        }
        
        
        public function testResetEntityEnum2():void {
        	var person:Person6 = new Person6();
        	person.version = 0;
        	person.salutation = Salutation.Mr;
        	person.salutations = new ArrayCollection([ Salutation.Dr ]);
        	_ctx.person = _ctx.meta_mergeExternalData(person);
			person = _ctx.person;
			
			person.salutations.setItemAt(Salutation.Mr, 0);
			_ctx.meta_resetEntity(person);
			
			assertEquals("Person reset coll", 1, person.salutations.length);
			assertStrictlyEquals("Person reset coll", Salutation.Dr, person.salutations.getItemAt(0));
        }
    }
}
