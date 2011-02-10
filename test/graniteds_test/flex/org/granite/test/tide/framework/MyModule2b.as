package org.granite.test.tide.framework
{
	import org.granite.tide.Tide;
	import org.granite.tide.ITideModule;
	
	
	public class MyModule2b implements ITideModule {
		
		public function init(tide:Tide):void {
			tide.addComponents([MyComponentModuleA2]);
			tide.addComponent("module1.myComponentB", MyComponentModuleB);
			tide.addComponent("module2.myComponentB", MyComponentModuleB);
		}
	}
}