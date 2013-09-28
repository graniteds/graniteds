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


        public function SpringServerSession(contextRoot:String = "", secure:Boolean = false, serverName:String = "", serverPort:String = "", destination:String = "server"):void {
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
