/*
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2015 GRANITE DATA SERVICES S.A.S.
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
    import org.flexunit.Assert;
    
    import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.tide.IComponent;
    import org.granite.test.tide.Contact;
    
    
    public class TestComponentModules
    {
        private var _ctx:BaseContext;
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Tide.getInstance().getContext();
            Tide.getInstance().initApplication();
            Tide.getInstance().addComponent("module1.myComponent1", MyComponentModule1);
            Tide.getInstance().addComponent("module1.myComponent1b", MyComponentModule1b);
            Tide.getInstance().addComponent("module2.myComponent2", MyComponentModule2);
            Tide.getInstance().addComponents([MyComponentModule3]);
        }
        
        
        [Test]
        public function testComponentModules():void {
        	_ctx.application.dispatchEvent(new MyEvent());
        	
        	Assert.assertTrue("Component module1 triggered", _ctx['module1.myComponent1'].triggered);
        	Assert.assertTrue("Component module2 triggered", _ctx['module2.myComponent2'].triggered);
        	Assert.assertTrue("Component module1 local triggered", _ctx['module1.myComponent1b'].localTriggered);
        	Assert.assertFalse("Component module2 not local triggered", _ctx['module2.myComponent2'].localTriggered);
        	
        	Assert.assertFalse("Component injections", _ctx['module1.myComponent1'].myVariable === _ctx['module2.myComponent2'].myVariable);
        	Assert.assertStrictlyEquals("Component module1 injection", _ctx['module1.myComponent1'].myVariable, _ctx['module1.myVariable']);
        	Assert.assertStrictlyEquals("Component module1 injection", _ctx['module1.myComponent1b'].myVariable, _ctx['module1.myVariable']);
        	Assert.assertStrictlyEquals("Component module2 injection", _ctx['module2.myComponent2'].myVariable, _ctx['module2.myVariable']);
        	
        	Assert.assertFalse("Component outjections", _ctx['module1.myComponent1'].outjectedVariable === _ctx['module2.myComponent2'].outjectedVariable);
        	Assert.assertStrictlyEquals("Component module1 outjection", _ctx['module1.myComponent1'].outjectedVariable, _ctx['module1.outjectedVariable']);
        	Assert.assertStrictlyEquals("Component module2 outjection", _ctx['module2.myComponent2'].outjectedVariable, _ctx['module2.outjectedVariable']);
        	
        	Assert.assertFalse("No global variable", Tide.getInstance().isComponent("myLocalVariable"));
        	Assert.assertFalse("Component global access", _ctx['myLocalVariable'] is Contact);
        	Assert.assertTrue("Component local access", _ctx['module1.myLocalVariable'] is Contact);
        	Assert.assertStrictlyEquals("Component module1 variable injection", _ctx['module1.myComponent1b'].myLocalVariable, _ctx['module1.myLocalVariable']);
        	
        	Assert.assertStrictlyEquals("Component global variable", _ctx['module1.myComponent1'].globalVariable, _ctx.globalVariable);
        	Assert.assertStrictlyEquals("Component global variable", _ctx['module2.myComponent2'].globalVariable, _ctx.globalVariable);
        	
        	_ctx['module3.myComponentModule3'];
        	Assert.assertNull("No local component proxy", _ctx['module3.myRemoteComponent']);
        	Assert.assertTrue("Component proxy", _ctx.myRemoteComponent is IComponent);
        }
    }
}
