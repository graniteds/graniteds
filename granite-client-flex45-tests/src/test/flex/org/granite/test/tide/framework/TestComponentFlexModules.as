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
import flash.system.ApplicationDomain;

import mx.events.FlexEvent;

import mx.events.ModuleEvent;
import mx.modules.IModuleInfo;

import mx.modules.ModuleLoader;
import mx.modules.ModuleManager;

import org.flexunit.Assert;
import org.flexunit.async.Async;
import org.fluint.uiImpersonation.UIImpersonator;
import org.granite.reflect.Type;

import org.granite.tide.BaseContext;
    import org.granite.tide.Tide;
    import org.granite.tide.IComponent;
    import org.granite.test.tide.Contact;
import org.granite.tide.spring.Spring;


public class TestComponentFlexModules
    {
        private var _ctx:BaseContext;
        
        
        [Before]
        public function setUp():void {
            Tide.resetInstance();
            _ctx = Spring.getInstance().getContext();
            UIImpersonator.removeAllChildren();
            for each (var ad:ApplicationDomain in Type.registeredDomains) {
                if (ad !== Type.systemDomain)
                    Type.unregisterDomain(ad);
            }
            var info:IModuleInfo = ModuleManager.getModule("module.swf");
            if (info.ready)
                info.unload();
            info = ModuleManager.getModule("module2.swf");
            if (info.ready)
                info.unload();
            Spring.getInstance().initApplication();
        }
        
        
        [Test(async="true")]
        [Ignore]
        public function testLoadFlexModule():void {
            var loader1:ModuleLoader = new ModuleLoader();
            loader1.url = "module.swf";
            loader1.applicationDomain = new ApplicationDomain(ApplicationDomain.currentDomain);
            loader1.addEventListener(ModuleEvent.READY, Async.asyncHandler(this, moduleReadyHandler, 1000));
            UIImpersonator.addChild(loader1);
        }

        private function moduleReadyHandler(event:ModuleEvent, pass:Object = null):void {
            var loader1:ModuleLoader = ModuleLoader(UIImpersonator.getChildAt(0));
            Tide.getInstance().addModule(loader1.child, loader1.applicationDomain);
            loader1.child.addEventListener(FlexEvent.CREATION_COMPLETE, Async.asyncHandler(this, moduleAddHandler, 1000));
        }

        private function moduleAddHandler(event:FlexEvent, pass:Object = null):void {
            Assert.assertNotNull("PersonService", event.target.personService);
        }


        [Test(async="true")]
        [Ignore]
        public function testLoadDifferentFlexModules():void {
            var loader1:ModuleLoader = new ModuleLoader();
            loader1.url = "module.swf";
            loader1.applicationDomain = new ApplicationDomain(ApplicationDomain.currentDomain);
            loader1.addEventListener(ModuleEvent.READY, Async.asyncHandler(this, module1ReadyHandler, 1000));
            UIImpersonator.addChild(loader1);
        }

        private function module1ReadyHandler(event:ModuleEvent, pass:Object = null):void {
            var loader1:ModuleLoader = ModuleLoader(UIImpersonator.getChildAt(0));
            Tide.getInstance().addModule(loader1.child, loader1.applicationDomain);
            loader1.child.addEventListener(FlexEvent.CREATION_COMPLETE, Async.asyncHandler(this, module1AddHandler, 1000));
        }

        private function module1AddHandler(event:FlexEvent, pass:Object = null):void {
            Assert.assertNotNull("PersonService", event.target.personService);

            var loader2:ModuleLoader = new ModuleLoader();
            loader2.url = "module2.swf";
            loader2.applicationDomain = new ApplicationDomain(ApplicationDomain.currentDomain);
            loader2.addEventListener(ModuleEvent.READY, Async.asyncHandler(this, module2ReadyHandler, 1000));
            UIImpersonator.addChild(loader2);
        }

        private function module2ReadyHandler(event:ModuleEvent, pass:Object = null):void {
            var loader2:ModuleLoader = ModuleLoader(UIImpersonator.getChildAt(1));
            Tide.getInstance().addModule(loader2.child, loader2.applicationDomain);
            loader2.child.addEventListener(FlexEvent.CREATION_COMPLETE, Async.asyncHandler(this, module2AddHandler, 1000));
        }

        private function module2AddHandler(event:FlexEvent, pass:Object = null):void {
            Assert.assertNotNull("PersonService", event.target.personService);
        }


        [Test(async="true")]
        [Ignore]
        public function testLoadSameFlexModules():void {
            var loaderA:ModuleLoader = new ModuleLoader();
            loaderA.url = "module.swf";
            loaderA.applicationDomain = new ApplicationDomain(ApplicationDomain.currentDomain);
            loaderA.addEventListener(ModuleEvent.READY, Async.asyncHandler(this, moduleAReadyHandler, 1000));
            UIImpersonator.addChild(loaderA);
        }

        private function moduleAReadyHandler(event:ModuleEvent, pass:Object = null):void {
            var loaderA:ModuleLoader = ModuleLoader(UIImpersonator.getChildAt(0));
            Tide.getInstance().addModule(loaderA.child, loaderA.applicationDomain);
            loaderA.child.addEventListener(FlexEvent.CREATION_COMPLETE, Async.asyncHandler(this, moduleAAddHandler, 1000));
        }

        private function moduleAAddHandler(event:FlexEvent, pass:Object = null):void {
            Assert.assertNotNull("PersonService", event.target.personService);

            var loaderB:ModuleLoader = new ModuleLoader();
            loaderB.url = "module.swf"; // Don't create a new app domain, already loaded
            loaderB.addEventListener(ModuleEvent.READY, Async.asyncHandler(this, moduleBReadyHandler, 1000));
            UIImpersonator.addChild(loaderB);
        }

        private function moduleBReadyHandler(event:ModuleEvent, pass:Object = null):void {
            var loaderB:ModuleLoader = ModuleLoader(UIImpersonator.getChildAt(1));
            Tide.getInstance().addModule(loaderB.child, loaderB.applicationDomain);
            loaderB.child.addEventListener(FlexEvent.CREATION_COMPLETE, Async.asyncHandler(this, moduleBAddHandler, 1000));
        }

        private function moduleBAddHandler(event:FlexEvent, pass:Object = null):void {
            Assert.assertNotNull("PersonService", event.target.personService);

            var loaderB:ModuleLoader = ModuleLoader(UIImpersonator.getChildAt(1));
            UIImpersonator.removeChildAt(0);

            Assert.assertNotNull("PersonService after remove", Object(loaderB.child).personService);
        }
    }
}
