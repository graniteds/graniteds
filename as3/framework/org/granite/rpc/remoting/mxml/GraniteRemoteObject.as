package org.granite.rpc.remoting.mxml {

	import mx.core.mx_internal;
	import mx.rpc.AbstractOperation;
	import mx.rpc.remoting.RemoteObject;
        import org.granite.tide.rpc.GraniteOperation;

	use namespace mx_internal;

	public dynamic class GraniteRemoteObject extends RemoteObject {

		public function GraniteRemoteObject(destination:String) {
			super(destination);
		}

		override public function getOperation(name:String):AbstractOperation {
			var op:AbstractOperation = this.getOperationLegacy(name);
			if (op == null) {
				op = new GraniteOperation(this, name);
				_operations[name] = op;
				op.asyncRequest = asyncRequest;
			}
			return op;
		}

		protected function getOperationLegacy(name:String):AbstractOperation {
			var o:Object = _operations[name];
			var op:AbstractOperation = (o is AbstractOperation) ? AbstractOperation(o) : null;
			return op;
		}

	}
}
