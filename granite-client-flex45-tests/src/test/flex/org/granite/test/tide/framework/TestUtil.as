/*
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2014 GRANITE DATA SERVICES S.A.S.
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
package org.granite.test.tide.framework
{

    import flash.net.registerClassAlias;
    import flash.utils.getQualifiedClassName;

    import mx.collections.ArrayCollection;
    import mx.core.FlexVersion;
    import mx.core.mx_internal;

    import mx.messaging.messages.AsyncMessage;
    import mx.messaging.messages.ErrorMessage;
    import mx.utils.RPCObjectUtil;

    import org.flexunit.Assert;
    import org.granite.persistence.PersistentSet;
    import org.granite.test.tide.Person;
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
			entity.test = new PersistentCollection(null, null, "test", new PersistentSet(false));
			Assert.assertEquals("(" + getQualifiedClassName(entity.test) + ") [ Uninitialized collection for object: null test ]", Util.toString(entity.test));
        }

		private function tideToString(value:Object, namespaceURIs:Array = null, exclude:Array = null):String {
			return BaseContext.toString(value);
		}


        [Test]
        public function testRPCObjectUtil():void {
            var message:AsyncMessage = new AsyncMessage();

            var s:String = message.toString();
            Assert.assertNotNull("toString", s);
        }

        private static function isApacheFlex():Boolean {
            return FlexVersion.CURRENT_VERSION >= 0x04100000;
        }

        [Test]
        public function testToStringMessage():void {
            if (!isApacheFlex())
                return;
            var message:AsyncMessage = new AsyncMessage();
            var entity:Object = new Object();
            entity.test = new PersistentCollection(null, null, "test", new PersistentSet(false));
            message.body = entity;

            var s:String = message.toString();
            Assert.assertTrue(s.indexOf("[ Uninitialized collection for object: null test ]") > 0);
            Assert.assertFalse("Not initialized", entity.test.isInitialized());
        }

        [Test]
        public function testToStringMessage2():void {
            if (!isApacheFlex())
                return;
            var message:AsyncMessage = new AsyncMessage();
            var entity:Object = new Object();
            entity.test = new PersistentCollection(null, null, "test", new PersistentSet(false));
            entity.ref = entity;
            message.body = entity;

            var s:String = message.toString();
            Assert.assertTrue(s.indexOf("[ Uninitialized collection for object: null test ]") > 0);
            Assert.assertFalse("Not initialized", entity.test.isInitialized());
        }

        [Test]
        public function testToStringMessage3():void {
            if (!isApacheFlex())
                return;
            var message:AsyncMessage = new AsyncMessage();
            var entity:Object = new Object();
            entity.test = new PersistentCollection(null, null, "test", new PersistentSet(false));
            entity.ref = entity;
            entity.test2 = new TestObject();
            entity.test3 = new TestObject2();
            message.body = entity;

            var s:String = message.toString();
            Assert.assertTrue(s.indexOf("[ Uninitialized collection for object: null test ]") > 0);
            Assert.assertFalse("Not initialized", entity.test.isInitialized());
            Assert.assertTrue(s.indexOf("Bla") > 0);

            var s2:String = message.toString();
            Assert.assertEquals("Same", s, s2);
        }

        [Test]
        public function testToStringMessage4():void {
            if (!isApacheFlex())
                return;
            var message:AsyncMessage = new AsyncMessage();
            var entity:Object = new Object();
            entity.test = new PersistentCollection(null, null, "test", new PersistentSet(false));
            entity.ref = entity;
            entity.test2 = new TestObject();
            entity.test3 = new TestObject2();
            entity.test3.test2 = new TestObject2();
            entity.test3.test2.test2 = new TestObject3();
            entity.test4 = new Person();
            entity.test4.contacts = new ArrayCollection();
            entity.test4.contacts.addItem(message);
            message.body = entity;

            var s:String = message.toString();
            Assert.assertTrue(s.indexOf("[ Uninitialized collection for object: null test ]") > 0);
            Assert.assertFalse("Not initialized", entity.test.isInitialized());
            Assert.assertTrue(s.indexOf("Bla") > 0);
            Assert.assertTrue(s.indexOf("blu") > 0);

            var s2:String = message.toString();
            Assert.assertEquals("Same", s, s2);
        }

        [Test]
        public function testToStringMessage5a():void {
            if (!isApacheFlex())
                return;
            var message:ErrorMessage = new ErrorMessage();
            message.faultCode = "Security.Failed";
            message.faultDetail = "Bla bla";
            message.faultString = "Blo blo";

            var s:String = message.toString();
            Assert.assertTrue(s.indexOf("Bla bla") > 0);
            Assert.assertTrue(s.indexOf("Blo blo") > 0);

            var s2:String = message.toString();
            Assert.assertEquals("Same", s, s2);
        }

        [Test]
        public function testToStringMessage5b():void {
            if (!isApacheFlex())
                return;
            var message:ErrorMessage = new ErrorMessage();
            message.faultCode = "Security.Failed";
            message.faultDetail = "Bla bla";
            message.faultString = "Blo blo";
            var person:Person = new Person();
            person.contacts = new PersistentSet(false);
            message.extendedData = {
                test: new TestObject(),
                test2: new TestObject2(),
                test3: person,
                test4: new PersistentSet(false)
            };

            var s:String = message.toString();
            Assert.assertTrue(s.indexOf(getQualifiedClassName(person.contacts)) > 0);
            Assert.assertFalse("Not initialized", message.extendedData.test4.isInitialized());
            Assert.assertTrue(s.indexOf("Bla bla") > 0);
            Assert.assertTrue(s.indexOf("Blo blo") > 0);
            Assert.assertTrue(s.indexOf("test2") > 0);

            var s2:String = message.toString();
            Assert.assertEquals("Same", s, s2);
        }
    }
}

import mx.utils.RPCObjectUtil;

class TestObject {

    public function toString():String {
        return "Bla";
    }
}


class TestObject2 {

    public var test:String = "blo";
    public var test2:Object;

    public function toString():String {
        return RPCObjectUtil.toString(this);
    }
}


class TestObject3 {

    public var test2:String = "blu";
}
