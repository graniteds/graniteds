package org.granite.tide.data.model {
	
	import flash.utils.IDataInput;
	import flash.utils.IDataOutput;
	import flash.utils.IExternalizable;
	
	
	[RemoteClass(alias="org.granite.tide.data.model.SortInfo")]
	public class SortInfo implements IExternalizable {
		
		public var order:Array;
		public var desc:Array;
		
		
		public function SortInfo(order:Array = null, desc:Array = null):void {
			this.order = order;
			this.desc = desc;
		}
		
		
		public function writeExternal(out:IDataOutput):void {
			out.writeObject(order);
			out.writeObject(desc);
		}
	
		public function readExternal(input:IDataInput):void {
			this.order = input.readObject() as Array;
			this.desc = input.readObject() as Array;
		}
	
	}
}