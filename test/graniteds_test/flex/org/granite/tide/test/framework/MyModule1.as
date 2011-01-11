package org.granite.tide.test.framework
{
	import org.granite.tide.Tide;
	import org.granite.tide.ITideModule;
	
	
	public class MyModule1 implements ITideModule {
		
		public function init(tide:Tide):void {
			tide.addComponents([MyComponentModuleA1, MyComponentModuleA2]);
			tide.addComponent("module1.myComponentB", MyComponentModuleB);
			tide.addComponent("module2.myComponentB", MyComponentModuleB);
		}
	}
}