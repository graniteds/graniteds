package org.granite.test.tide.framework {
	
	[Name("myComponentObserver3")]
	[Bindable]
    public class MyComponentObserver3 {
    	
		public var triggered1:Boolean = false;
				
		[Observer("org.test.myManagedEvent")]
		public function observe1(event:MyManagedEvent2):void {
			triggered1 = true;
		}
    }
}
