package org.granite.tide.service {

    import flash.events.Event;
    import flash.utils.Dictionary;

    import mx.controls.Alert;
    import mx.core.mx_internal;
    import mx.logging.ILogger;
    import mx.logging.Log;
    import mx.messaging.Channel;
    import mx.messaging.ChannelSet;
    import mx.messaging.ChannelSet;
    import mx.messaging.events.ChannelFaultEvent;
    import mx.messaging.events.MessageEvent;
    import mx.messaging.messages.ErrorMessage;
    import mx.messaging.messages.IMessage;
    import mx.rpc.AbstractOperation;
    import mx.rpc.AsyncToken;
    import mx.rpc.Fault;
    import mx.rpc.Responder;
    import mx.rpc.events.FaultEvent;
    import mx.rpc.events.ResultEvent;
    import mx.rpc.remoting.mxml.RemoteObject;

    import org.granite.gravity.Consumer;
    import org.granite.gravity.Producer;

    import org.granite.gravity.channels.SessionAware;
    import org.granite.persistence.PersistentMap;
    import org.granite.reflect.Type;
    import org.granite.tide.BaseContext;
    import org.granite.tide.IComponent;
    import org.granite.tide.IEntity;
    import org.granite.tide.IExceptionHandler;
    import org.granite.tide.IExpression;
    import org.granite.tide.IExtendedExceptionHandler;
    import org.granite.tide.IInvocationCall;
    import org.granite.tide.IInvocationResult;
    import org.granite.tide.IMessageInterceptor;
    import org.granite.tide.ITideMergeResponder;
    import org.granite.tide.ITideResponder;
    import org.granite.tide.Tide;
    import org.granite.tide.collections.PersistentCollection;
    import org.granite.tide.events.TideFaultEvent;
    import org.granite.tide.events.TidePluginEvent;
    import org.granite.tide.events.TideResultEvent;
    import org.granite.tide.rpc.ComponentResponder;
    import org.granite.tide.rpc.IInvocationInterceptor;
    import org.granite.tide.rpc.InitializerResponder;
    import org.granite.tide.rpc.TideOperation;
    import org.granite.tide.validators.ValidatorResponder;

    public class ServerSession {

        private static var log:ILogger = Log.getLogger("org.granite.tide.service.ServerSession");

        private var _server:IServer;

        private var _destination:String = null;

        private var _sessionId:String = null;
        private var _firstCall:Boolean = true;

        protected var _ro:RemoteObject = null;
        protected var _roInitialize:RemoteObject = null;
        protected var _rosByDestination:Dictionary = new Dictionary();
        protected var _channelSetsByType:Dictionary = new Dictionary();
        private var _defaultChannelBuilder:IChannelBuilder = new DefaultChannelBuilder();

        private var _initializing:Boolean = true;
        private var _logoutInProgress:Boolean = false;
        private var _waitForLogout:int = 0;

        private var _busy:Boolean = false;
        public var disconnected:Boolean = false;


        public function ServerSession(destination:String = "server", secure:Boolean = false, serverName:String = "", serverPort:String = "", contextRoot:String = ""):void {
            _server = new DefaultServer(secure, serverName, serverPort, contextRoot);
            _destination = destination;

            initServer();
        }
		
        public function set server(server:IServer):void {
            if (server == _server)
                return;
            _server = server;
            initServer();
        }

        private function initServer():void {
            _server.initialize();

            // Update channelSets
            for (var type:String in _channelSetsByType) {
                var channelSet:ChannelSet = _channelSetsByType[type];
                channelSet.removeChannel(channelSet.channels[0]);
                initChannelSet(type);
            }
        }


        /**
         *  RemoteObject destination used
         */
        public function get destination():String {
            return _destination;
        }

        protected function get ro():RemoteObject {
            if (_ro == null)
                initRemoteObjects();
            return _ro;
        }

        protected function get roInitialize():RemoteObject {
            if (_roInitialize == null)
                initRemoteObjects();
            return _roInitialize;
        }

        /**
         * 	@private
         *  Init RemoteObject
         */
        protected final function initRemoteObjects():void {
            _ro = createRemoteObject();
            _roInitialize = createRemoteObject();
            setupRemoteObjects();
        }

        protected function setupRemoteObjects():void {
        }


        protected function initChannelSet(type:String):ChannelSet {
            var channelSet:ChannelSet = _channelSetsByType[type];
            if (channelSet == null) {
                channelSet = new ChannelSet();
                _channelSetsByType[type] = channelSet;
            }
            var channelBuilders:Array = allByType(IChannelBuilder, true);
            var channel:Channel = null;
            for each (var channelBuilder:IChannelBuilder in channelBuilders) {
                channel = channelBuilder.build(type, _server);
                if (channel != null) {
                    channelSet.addChannel(channel);
                    return channelSet;
                }
            }

            channel = _defaultChannelBuilder.build(type, _server);
            if (channel != null) {
                channelSet.addChannel(channel);
                return channelSet;
            }

            throw new Error("Could not init ChannelSet of type " + type + ": no channel builder found")
        }


        /**
         * 	@private
         *  Create RemoteObject
         *
         * 	@param destination destination
         *  @param concurrency remote object concurrenty
         *  @return internal RemoteObject
         */
        protected function createRemoteObject(destination:String = null, concurrency:String = "multiple"):RemoteObject {
            var ro:RemoteObject = new RemoteObject(destination != null ? destination : _destination);
            ro.channelSet = initChannelSet(DefaultChannelBuilder.DEFAULT);
            ro.concurrency = concurrency;
            ro.makeObjectsBindable = true;
            return ro;
        }

        /**
         * 	@private
         *  Create Operation
         *
         *  @param name operation name
         *  @param ro remote object for the operation
         *
         *  @return internal operation
         */
        public function createOperation(name:String, ro:RemoteObject = null):TideOperation {
            var op:TideOperation = new TideOperation(this, ro);
            op.name = name;
            return op;
        }

        /**
         * 	Returns the internal RemoteObject used for a particular component name
         *
         * 	@param componentName component name
         *
         *  @return remote object instance
         */
        public function getRemoteObject(componentName:String):RemoteObject {
            if (ro != null)
                return ro;
            return _rosByDestination[componentName];
        }


        public function getConsumer(type:String, componentName:String):Consumer {
            var consumer:Consumer = new Consumer();
            consumer.channelSet = initChannelSet(type);
            consumer.destination = componentName;
            return consumer;
        }

        public function getProducer(type:String, componentName:String):Producer {
            var producer:Producer = new Producer();
            producer.channelSet = initChannelSet(type);
            producer.destination = componentName;
            return producer;
        }


        /**
         * 	@private
         * 	Current sessionId
         *
         * 	@return sessionId
         */
        public function get sessionId():String {
            return _sessionId;
        }

        /**
         * 	@private
         * 	Is it the first remote call ?
         *
         * 	@return is first call
         */
        public function get firstCall():Boolean {
            return _firstCall;
        }

        public function set busy(busy:Boolean):void {
            _busy = busy;
            Tide.getInstance().notifyBusy(busy);
        }

        /**
         * @param message
         * @param before
         */
        public function processMessageInterceptors(message:IMessage, before:Boolean):void {
            var interceptors:Array = allByType(IMessageInterceptor, true);
            for each (var interceptor:IMessageInterceptor in interceptors) {
                if (before)
                    interceptor.before(message);
                else
                    interceptor.after(message);
            }
        }


        /**
         * 	@private
         * 	Internal implementation of component invocation
         *
         *  @param componentResponder the component token responder for the operation
         * 	@param ctx current context
         *  @param component component proxy
         *  @param op remote operation
         *  @param args array of operation arguments
         *  @param responder Tide responder
         *  @param withContext send additional context with the call
         *
         *  @return token for the remote operation
         */
        private function internalInvokeComponent(componentResponder:ComponentResponder, withContext:Boolean):AsyncToken {
            var ctx:BaseContext = componentResponder.sourceContext;
            var component:IComponent = componentResponder.component;
            var op:String = componentResponder.op;
            var args:Array = componentResponder.args;

            var interceptors:Array = ctx.allByType(IInvocationInterceptor);
            if (interceptors != null) {
                for each (var interceptor:IInvocationInterceptor in interceptors)
                    interceptor.beforeInvocation(ctx, component, op, args, componentResponder);
            }

            ctx.meta_contextManager.destroyFinishedContexts();

            var token:AsyncToken = null;
            var operation:AbstractOperation = null;
            if (ro != null) {
                ro.showBusyCursor = Tide.showBusyCursor;
                operation = ro.getOperation("invokeComponent");
                var call:IInvocationCall = ctx.meta_prepareCall(this, operation, withContext);
                var alias:String = component != null ? Type.forInstance(component).alias : null;
                var componentClassName:String = alias ? alias : null;
                token = operation.send(component.meta_name, componentClassName, op, args, call);
            }
            else {
                var roCall:RemoteObject = _rosByDestination[component.meta_name];
                if (roCall == null) {
                    roCall = createRemoteObject(component.meta_name);
                    _rosByDestination[component.meta_name] = roCall;
                }
                roCall.showBusyCursor = Tide.showBusyCursor;
                var ops:Object = roCall.operations;
                operation = ops[op];
                if (operation == null) {
                    operation = createOperation(op, roCall);
                    ops[op] = operation;
                    operation.mx_internal::asyncRequest = roCall.mx_internal::asyncRequest;
                }

                operation.arguments = args;
                token = operation.send();
            }
            componentResponder.operation = operation;

            token.addResponder(componentResponder);

            _firstCall = false;

            checkWaitForLogout();

            return token;
        }

        /**
         * 	@private
         * 	Implementation of component invocation
         *
         * 	@param ctx current context
         *  @param component component proxy
         *  @param op remote operation
         *  @param args array of operation arguments
         *  @param responder Tide responder
         *  @param withContext send additional context with the call
         *  @param resultHandler additional result handler
         *  @param faultHandler additional fault handler
         *
         *  @return token for the remote operation
         */
        public function invokeComponent(ctx:BaseContext, component:IComponent, op:String, args:Array, responder:ITideResponder,
                                        withContext:Boolean = true, resultHandler:Function = null, faultHandler:Function = null):AsyncToken {
            log.debug("invokeComponent {0} > {1}.{2}", ctx.contextId, component.meta_name, op);

            var rh:Function = resultHandler != null ? resultHandler : result;
            var fh:Function = faultHandler != null ? faultHandler : fault;
            var componentResponder:ComponentResponder = new ComponentResponder(this, ctx, rh, fh, component, op, args, null, false, responder);

            return internalInvokeComponent(componentResponder, withContext);
        }

        /**
         * 	@private
         * 	Invoke again the same operation of a component (retry after fault for example)
         *
         * 	@param ctx current context
         *  @param component component proxy
         *  @param op remote operation
         *  @param args array of operation arguments
         *  @param responder Tide responder
         *  @param withContext send additional context with the call
         *  @param resultHandler additional result handler
         *  @param faultHandler additional fault handler
         *
         *  @return token for the remote operation
         */
        public function reinvokeComponent(componentResponder:ComponentResponder):AsyncToken {
            var ctx:BaseContext = componentResponder.sourceContext;
            var component:IComponent = componentResponder.component;
            var op:String = componentResponder.op;

            log.debug("reinvokeComponent {0} > {1}.{2}", ctx.contextId, component.meta_name, op);

            return internalInvokeComponent(componentResponder, true);
        }


        /**
         * 	@private
         * 	Implementation of logout
         *
         * 	@param ctx current context
         *  @param componentName component name of identity
         */
        public function logout(context:BaseContext, component:IComponent, expired:Boolean = false):void {
            // For late execution in case logout() is called during a result handler
            Tide.currentApplication().callLater(doLogout, [ context, expired ]);
        }

        private function doLogout(context:BaseContext, expired:Boolean = false):void {
            _logoutInProgress = true;
            _waitForLogout = 1;

            context.raiseEvent(Tide.LOGOUT);

            // If expired, tryLogout() will be called later by the global fault handler
            if (!expired)
                tryLogout();
        }


        /**
         * 	@private
         * 	Get the contextId from the server response, should be overriden by subclasses
         *
         * 	@param event the response message
         *  @param fromFault the message is a fault
         *
         *  @return contextId
         */
        protected function extractContextId(event:MessageEvent, fromFault:Boolean = false):String {
            return Tide.DEFAULT_CONTEXT;
        }
        /**
         * 	@private
         * 	Get the conversation status from the server response, should be overriden by subclasses
         *
         * 	@param event the response message
         *
         *  @return true if the conversation was created by the server
         */
        protected function wasConversationCreated(event:MessageEvent):Boolean {
            return false;
        }
        /**
         * 	@private
         * 	Get the conversation status from the server response, should be overriden by subclasses
         *
         * 	@param event the response message
         *
         *  @return true if the conversation was ended by the server
         */
        protected function wasConversationEnded(event:MessageEvent):Boolean {
            return false;
        }


        /**
         *	@private
         *  Get the context where the result/fault shall be processed
         *
         *  @param sourceContext context from where the call has been issued
         *  @param event response message
         *  @param fromFault is a fault
         */
        private function extractContext(sourceContext:BaseContext, event:MessageEvent, fromFault:Boolean = false):BaseContext {
            var sessionId:String = event.message.headers['org.granite.sessionId'];
            if (sessionId != _sessionId) {
                _sessionId = sessionId;
                // Update sessionId for all SessionAware channels
                for each (var channelSet:ChannelSet in _channelSetsByType) {
                    channelSet.channels.forEach(function(channel:Channel):void {
                        if (channel is SessionAware)
                            SessionAware(channel).sessionId = sessionId;
                    });
                }
            }

            processMessageInterceptors(event.message, false);

            var contextId:String = extractContextId(event, fromFault);
            var wasConvCreated:Boolean = wasConversationCreated(event);
            var wasConvEnded:Boolean = wasConversationEnded(event);

            var context:BaseContext = null;
            if (!sourceContext.meta_isGlobal() && contextId == Tide.DEFAULT_CONTEXT && wasConvEnded) {
                // The conversation of the source context was ended
                // Get results in the current conversation when finished
                context = sourceContext;
                context.meta_markAsFinished();
            }
            else if (!sourceContext.meta_isGlobal() && contextId == Tide.DEFAULT_CONTEXT && !sourceContext.meta_isContextIdFromServer) {
                // A call to a non conversational component was issued from a conversation context
                // Get results in the current conversation
                context = sourceContext;
            }
            else if (!sourceContext.meta_isGlobal() && contextId != Tide.DEFAULT_CONTEXT
                    && (sourceContext.contextId == null || (sourceContext.contextId != contextId && !wasConvCreated))) {
                // The conversationId has been updated by the server
                var previousContextId:String = sourceContext.contextId;
                context = sourceContext;
                context.meta_setContextId(contextId, true);
                context.meta_contextManager.updateContextId(previousContextId, context);
            }
            else {
                context = sourceContext.meta_tide.getContext(contextId);
                if (contextId != Tide.DEFAULT_CONTEXT)
                    context.meta_setContextId(contextId, true);
            }

            return context;
        }

        /**
         * 	@private
         * 	Implementation of result handler
         *
         * 	@param sourceContext source context of remote call
         *  @param data return object
         *  @param componentName component name
         *  @param op remote operation
         *  @param tideResponder Tide responder for the remote call
         */
        public function result(sourceContext:BaseContext, sourceModulePrefix:String, data:Object, componentName:String = null, operation:String = null, tideResponder:ITideResponder = null, componentResponder:ComponentResponder = null):void {
            var invocationResult:IInvocationResult = null;
            var result:Object = null;
            if (data is ResultEvent)
                result = ResultEvent(data).result;
            else if (data is MessageEvent)
                result = MessageEvent(data).message.body;

            if (result is IInvocationResult) {
                invocationResult = result as IInvocationResult;
                result = invocationResult.result;
            }

            var context:BaseContext = extractContext(sourceContext, MessageEvent(data));

            var saveModulePrefix:String = sourceContext.meta_tide.currentModulePrefix;
            try {
                sourceContext.meta_tide.currentModulePrefix = sourceModulePrefix;

                context.meta_setServerSession(this);
                context.meta_result(componentName, operation, invocationResult, result,
                        tideResponder is ITideMergeResponder ? ITideMergeResponder(tideResponder).mergeResultWith : null);
                if (invocationResult)
                    result = invocationResult.result;

                var handled:Boolean = false;
                if (tideResponder) {
                    var event:TideResultEvent = new TideResultEvent(TideResultEvent.RESULT, context, false, true, data.token, componentResponder, result);
                    tideResponder.result(event);
                    if (event.isDefaultPrevented())
                        handled = true;
                }
            }
            finally {
                sourceContext.meta_tide.currentModulePrefix = saveModulePrefix;
            }

            context.meta_clearCache();

            // Should be after event result handling and responder: previous could trigger other remote calls
            if (context.meta_finished)
                context.meta_scheduleDestroy();

            _initializing = false;

            if (!handled && !_logoutInProgress)
                context.raiseEvent(Tide.CONTEXT_RESULT, result);

            tryLogout();
        }

        /**
         * 	@private
         * 	Implementation of fault handler
         *
         * 	@param sourceContext source context of remote call
         *  @param info fault object
         *  @param componentName component name
         *  @param op remote operation
         *  @param tideResponder Tide responder for the remote call
         */
        public function fault(sourceContext:BaseContext, sourceModulePrefix:String, info:Object, componentName:String = null, operation:String = null, tideResponder:ITideResponder = null, componentResponder:ComponentResponder = null):void {
            log.error("fault {0}", info);
            var faultEvent:FaultEvent = info as FaultEvent;

            var context:BaseContext = extractContext(sourceContext, faultEvent, true);

            var emsg:ErrorMessage = faultEvent.message is ErrorMessage ? faultEvent.message as ErrorMessage : null;
            var m:ErrorMessage = emsg;
            var extendedData:Object = emsg ? emsg.extendedData : null;
            do {
                if (m && m.faultCode && m.faultCode.search("Server.Security.") == 0) {
                    emsg = m;
                    extendedData = emsg ? emsg.extendedData : null;
                    break;
                }
                if (m && (m.rootCause is FaultEvent || m.rootCause is ChannelFaultEvent))
                    m = m.rootCause.rootCause as ErrorMessage;
                else if (m)
                    m = m.rootCause as ErrorMessage;
            }
            while (m);

            var saveModulePrefix:String = sourceContext.meta_tide.currentModulePrefix;
            try {
                sourceContext.meta_tide.currentModulePrefix = sourceModulePrefix;

                context.meta_setServerSession(this);
                context.meta_fault(componentName, operation, emsg);

                var handled:Boolean = false;
                var fault:Fault = null;
                if (emsg != null && emsg !== faultEvent.message) {
                    fault = new Fault(emsg.faultCode, emsg.faultString, emsg.faultDetail);
                    fault.message = faultEvent.fault.message;
                    fault.rootCause = faultEvent.fault.rootCause;
                }
                else
                    fault = faultEvent.fault;
                var event:TideFaultEvent = new TideFaultEvent(TideFaultEvent.FAULT, context, false, true, info.token, componentResponder, fault, extendedData);
                if (tideResponder) {
                    tideResponder.fault(event);
                    if (event.isDefaultPrevented())
                        handled = true;
                }

                if (!handled) {
                    var _exceptionHandlers:Array = allByType(IExceptionHandler, true);
                    if (emsg != null) {
                        // Lookup for a suitable exception handler
                        for each (var handler:IExceptionHandler in _exceptionHandlers) {
                            if (handler.accepts(emsg)) {
                                if (handler is IExtendedExceptionHandler)
                                    IExtendedExceptionHandler(handler).handleEx(this, context, emsg, event);
                                else
                                    handler.handle(this, context, emsg);
                                handled = true;
                                break;
                            }
                        }
                        if (!handled)
                            log.error("Unhandled fault: " + emsg.faultCode + ": " + emsg.faultDetail);
                    }
                    else if (_exceptionHandlers.length > 0 && faultEvent.message is ErrorMessage) {
                        // Handle fault with default exception handler
                        if (_exceptionHandlers[0] is IExtendedExceptionHandler)
                            IExtendedExceptionHandler(_exceptionHandlers[0]).handleEx(this, context, faultEvent.message as ErrorMessage, event);
                        else
                            _exceptionHandlers[0].handle(context, faultEvent.message as ErrorMessage);
                    }
                    else {
                        log.error("Unknown fault: " + faultEvent.toString());
                        Alert.show("Unknown fault: " + faultEvent.toString());
                    }
                }
            }
            finally {
                sourceContext.meta_tide.currentModulePrefix = saveModulePrefix;
            }

            if (!handled && !_logoutInProgress)
                context.raiseEvent(Tide.CONTEXT_FAULT, info.message);

            tryLogout();
        }


        /**
         * 	Notify the framework that it should wait for a async operation before effectively logging out.
         *  Only if a logout has been requested.
         */
        public function checkWaitForLogout():void {
            if (_logoutInProgress)
                _waitForLogout++;
        }

        /**
         * 	Try logout. Should be called after all remote operations on a component are finished.
         *  The effective logout is done when all remote operations on all components have been notified as finished.
         */
        public function tryLogout():void {
            if (!_logoutInProgress)
                return;

            _waitForLogout--;
            if (_waitForLogout > 0)
                return;

            dispatchEvent(new TidePluginEvent(Tide.PLUGIN_LOGOUT, { serverSession: this }));

            if (ro.channelSet) {
                var asyncToken:AsyncToken = ro.channelSet.logout();	// Workaround described in BLZ-310
                asyncToken.addResponder(new Responder(logoutComplete, logoutFault));
                checkWaitForLogout();
            }
            else
                logoutComplete(null);
        }

        /**
         *	@private
         * 	Abtract method: check of user login status
         *
         *  @return true if logged in
         */
        protected function isLoggedIn():Boolean {
            throw new Error("Must be overriden");
        }

        /**
         *	@private
         * 	Abtract method: define user login status
         *
         *  @param value true if logged in
         */
        protected function setLoggedIn(value:Boolean):void {
            throw new Error("Must be overriden");
        }

        /**
         * 	@private
         *
         * 	Handler method for logout complete
         */
        private function logoutComplete(event:Event, logoutRemoteObject:Boolean = true):void {
            if (logoutRemoteObject)
                ro.logout();

            log.info("Tide application logout");

            getContext().meta_contextManager.destroyContexts();

            _logoutInProgress = false;
            _waitForLogout = 0;

            getContext().raiseEvent(Tide.LOGGED_OUT);
        }

        private function logoutFault(event:Event):void {
            log.warn("Channel logout failed, assume the client is logged out");

            logoutComplete(event, false);
        }

        /**
         * 	@private
         * 	Implementation of login
         *
         * 	@param ctx current context
         *  @param componentName component name of identity
         *  @param username user name
         *  @param password password
         *  @param responder Tide responder
         *
         *  @return token for the remote operation
         */
        public function login(ctx:BaseContext, component:IComponent, username:String, password:String, responder:ITideResponder = null, charset:String = null):AsyncToken {
            log.info("login {0} > {1}", component.meta_name, username);

            _firstCall = false;
            Tide.getInstance().forceNewListeners();

            ro.setCredentials(username, password, charset);
            dispatchEvent(new TidePluginEvent(Tide.PLUGIN_SET_CREDENTIALS, { serverSession: this, username: username, password: password }));
            return null;
        }


        /**
         * 	@private
         * 	Implementation of login check
         *
         * 	@param ctx current context
         *  @param componentName component name of identity
         *  @param responder Tide responder
         *
         *  @return token for the remote operation
         */
        public function checkLoggedIn(ctx:BaseContext, component:IComponent, responder:ITideResponder = null):AsyncToken {
            return null;
        }


        /**
         * 	@private
         * 	Implementation of login success handler
         *
         * 	@param sourceContext source context of remote call
         *  @param sourceModulePrefix source module prefix
         *  @param data return object
         *  @param componentName component name
         *  @param op remote operation
         *  @param tideResponder Tide responder for the remote call
         */
        public function loginSuccessHandler(sourceContext:BaseContext, sourceModulePrefix:String, data:Object, componentName:String = null, op:String = null, tideResponder:ITideResponder = null, componentResponder:ComponentResponder = null):void {
            result(sourceContext, sourceModulePrefix, data, componentName, op, tideResponder, componentResponder);

            if (isLoggedIn()) {
                // Force reinitialization of all application at login
                Tide.currentApplication().executeBindings(true);

                initAfterLogin(sourceContext);
            }
            else {
                // Not logged in : consider as a login fault
                dispatchEvent(new TidePluginEvent(Tide.PLUGIN_LOGOUT, { serverSession: this }));
                ro.logout();

                dispatchEvent(new TidePluginEvent(Tide.PLUGIN_LOGIN_FAULT, { serverSession: this, sessionId: _sessionId }));
            }

            _initializing = false;
        }

        /**
         * 	@private
         * 	Implementation of is logged in success handler
         *
         * 	@param sourceContext source context of remote call
         *  @param data return object
         *  @param componentName component name
         *  @param op remote operation
         *  @param tideResponder Tide responder for the remote call
         */
        public function isLoggedInSuccessHandler(sourceContext:BaseContext, sourceModulePrefix:String, data:Object, componentName:String = null, op:String = null, tideResponder:ITideResponder = null, componentResponder:ComponentResponder = null):void {
            result(sourceContext, sourceModulePrefix, data, componentName, op, tideResponder, componentResponder);

            if (isLoggedIn())
                initAfterLogin(sourceContext);

            _initializing = false;
        }

        /**
         * 	@private
         * 	Called when user is already logged in at application startup
         *
         * 	@param sourceContext source context of remote call
         */
        public function initAfterLogin(sourceContext:BaseContext):void {
            dispatchEvent(new TidePluginEvent(Tide.PLUGIN_LOGIN_SUCCESS, { serverSession: this, sessionId: _sessionId }));

            sourceContext.raiseEvent(Tide.LOGIN);
        }

        /**
         *
         * @private
         *
         * Called when session expiration has been detected
         *
         * 	@param sourceContext source context of remote call
         */
        public function sessionExpired(sourceContext:BaseContext):void {
            log.info("Application session expired");

            _sessionId = null;
            _firstCall = true;

            _logoutInProgress = false;
            _waitForLogout = 0;

            sourceContext.raiseEvent(Tide.SESSION_EXPIRED);
            logout(sourceContext, null, true);
        }


        /**
         * 	@private
         * 	Implementation of login fault handler
         *
         * 	@param sourceContext source context of remote call
         *  @param data return object
         *  @param componentName component name
         *  @param op remote operation
         *  @param tideResponder Tide responder for the remote call
         */
        public function loginFaultHandler(sourceContext:BaseContext, sourceModulePrefix:String, info:Object, componentName:String = null, op:String = null, tideResponder:ITideResponder = null, componentResponder:ComponentResponder = null):void {
            fault(sourceContext, sourceModulePrefix, info, componentName, op, tideResponder, componentResponder);

            dispatchEvent(new TidePluginEvent(Tide.PLUGIN_LOGOUT, { serverSession: ServerSession }));
            ro.logout();

            dispatchEvent(new TidePluginEvent(Tide.PLUGIN_LOGIN_FAULT, { serverSession: ServerSession, sessionId: _sessionId }));
        }

        /**
         * 	@private
         * 	Implementation of is logged in success handler
         *
         * 	@param sourceContext source context of remote call
         *  @param data return object
         *  @param componentName component name
         *  @param op remote operation
         *  @param tideResponder Tide responder for the remote call
         */
        public function isLoggedInFaultHandler(sourceContext:BaseContext, sourceModulePrefix:String, data:Object, componentName:String = null, op:String = null, tideResponder:ITideResponder = null, componentResponder:ComponentResponder = null):void {
            if (data is FaultEvent && data.fault is Fault && data.fault.faultCode == "Server.Security.NotLoggedIn")
                result(sourceContext, sourceModulePrefix, data, componentName, op, tideResponder, componentResponder);
            else
                fault(sourceContext, sourceModulePrefix, data, componentName, op, tideResponder, componentResponder);
        }


        /**
         * 	@private
         * 	Implementation of context resync
         *
         * 	@param ctx current context
         *  @param responder Tide responder
         *
         *  @return token for the remote operation
         */
        public function resyncContext(ctx:BaseContext, responder:ITideResponder):AsyncToken {
            log.debug("resyncContext {0}", ctx.contextId);

            ctx.meta_contextManager.destroyFinishedContexts();

            var operation:AbstractOperation = ro.getOperation("resyncContext");
            var call:IInvocationCall = ctx.meta_prepareCall(this, operation, true);
            var token:AsyncToken = operation.send(call);
            token.addResponder(new ComponentResponder(this, ctx, result, fault, null, null, null, operation, true, responder));

            _firstCall = false;

            if (_logoutInProgress)
                _waitForLogout++;

            return token;
        }

        /**
         * 	@private
         * 	Implementation of lazy initialization
         *
         * 	@param ctx current context
         *  @param obj object to initialize (should be a PersistentCollection or PersistentMap)
         *  @param path path of the object in the context
         *
         *  @return token for the remote operation
         */
        public function initializeObject(ctx:BaseContext, obj:Object, path:IExpression):void {
            log.debug("initializeObject {0} > {1}", ctx.contextId, obj);

            // For now, assumes that obj is a PersistentCollection
            if (!(obj is PersistentCollection || obj is PersistentMap))
                throw new Error("Auto initialization works only with PersistentCollection/PersistentMap " + BaseContext.toString(obj));

            var entity:Object = obj.entity;

            _objectsInitializing.push({ context: ctx, entity: path ? path.path : obj.entity, propertyName: obj.propertyName });

            Tide.currentApplication().callLater(doInitializeObjects, [ctx]);
        }

        private function doInitializeObjects(ctx:BaseContext):void {

            var initMap:Dictionary = new Dictionary();

            for (var i:int = 0; i < _objectsInitializing.length; i++) {
                if (_objectsInitializing[i].context != ctx)
                    continue;

                var propertyNames:Array = initMap[_objectsInitializing[i].entity];
                if (propertyNames == null) {
                    propertyNames = [ _objectsInitializing[i].propertyName ];
                    initMap[_objectsInitializing[i].entity] = propertyNames;
                }
                else
                    propertyNames.push(_objectsInitializing[i].propertyName);

                _objectsInitializing.splice(i, 1);
                i--;
            }

            for (var entity:Object in initMap) {
                var operation:AbstractOperation = roInitialize.getOperation("initializeObject");
                var call:IInvocationCall = ctx.meta_prepareCall(this, operation, false);
                var token:AsyncToken = operation.send(entity, initMap[entity], call);
                token.addResponder(new InitializerResponder(ctx, initializerResult, initializerFault, entity, initMap[entity]));

                if (_logoutInProgress)
                    _waitForLogout++;
            }
        }

        private var _objectsInitializing:Array = new Array();

        /**
         * 	@private
         * 	Implementation of remote validation
         *
         * 	@param ctx current context
         *  @param entity object to validate
         *  @param propertyName property to validate
         *  @param value value to validate
         *
         *  @return token for the remote operation
         */
        public function validateObject(ctx:BaseContext, entity:IEntity, propertyName:String, value:Object):AsyncToken {
            log.debug("validateObject {0} > {1}", ctx.contextId, entity);

            var operation:AbstractOperation = ro.getOperation("validateObject");
            var call:IInvocationCall = ctx.meta_prepareCall(this, operation, false);
            // For now, assumes that obj is a PeristentCollection
            var token:AsyncToken = operation.send(entity, propertyName, value, call);

            token.addResponder(new ValidatorResponder(ctx, entity, propertyName));

            if (_logoutInProgress)
                _waitForLogout++;

            return token;
        }

        /**
         * 	@private
         * 	Implementation of initializer success handler
         *
         * 	@param sourceContext source context of remote call
         *  @param data return object
         *  @param entity object to initialize
         *  @param propertyNames array of property names to initialize
         */
        public function initializerResult(sourceContext:BaseContext, data:Object, entity:Object, propertyNames:Array):void {
            var res:Array = data.result.result as Array;

            var saveUninitializeAllowed:Boolean = sourceContext.meta_uninitializeAllowed;
            try {
                sourceContext.meta_uninitializeAllowed = false;

                // Assumes objects is a PersistentCollection or PersistentMap
                sourceContext.meta_mergeExternal(data.result.result, entity);

                result(sourceContext, "", data);
            }
            finally {
                sourceContext.meta_uninitializeAllowed = saveUninitializeAllowed;
            }
        }

        /**
         * 	@private
         * 	Implementation of initializer fault handler
         *
         * 	@param sourceContext source context of remote call
         *  @param data return object
         *  @param entity object to initialize
         *  @param propertyNames array of property names to initialize
         */
        public function initializerFault(sourceContext:BaseContext, info:Object, entity:Object, propertyNames:Array):void {
            log.error("Fault initializing collection " + BaseContext.toString(entity) + " " + info.toString());

            fault(sourceContext, "", info);
        }


        protected function getContext():BaseContext {
            return Tide.getInstance().getContext();
        }

        protected function byType(serviceClass:Class, create:Boolean = false):Object {
            return getContext().byType(serviceClass, create);
        }

        protected function allByType(serviceClass:Class, create:Boolean = false):Array {
            return getContext().allByType(serviceClass, create);
        }

        protected function dispatchEvent(event:Event):Boolean {
            return Tide.getInstance().dispatchEvent(event);
        }
    }
}
