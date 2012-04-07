package
{
	import mx.messaging.Channel;
	
	import org.granite.gravity.channels.WebSocketChannel;
	import org.granite.tide.service.DefaultServiceInitializer;
	
	public class WebSocketServiceInitializer extends DefaultServiceInitializer {
		
		protected var _embedded:Boolean = false;
		
		public function WebSocketServiceInitializer(contextRoot:String = "", graniteUrlMapping:String = null, gravityUrlMapping:String = null, secure:Boolean = false, embedded:Boolean = false) {
			super(contextRoot, graniteUrlMapping, gravityUrlMapping, secure);
			_embedded = embedded;
		}
		
		protected override function newGravityChannel(id:String, uri:String):Channel {
			var ws:String = _secure ? "wss" : "ws";
			if (_embedded)
				return new WebSocketChannel(id, ws + "://" + _serverName + ":" + _serverPort);
			return new WebSocketChannel(id, ws + "://" + _serverName + ":" + _serverPort + _contextRoot + gravityUrlMapping);
		}
	}
}