package org.granite.tide.test.data
{
    import flexunit.flexui.patterns.AssertNullPattern;
    import flexunit.framework.TestCase;
    
    import org.granite.meta;
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.tide.test.Group;
    import org.granite.tide.test.User;
    
    
    public class TestMergeLazyEntity2 extends TestCase
    {
        public function TestMergeLazyEntity2() {
            super("testMergeLazyEntity2");
        }
        
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        public override function setUp():void {
            super.setUp();
            
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
        }
        
        
        public function testMergeLazyEntity2():void {
        	var user:User = new User();
        	user.username = 'toto';
        	user.meta::setInitialized(false);
        	
        	var group:Group = new Group();
        	group.name = 'tutu';
        	group.user = user;
        	
        	_ctx.group = _ctx.meta_mergeExternal(group);
        	group = _ctx.group;
        	
        	assertFalse("User not initialized", group.user.meta::isInitialized());
        	
        	assertNull("User not init 2", group.user.name);
        	
        	var user2:User = new User();
        	user2.username = 'toto';
        	user2.name = 'Jean Richard';
        	var group2:Group = new Group();
        	group2.name = 'tutu'
        	group2.user = user2;
        	
        	_ctx.meta_mergeExternal(group2);
        	
        	assertTrue("User initialized", group.user.meta::isInitialized());
        }
    }
}
