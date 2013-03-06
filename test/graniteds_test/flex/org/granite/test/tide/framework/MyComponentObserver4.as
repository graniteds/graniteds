package org.granite.test.tide.framework {
	
	[Name("myComponentObserver4")]
	[Bindable]
    public class MyComponentObserver4 {
    	
		public var triggered1:Boolean = false;
				
		[Observer("myManagedEvent4")]
		public function observe1(event:MyManagedEvent):void {
			triggered1 = true;
		}
    }
}
