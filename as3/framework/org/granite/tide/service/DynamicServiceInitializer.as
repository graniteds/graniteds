package	org.granite.tide.service
{
	import flash.events.Event;
	import flash.events.EventDispatcher;
	
	import mx.logging.ILogger;
	import mx.logging.Log;
	import mx.messaging.Channel;
	import mx.messaging.ChannelSet;
	import mx.messaging.channels.AMFChannel;
	import mx.messaging.channels.SecureAMFChannel;
	import mx.rpc.events.FaultEvent;
	import mx.rpc.events.ResultEvent;
	import mx.rpc.http.HTTPService;
	import mx.rpc.remoting.RemoteObject;
	
	import org.granite.gravity.Consumer;
	import org.granite.gravity.Producer;
	import org.granite.gravity.channels.GravityChannel;
	import org.granite.gravity.channels.SecureGravityChannel;
	import org.granite.tide.service.DefaultServiceInitializer;
	import org.granite.tide.service.IServiceInitializer;
	
	
	[Event(name="configured", type="flash.events.Event")] 
	[Event(name="error", type="flash.events.Event")]
	
	public class DynamicServiceInitializer extends EventDispatcher implements IServiceInitializer {
		
		private static var log:ILogger = Log.getLogger("org.granite.tide.service.DynamicServiceInitializer");
		
		public static const CONFIGURED:String = "configured";
		public static const ERROR:String = "error";
		
		private var _graniteChannelSet:ChannelSet;
		private var _gravityChannelSet:ChannelSet;
		
		private var _initialized:Boolean = false;
		private var _protocol:String = "http";
		private var _contextRoot:String = null;
		private var _serverName:String = null;
		private var _serverPort:String = "80";
		private var _graniteUrlMapping:String = "/graniteamf/amf.txt";		// .txt for stupid bug in IE8
		private var _gravityUrlMapping:String = "/gravityamf/amf.txt";
		
		
		/**
		 * 	Tide constructor used at component instantiation
		 *
		 * 	@param name component name
		 *  @param context current context
		 */
		public function DynamicServiceInitializer(configUrl:String = "config.xml") {
			super();
			var httpService:HTTPService = new HTTPService();
			httpService.url = configUrl;
			httpService.method = "GET";
			httpService.useProxy = false;
			httpService.resultFormat = "e4x";
			httpService.addEventListener(ResultEvent.RESULT, resultHandler);
			httpService.addEventListener(FaultEvent.FAULT, faultHandler);
			httpService.send();
		}
		
		private function resultHandler(event:ResultEvent):void {
			if (event.result.hasOwnProperty("protocol"))
				_protocol = event.result.protocol;
			_contextRoot = event.result["context-root"];
			_serverName = event.result["server-name"];
			if (event.result.hasOwnProperty("server-port"))
				_serverPort = event.result["server-port"];
			if (event.result.hasOwnProperty("granite-url-mapping"))
				_graniteUrlMapping = event.result["granite-url-mapping"];
			if (event.result.hasOwnProperty("gravity-url-mapping"))
				_gravityUrlMapping = event.result["gravity-url-mapping"];
			
			_initialized = true;
			dispatchEvent(new Event(CONFIGURED));
		}
		
		private function faultHandler(event:FaultEvent):void {
			_initialized = false;
			log.error("ServiceInitializer configuration error: " + event.formatToString("FaultEvent", "fault"));
			dispatchEvent(new Event(ERROR));
		}
		
		protected function get protocol():String {
			return _protocol;
		}
		
		protected function newAMFChannel(id:String, uri:String):Channel {
			if (_protocol == "https")
				return new SecureAMFChannel(id, uri);
			return new AMFChannel(id, uri);
		}
		
		protected function newGravityChannel(id:String, uri:String):Channel {
			if (_protocol == "https")
				return new SecureGravityChannel(id, uri);
			return new GravityChannel(id, uri);
		}
		
		private function get graniteChannelSet():ChannelSet {
			if (_graniteChannelSet == null) {
				_graniteChannelSet = new ChannelSet();
				_graniteChannelSet.addChannel(newAMFChannel("graniteamf", protocol + "://" + _serverName + ":" + _serverPort + _contextRoot + _graniteUrlMapping));
			}
			return _graniteChannelSet;
		}
		
		private function get gravityChannelSet():ChannelSet {
			if (_gravityChannelSet == null) {
				_gravityChannelSet = new ChannelSet();
				_gravityChannelSet.addChannel(newGravityChannel("gravityamf", protocol + "://" + _serverName + ":" + _serverPort + _contextRoot + _gravityUrlMapping));
			}
			return _gravityChannelSet;
		}
		
		public function initialize(service:Object):void {
			if (!_initialized)
				throw new Error("DynamicServiceInitializer not configured");
			
			if (service is RemoteObject)
				service.channelSet = graniteChannelSet;
			else if (service is Consumer || service is Producer)
				service.channelSet = gravityChannelSet;
		}
	}
}