package org.granite.test.tide.data
{
    import flash.utils.ByteArray;
    
    import mx.collections.ArrayCollection;
    
    import org.flexunit.Assert;
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    
    
    public class TestResetEntityByteArray
    {
        private var _ctx:BaseContext = Tide.getInstance().getContext();
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
        }
        
		
		private function newByteArray(s:String):ByteArray {
			var ba:ByteArray = new ByteArray();
			ba.position = 0;
			ba.writeUTF(s);
			ba.position = 0;
			return ba;
		}
		
		private function readByteArray(ba:ByteArray):String {
			if (ba == null)
				return null;
			ba.position = 0;
			var s:String = ba.readUTF();
			ba.position = 0;
			return s;
		}
		
        
        [Test]
        public function testResetEntityByteArray():void {
			
        	var person:Person7b = new Person7b();
        	person.version = 0;
        	person.byteArray = newByteArray("bla");
        	person.byteArrays = new ArrayCollection([ newByteArray("blo") ]);
        	_ctx.person = _ctx.meta_mergeExternalData(person);
			person = _ctx.person;
			
			person.byteArray = null;
        	_ctx.meta_resetEntity(person);
        	
        	Assert.assertTrue("Person reset", "bla", readByteArray(person.byteArray));
        	
        	person.byteArray = newByteArray("300");
        	_ctx.meta_resetEntity(person);
        	
        	Assert.assertTrue("Person reset 2", "bla", readByteArray(person.byteArray));
			
			person.byteArrays.setItemAt(newByteArray("zzz"), 0);
			_ctx.meta_resetEntity(person);
			
			Assert.assertEquals("Person reset coll", 1, person.byteArrays.length);
			Assert.assertTrue("Person reset coll", "blo", readByteArray(person.byteArrays.getItemAt(0) as ByteArray));
        }
    }
}
