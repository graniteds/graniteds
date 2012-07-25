package org.granite.tide.data.model {
	
	import flash.utils.IDataInput;
	import flash.utils.IDataOutput;
	import flash.utils.IExternalizable;
	
	
	[RemoteClass(alias="org.granite.tide.data.model.PageInfo")]
	public class PageInfo implements IExternalizable {
		
		public var firstResult:int;
		public var maxResults:int;
		public var sortInfo:SortInfo;
		
		
		public function PageInfo(firstResult:int = 0, maxResults:int = 0, order:Array = null, desc:Array = null):void {
			this.firstResult = firstResult;
			this.maxResults = maxResults;
			if (order == null || desc == null)
				this.sortInfo = null;
			else
				this.sortInfo = new SortInfo(order, desc);
		}
		
	
		public function writeExternal(out:IDataOutput):void {
			out.writeObject(firstResult);
			out.writeObject(maxResults);
			out.writeObject(sortInfo != null ? sortInfo.order : null);
			out.writeObject(sortInfo != null ? sortInfo.desc : null);
		}
		
		public function readExternal(input:IDataInput):void {
			this.firstResult = input.readObject() as int;
			this.maxResults = input.readObject() as int;
			var order:Array = input.readObject() as Array;
			var desc:Array = input.readObject() as Array;
			if (order != null && desc != null)
				this.sortInfo = new SortInfo(order, desc);
			else
				this.sortInfo = null;
		}
	
	}
}