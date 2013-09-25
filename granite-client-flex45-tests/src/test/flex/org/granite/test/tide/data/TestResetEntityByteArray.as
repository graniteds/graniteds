/*
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of the Granite Data Services Platform.
 *
 *   Granite Data Services is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Lesser General Public
 *   License as published by the Free Software Foundation; either
 *   version 2.1 of the License, or (at your option) any later version.
 *
 *   Granite Data Services is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 *   General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public
 *   License along with this library; if not, write to the Free Software
 *   Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301,
 *   USA, or see <http://www.gnu.org/licenses/>.
 */
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
        	
        	Assert.assertEquals("Person reset", "bla", readByteArray(person.byteArray));
        	
        	person.byteArray = newByteArray("300");
        	_ctx.meta_resetEntity(person);
        	
        	Assert.assertEquals("Person reset 2", "bla", readByteArray(person.byteArray));
			
			person.byteArrays.setItemAt(newByteArray("zzz"), 0);
			_ctx.meta_resetEntity(person);
			
			Assert.assertEquals("Person reset coll", 1, person.byteArrays.length);
			Assert.assertEquals("Person reset coll", "blo", readByteArray(person.byteArrays.getItemAt(0) as ByteArray));
        }
    }
}
