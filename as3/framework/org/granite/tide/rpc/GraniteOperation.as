package org.granite.tide.rpc {

	import flash.utils.ByteArray;
	import flash.utils.getTimer;

	import mx.core.mx_internal;
	import mx.messaging.messages.IMessage;
	import mx.messaging.messages.RemotingMessage;
	import mx.rpc.AbstractService;
	import mx.rpc.AsyncToken;
	import mx.rpc.remoting.Operation;

	use namespace mx_internal;

	public class GraniteOperation extends Operation {

		public static const ZIP_HEADER:String = "DEFLATE";

		public static const INFLATED_BYTES_LENGTH:String = "INFLATED_BYTES_LENGTH";

		public static const DEFAULT_ZIP_HEADER_VALUE:String = "TIME_SPENT=";

		public static const INFLATE_BODY_MIN_SIZE:int = 2048;

		public function GraniteOperation(remoteObject:AbstractService = null, name:String = null) {
			super(remoteObject, name);
		}

		override mx_internal function invoke(message:IMessage, token:AsyncToken = null):AsyncToken {
			var remotingMessage:RemotingMessage = message as RemotingMessage;
			var byteArrayBody:ByteArray = new ByteArray();
			byteArrayBody.writeObject(remotingMessage.body);

			var inflatedBytesLength:int = byteArrayBody.length;
			if (inflatedBytesLength > INFLATE_BODY_MIN_SIZE) {
				var start:int = getTimer();
				byteArrayBody.deflate();
				var end:int = getTimer();

				remotingMessage.headers[ZIP_HEADER] = DEFAULT_ZIP_HEADER_VALUE + (end - start).toString() + "ms";
				remotingMessage.headers[INFLATED_BYTES_LENGTH] = inflatedBytesLength;
				remotingMessage.body = byteArrayBody;
			}

			return super.invoke(remotingMessage, token);
		}

	}
}
