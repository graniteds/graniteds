package org.granite.test.tide.framework {

	import org.granite.tide.Context;
	import org.granite.test.tide.Contact;
	

	[Name("myComponentInject")]
	[Bindable]
    public class MyComponentInjectGDS676 {

		[In]
    	public var testVector:Vector.<Contact>;
		
		[In(create="true")]
		public var testVectorWild:Vector.<*>;
    }
}
