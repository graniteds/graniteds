package org.granite.test.tide.framework
{
    import org.flexunit.Assert;
    import org.granite.persistence.PersistentSet;
    import org.granite.test.util.Util;
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.tide.collections.PersistentCollection;
    
    
    public class TestUtil
    {
        private var _ctx:BaseContext;
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
            Tide.getInstance().initApplication();
        }
        
        
        [Test]
        public function testToString():void {
			Util['externalToString'](tideToString);
			
			var entity:Object = new Object();
			entity.test = new PersistentCollection(null, "test", new PersistentSet(false));
			Assert.assertEquals("Uninitialized collection for object: null test", Util.toString(entity.test));
        }
		
		private function tideToString(value:Object, namespaceURIs:Array = null, exclude:Array = null):String {
			return BaseContext.toString(value);
		}
    }
}
