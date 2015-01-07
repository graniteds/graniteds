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
package org.granite.tide.cdi {

    import org.granite.tide.IIdentity;
    import org.granite.tide.service.*;

    import mx.logging.ILogger;
    import mx.logging.Log;
    import mx.messaging.events.MessageEvent;

    import mx.rpc.AbstractOperation;
    import mx.rpc.AsyncToken;

    import mx.rpc.remoting.mxml.RemoteObject;

    import org.granite.tide.BaseContext;
    import org.granite.tide.IComponent;
    import org.granite.tide.ITideResponder;

    import org.granite.tide.Tide;
    import org.granite.tide.IInvocationCall;
    import org.granite.tide.rpc.ComponentResponder;

    import org.granite.tide.rpc.TideOperation;


    public class CdiServerSession extends ServerSession {

        private static var log:ILogger = Log.getLogger("org.granite.tide.cdi.CdiServerSession");


        public function CdiServerSession(contextRoot:String = null, secure:Boolean = false, serverName:String = null, serverPort:String = null, destination:String = "server"):void {
            super(contextRoot, secure, serverName, serverPort, destination);
        }

        protected override function setupRemoteObjects():void {
            var op1:TideOperation = createOperation("invokeComponent");
            var op3:TideOperation = createOperation("validateObject");
            var op4:TideOperation = createOperation("login");
            var op5:TideOperation = createOperation("logout");
            var op6:TideOperation = createOperation("hasRole");
            var op7:TideOperation = createOperation("resyncContext");
            _ro.operations = { invokeComponent: op1, validateObject: op3, login: op4, logout: op5, hasRole: op6, resyncContext: op7 };

            var op2:TideOperation = createOperation("initializeObject");
            _roInitialize.operations = { initializeObject: op2 };
        }

        public override function createOperation(name:String, ro:RemoteObject = null):TideOperation {
            var op:TideOperation = new CdiOperation(this, ro);
            op.name = name;
            return op;
        }

        protected override function isLoggedIn():Boolean {
            return byType(IIdentity).loggedIn;
        }

        protected override function setLoggedIn(value:Boolean):void {
            byType(IIdentity).loggedIn = value;
        }

        public override function checkLoggedIn(ctx:BaseContext, component:IComponent, responder:ITideResponder = null):AsyncToken {
            return invokeComponent(ctx, component, "isLoggedIn", [], responder, true, isLoggedInSuccessHandler, isLoggedInFaultHandler);
        }

        public override function isLoggedInSuccessHandler(sourceContext:BaseContext, sourceModulePrefix:String, data:Object, componentName:String = null, op:String = null, tideResponder:ITideResponder = null, componentResponder:ComponentResponder = null):void {
            if (componentName == Cdi.IDENTITY_NAME) {
                var wasLoggedIn:Boolean = getContext()[Cdi.IDENTITY_NAME].loggedIn;

                Tide.getInstance().getContext()[Cdi.IDENTITY_NAME].username = data.result.result as String;
                getContext()[Cdi.IDENTITY_NAME].loggedIn = data.result.result != null;

                if (!getContext()[Cdi.IDENTITY_NAME].loggedIn && wasLoggedIn)
                    sessionExpired(sourceContext);
            }

            super.isLoggedInSuccessHandler(sourceContext, sourceModulePrefix, data, componentName, op, tideResponder, componentResponder);
        }

        public override function isLoggedInFaultHandler(sourceContext:BaseContext, sourceModulePrefix:String, data:Object, componentName:String = null, op:String = null, tideResponder:ITideResponder = null, componentResponder:ComponentResponder = null):void {
            if (componentName == Cdi.IDENTITY_NAME) {
                getContext()[Cdi.IDENTITY_NAME].username = null;
                getContext()[Cdi.IDENTITY_NAME].loggedIn = false;
            }

            super.isLoggedInFaultHandler(sourceContext, sourceModulePrefix, data, componentName, op, tideResponder, componentResponder);
        }

        public override function login(ctx:BaseContext, component:IComponent, username:String, password:String, responder:ITideResponder = null, charset:String = null):AsyncToken {
            super.login(ctx, component, username, password, responder, charset);

            if (component.meta_name == Cdi.IDENTITY_NAME) {
                var operation:AbstractOperation = ro.getOperation("login");
                var call:IInvocationCall = ctx.meta_prepareCall(this, operation);
                var token:AsyncToken = operation.send(call);
                token.addResponder(new ComponentResponder(this, ctx, cdiLoginSuccessHandler, cdiLoginFaultHandler, component, null, null, operation, false, responder, username));
                return token;
            }
            else
                return invokeComponent(ctx, component, "login", [], responder, true, loginSuccessHandler, loginFaultHandler);
        }

        public function cdiLoginSuccessHandler(sourceContext:BaseContext, sourceModulePrefix:String, data:Object, username:String, componentName:String = null, op:String = null, tideResponder:ITideResponder = null, componentResponder:ComponentResponder = null):void {
            getContext()[Cdi.IDENTITY_NAME].username = username;
            getContext()[Cdi.IDENTITY_NAME].loggedIn = true;

            super.loginSuccessHandler(sourceContext, sourceModulePrefix, data, componentName, op, tideResponder, componentResponder);
        }

        public function cdiLoginFaultHandler(sourceContext:BaseContext, sourceModulePrefix:String, data:Object, username:String, componentName:String = null, op:String = null, tideResponder:ITideResponder = null, componentResponder:ComponentResponder = null):void {
            getContext()[Cdi.IDENTITY_NAME].username = null;
            getContext()[Cdi.IDENTITY_NAME].loggedIn = false;

            super.loginFaultHandler(sourceContext, sourceModulePrefix, data, componentName, op, tideResponder, componentResponder);
        }


        protected override function extractContextId(event:MessageEvent, fromFault:Boolean = false):String {
            if (event.message.headers[Tide.IS_LONG_RUNNING_CONVERSATION_TAG]) // || fromFault ??
                return event.message.headers[Tide.CONVERSATION_TAG];

            return Tide.DEFAULT_CONTEXT;
        }
        protected override function wasConversationEnded(event:MessageEvent):Boolean {
            return event.message.headers[Tide.WAS_LONG_RUNNING_CONVERSATION_ENDED_TAG] != null;
        }
        protected override function wasConversationCreated(event:MessageEvent):Boolean {
            return event.message.headers[Tide.WAS_LONG_RUNNING_CONVERSATION_CREATED_TAG] != null;
        }
    }
}
