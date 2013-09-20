package org.granite.test.tide.data
{
    import mx.binding.utils.BindingUtils;
    import mx.binding.utils.ChangeWatcher;
    import mx.collections.ArrayCollection;
    
    import org.flexunit.Assert;
    import org.granite.collections.BasicMap;
    import org.granite.test.tide.Contact;
    import org.granite.test.tide.Person;
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.tide.data.Change;
    import org.granite.tide.data.ChangeRef;
    import org.granite.tide.data.ChangeSet;
    import org.granite.tide.data.ChangeSetBuilder;
    
    
    public class TestChangeSetEntity3
    {
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
        }
        
        
        [Test]
        public function testChangeSetEntity3():void {
        	var person:Person4 = new Person4();
			person.uid = "P1";
			person.id = 1;
			person.version = 0;
        	person.salutation = Salutation.Dr;
			person.address = new EmbeddedAddress();
        	
        	_ctx.person = _ctx.meta_mergeExternal(person);

        	person = _ctx.person;
			
			var changeSet:ChangeSet = new ChangeSetBuilder(_ctx).buildChangeSet();
			
			Assert.assertEquals("ChangeSet empty", 0, changeSet.changes.length);
			
			person.salutation = Salutation.Mr;
			person.address.address1 = "1 rue des Bleuets";
			
			changeSet = new ChangeSetBuilder(_ctx).buildChangeSet();
			
			Assert.assertEquals("ChangeSet count 1", 1, changeSet.changes.length);
			Assert.assertEquals("ChangeSet salutation value", Salutation.Mr, changeSet.changes[0].changes.salutation);
			Assert.assertEquals("ChangeSet address value", "1 rue des Bleuets", changeSet.changes[0].changes.address.address1);
		}
    }
}
