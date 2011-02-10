package org.granite.test.tide.data
{
    import org.flexunit.Assert;
    
    import org.granite.meta;
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.test.tide.Group;
    import org.granite.test.tide.User;
    
    
    public class TestMergeLazyEntity2 
    {
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
        }
        
        
        [Test]
        public function testMergeLazyEntity2():void {
        	var user:User = new User();
        	user.username = 'toto';
        	user.meta::setInitialized(false);
        	
        	var group:Group = new Group();
        	group.name = 'tutu';
        	group.user = user;
        	
        	_ctx.group = _ctx.meta_mergeExternal(group);
        	group = _ctx.group;
        	
        	Assert.assertFalse("User not initialized", group.user.meta::isInitialized());
        	
        	Assert.assertNull("User not init 2", group.user.name);
        	
        	var user2:User = new User();
        	user2.username = 'toto';
        	user2.name = 'Jean Richard';
        	var group2:Group = new Group();
        	group2.name = 'tutu'
        	group2.user = user2;
        	
        	_ctx.meta_mergeExternal(group2);
        	
        	Assert.assertTrue("User initialized", group.user.meta::isInitialized());
        }
    }
}
