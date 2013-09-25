package org.granite.tide.seam {

    import mx.messaging.events.MessageEvent;
    import mx.rpc.remoting.mxml.RemoteObject;

    import org.granite.tide.Tide;

    import org.granite.tide.service.ServerSession;

    import mx.logging.ILogger;
    import mx.logging.Log;

    import mx.rpc.AsyncToken;

    import org.granite.tide.BaseContext;
    import org.granite.tide.IComponent;
    import org.granite.tide.ITideResponder;

    import org.granite.tide.rpc.TideOperation;


    public class SeamServerSession extends ServerSession {

        private static var log:ILogger = Log.getLogger("org.granite.tide.seam.SeamServerSession");


        public function SeamServerSession(destination:String):void {
            super(destination);
        }


        protected override function setupRemoteObjects():void {
            var op1:TideOperation = createOperation("invokeComponent");
            var op3:TideOperation = createOperation("validateObject");
            var op4:TideOperation = createOperation("logout");
            var op5:TideOperation = createOperation("resyncContext");
            _ro.operations = { invokeComponent: op1, validateObject: op3, logout: op4, resyncContext: op5 };

            var op2:TideOperation = createOperation("initializeObject");
            _roInitialize.operations = { initializeObject: op2 };
        }

        public override function createOperation(name:String, ro:RemoteObject = null):TideOperation {
            var op:TideOperation = new SeamOperation(this, ro);
            op.name = name;
            return op;
        }

        protected override function isLoggedIn():Boolean {
            return getContext().identity.loggedIn;
        }

        protected override function setLoggedIn(value:Boolean):void {
            getContext().identity.loggedIn = value;
        }


        public override function login(ctx:BaseContext, component:IComponent, username:String, password:String, responder:ITideResponder = null, charset:String = null):AsyncToken {
            super.login(ctx, component, username, password, responder, charset);

            return invokeComponent(ctx, component, "login", [], responder, true, loginSuccessHandler, loginFaultHandler);
        }

        public override function checkLoggedIn(ctx:BaseContext, component:IComponent, responder:ITideResponder = null):AsyncToken {
            return invokeComponent(ctx, component, "isLoggedIn", [], responder, true, isLoggedInSuccessHandler, null);
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
