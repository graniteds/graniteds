package org.granite.tide.data.model {
		
	import flash.utils.IDataInput;
	import flash.utils.IDataOutput;
	import flash.utils.IExternalizable;
	
	import mx.collections.IList;
	
	
	[RemoteClass(alias="org.granite.tide.data.model.Page")]
	public class Page implements IExternalizable {
		
		public var firstResult:int;
		public var maxResults:int;
		public var resultCount:int;
		public var resultList:IList;
		
		
		public function Page():void {
		}
	
		public function writeExternal(out:IDataOutput):void {
			out.writeObject(firstResult);
			out.writeObject(maxResults);
			out.writeObject(resultCount);
			out.writeObject(resultList);
		}
	
		public function readExternal(input:IDataInput):void {
			firstResult = input.readObject() as int;
			maxResults = input.readObject() as int;
			resultCount = input.readObject() as int;
			resultList = input.readObject() as IList;
		}
	
	}
}