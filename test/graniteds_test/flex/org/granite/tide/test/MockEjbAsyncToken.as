package org.granite.tide.test
{
    import flash.events.TimerEvent;
    import flash.utils.Timer;
    
    import mx.collections.ArrayCollection;
    import mx.messaging.messages.AcknowledgeMessage;
    import mx.messaging.messages.ErrorMessage;
    import mx.messaging.messages.IMessage;
    import mx.rpc.AsyncToken;
    import mx.rpc.Fault;
    import mx.rpc.IResponder;
    import mx.rpc.events.AbstractEvent;
    import mx.rpc.events.FaultEvent;
    import mx.rpc.events.ResultEvent;
    
    import org.granite.tide.invocation.ContextUpdate;
    import org.granite.tide.invocation.InvocationCall;
    import org.granite.tide.invocation.InvocationResult;
    
    
    public class MockEjbAsyncToken extends AsyncToken {
        
        private var _operation:String = null;
        private var _args:Array = null;
        private var _timer:Timer = new Timer(50, 1);
        private var _responders:Array = new Array();
        
        
        function MockEjbAsyncToken(message:IMessage) {
            super(message);
            _timer.addEventListener(TimerEvent.TIMER_COMPLETE, timerHandler);
        }
        
        public function send(operation:String, args:Array):void {
            _responders = new Array();
            _operation = operation;
            _args = args;
            _timer.reset();
            _timer.start();
        }
        
        public override function addResponder(responder:IResponder):void {
            _responders.push(responder);
        }
        
        protected function buildResponse(call:InvocationCall, componentName:String, op:String, params:Array):AbstractEvent {
            return null;
        }
        
        protected function buildInitializerResponse(call:InvocationCall, entity:Object, propertyName:String):AbstractEvent {
            return null;
        }
        
        
        private function timerHandler(event:TimerEvent):void {
            var re:AbstractEvent = null;
             
            if (_operation == "invokeComponent") { 
                var componentName:String = _args[0];
                var op:String = _args[2];
                var params:Array = _args[3];
                re = buildResponse(_args[4] as InvocationCall, componentName, op, params);
            }
            else if (_operation == "initializeObject") {
                var entity:Object = _args[0];
                var propertyName:String = _args[1];
                re = buildInitializerResponse(_args[2] as InvocationCall, entity, propertyName);
            }
            
            var resp:IResponder = null;
            if (re is FaultEvent) {
                for each (resp in _responders)
                    resp.fault(re as FaultEvent);
            }
            else if (re is ResultEvent) {
                for each (resp in _responders)
                    resp.result(re as ResultEvent);
            }
        }
        
        protected function buildFault(faultCode:String):FaultEvent {
            var emsg:ErrorMessage = new ErrorMessage();
            emsg.faultCode = faultCode;
            return new FaultEvent(FaultEvent.FAULT, false, true, null, this, emsg);
        }
        
        protected function buildResult(result:Object = null, results:Array = null):ResultEvent {
            var msg:AcknowledgeMessage = new AcknowledgeMessage();
            var res:InvocationResult = new InvocationResult();
            res.result = result;
            res.scope = 3;
            res.results = new ArrayCollection();
            if (results) {
                for each (var rs:Array in results) {
                    var r:String = rs[0] as String;
                    var v:Object = rs[1];
                    var idx:int = r.indexOf(".");
                    var u:ContextUpdate = null;
                    if (idx > 0)
                        u = new ContextUpdate(r.substring(0, idx), r.substring(idx+1), v);
                    else
                        u = new ContextUpdate(r, null, v);
                    if (rs.length > 2 && rs[2] == true)
                        u.scope = 2;
                    res.results.addItem(u);
                }
            }
            res.events = new ArrayCollection();
            res.messages = new ArrayCollection();
            return new ResultEvent(ResultEvent.RESULT, false, false, res, this, msg);
        }
    }
}