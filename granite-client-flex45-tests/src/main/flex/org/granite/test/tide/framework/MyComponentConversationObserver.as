package org.granite.test.tide.framework {
	
	import org.flexunit.Assert;
	import org.granite.tide.events.TideContextEvent;
	

	[Bindable]
	[Name("myComponentObserver")]
    public class MyComponentConversationObserver {
    	
    	public var untypedEvent:int = 0;
    	public var typedEvent:int = 0;
		public var arg:Object = null;
    	
    	
    	[Observer]
    	public function myHandler(event:MyConversationEvent):void {
			Assert.assertEquals("Conversation event param", "toto", event.obj);
    		typedEvent++;
    	}
    	
    	[Observer("someEvent")]
    	public function myHandler2(event:TideContextEvent):void {
    		untypedEvent++;
    	}
		
		[Observer("someEvent2")]
		public function myHandler3(callback:Function):void {
			Assert.assertNotNull("Callback function passed to conversation", callback);
		}
		
		[Observer("someEvent3")]
		public function myHandler4(args:Array):void {
			Assert.assertEquals("Args array passed to conversation", 1, args.length);
			this.arg = args[0];
		}
    }
}
