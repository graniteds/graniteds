package org.granite.test.validation
{
	[Bindable]
	public class Field {
		
		[Size(min="10", max="30")]
		public var subField1:String;
		
		[Size(min="10", max="30")]
		public var subField2:String;
		
		[Size(min="10", max="30")]
		public var subField3:String;
		
		[Size(min="10", max="30")]
		public var subField4:String;
	}
}