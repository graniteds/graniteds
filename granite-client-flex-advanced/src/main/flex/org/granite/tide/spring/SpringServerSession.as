/*
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2015 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of the Granite Data Services Platform.
 *
 *                               ***
 *
 *   Community License: GPL 3.0
 *
 *   This file is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published
 *   by the Free Software Foundation, either version 3 of the License,
 *   or (at your option) any later version.
 *
 *   This file is distributed in the hope that it will be useful, but
 *   WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *                               ***
 *
 *   Available Commercial License: GraniteDS SLA 1.0
 *
 *   This is the appropriate option if you are creating proprietary
 *   applications and you are not prepared to distribute and share the
 *   source code of your application under the GPL v3 license.
 *
 *   Please visit http://www.granitedataservices.com/license for more
 *   details.
 */
package org.granite.tide.spring {

    import org.granite.tide.service.*;

    import mx.logging.ILogger;
    import mx.logging.Log;

    import mx.rpc.AbstractOperation;
    import mx.rpc.AsyncToken;

    import org.granite.tide.BaseContext;
    import org.granite.tide.IComponent;
    import org.granite.tide.ITideResponder;

    import org.granite.tide.IInvocationCall;
    import org.granite.tide.rpc.ComponentResponder;

    import org.granite.tide.rpc.TideOperation;


    public class SpringServerSession extends ServerSession {

        private static var log:ILogger = Log.getLogger("org.granite.tide.spring.SpringServerSession");


        public function SpringServerSession(contextRoot:String = null, secure:Boolean = false, serverName:String = null, serverPort:String = null, destination:String = "server"):void {
            super(contextRoot, secure, serverName, serverPort, destination);
        }


        protected override function setupRemoteObjects():void {
            var op1:TideOperation = createOperation("invokeComponent");
            var op3:TideOperation = createOperation("validateObject");
            var op4:TideOperation = createOperation("login");
            var op5:TideOperation = createOperation("logout");
            _ro.operations = { invokeComponent: op1, validateObject:op3, login: op4, logout: op5 };

            var op2:TideOperation = createOperation("initializeObject");
            _roInitialize.operations = { initializeObject: op2 };
        }


        protected override function isLoggedIn():Boolean {
            return getContext().identity.loggedIn;
        }

        protected override function setLoggedIn(value:Boolean):void {
            getContext().identity.loggedIn = value;
        }

        public override function checkLoggedIn(ctx:BaseContext, component:IComponent, responder:ITideResponder = null):AsyncToken {
            return invokeComponent(ctx, component, "isLoggedIn", [], responder, true, isLoggedInSuccessHandler, isLoggedInFaultHandler);
        }

        public override function isLoggedInSuccessHandler(sourceContext:BaseContext, sourceModulePrefix:String, data:Object, componentName:String = null, op:String = null, tideResponder:ITideResponder = null, componentResponder:ComponentResponder = null):void {
            var wasLoggedIn:Boolean = getContext().identity.loggedIn;

            getContext().identity.username = data.result.result as String;
            getContext().identity.loggedIn = data.result.result != null;

            if (!getContext().identity.loggedIn && wasLoggedIn)
                sessionExpired(sourceContext);

            super.isLoggedInSuccessHandler(sourceContext, sourceModulePrefix, data, componentName, op, tideResponder, componentResponder);
        }

        public override function isLoggedInFaultHandler(sourceContext:BaseContext, sourceModulePrefix:String, data:Object, componentName:String = null, op:String = null, tideResponder:ITideResponder = null, componentResponder:ComponentResponder = null):void {
            getContext().identity.username = null;
            getContext().identity.loggedIn = false;

            super.isLoggedInFaultHandler(sourceContext, sourceModulePrefix, data, componentName, op, tideResponder, componentResponder);
        }


        public override function login(ctx:BaseContext, component:IComponent, username:String, password:String, responder:ITideResponder = null, charset:String = null):AsyncToken {
            super.login(ctx, component, username, password, responder, charset);

            var operation:AbstractOperation = ro.getOperation("login");
            var call:IInvocationCall = ctx.meta_prepareCall(this, operation);
            var token:AsyncToken = operation.send(call);
            token.addResponder(new ComponentResponder(this, ctx, springLoginSuccessHandler, springLoginFaultHandler, component, null, null, operation, false, responder, username));

            return token;
        }

        public function springLoginSuccessHandler(sourceContext:BaseContext, sourceModulePrefix:String, data:Object, username:String, componentName:String = null, op:String = null, tideResponder:ITideResponder = null, componentResponder:ComponentResponder = null):void {
            getContext().identity.username = username;
            getContext().identity.loggedIn = true;

            super.loginSuccessHandler(sourceContext, sourceModulePrefix, data, componentName, op, tideResponder, componentResponder);
        }

        public function springLoginFaultHandler(sourceContext:BaseContext, sourceModulePrefix:String, data:Object, username:String, componentName:String = null, op:String = null, tideResponder:ITideResponder = null, componentResponder:ComponentResponder = null):void {
            getContext().identity.username = null;
            getContext().identity.loggedIn = false;

            super.loginFaultHandler(sourceContext, sourceModulePrefix, data, componentName, op, tideResponder, componentResponder);
        }
    }
}
