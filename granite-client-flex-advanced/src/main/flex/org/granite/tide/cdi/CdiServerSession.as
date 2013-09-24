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


        public function CdiServerSession(destination:String):void {
            super(destination);
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
            return _tide.getContext().byType(IIdentity).loggedIn;
        }

        protected override function setLoggedIn(value:Boolean):void {
            _tide.getContext().byType(IIdentity).loggedIn = value;
        }

        public override function checkLoggedIn(ctx:BaseContext, component:IComponent, responder:ITideResponder = null):AsyncToken {
            return invokeComponent(ctx, component, "isLoggedIn", [], responder, true, isLoggedInSuccessHandler, isLoggedInFaultHandler);
        }

        public override function isLoggedInSuccessHandler(sourceContext:BaseContext, sourceModulePrefix:String, data:Object, componentName:String = null, op:String = null, tideResponder:ITideResponder = null, componentResponder:ComponentResponder = null):void {
            if (componentName == Cdi.IDENTITY_NAME) {
                var wasLoggedIn:Boolean = _tide.getContext()[Cdi.IDENTITY_NAME].loggedIn;

                _tide.getContext()[Cdi.IDENTITY_NAME].username = data.result.result as String;
                _tide.getContext()[Cdi.IDENTITY_NAME].loggedIn = data.result.result != null;

                if (!_tide.getContext()[Cdi.IDENTITY_NAME].loggedIn && wasLoggedIn)
                    sessionExpired(sourceContext);
            }

            super.isLoggedInSuccessHandler(sourceContext, sourceModulePrefix, data, componentName, op, tideResponder, componentResponder);
        }

        public override function isLoggedInFaultHandler(sourceContext:BaseContext, sourceModulePrefix:String, data:Object, componentName:String = null, op:String = null, tideResponder:ITideResponder = null, componentResponder:ComponentResponder = null):void {
            if (componentName == Cdi.IDENTITY_NAME) {
                _tide.getContext()[Cdi.IDENTITY_NAME].username = null;
                _tide.getContext()[Cdi.IDENTITY_NAME].loggedIn = false;
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
            _tide.getContext()[Cdi.IDENTITY_NAME].username = username;
            _tide.getContext()[Cdi.IDENTITY_NAME].loggedIn = true;

            super.loginSuccessHandler(sourceContext, sourceModulePrefix, data, componentName, op, tideResponder, componentResponder);
        }

        public function cdiLoginFaultHandler(sourceContext:BaseContext, sourceModulePrefix:String, data:Object, username:String, componentName:String = null, op:String = null, tideResponder:ITideResponder = null, componentResponder:ComponentResponder = null):void {
            _tide.getContext()[Cdi.IDENTITY_NAME].username = null;
            _tide.getContext()[Cdi.IDENTITY_NAME].loggedIn = false;

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
