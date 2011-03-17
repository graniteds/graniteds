package org.granite.test.tide.framework {
	
	import org.granite.tide.events.TideContextEvent;
	
	[ManagedEvent(name="myManagedEvent")]

	[Name("myComponentObserver2")]
	[Bindable]
    public class MyComponentObserver2 {
    	
    	public function dispatch():void {
    		dispatchEvent(new MyManagedEvent());
    	}
		
		public var triggered1:Boolean = false;
				
		[Observer("myManagedEvent")]
		public function observe1(event:MyManagedEvent):void {
			triggered1 = true;
		}
		
		public var triggered2:Boolean = false;
		
		[Observer("myManagedEvent2")]
		public function observe2(event:MyManagedEvent):void {
			triggered2 = true;
		}
    }
}
