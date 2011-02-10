package org.granite.test.tide.framework
{
	import org.granite.tide.Tide;
	import org.granite.tide.ITideModule;
	
	
	public class MyModule2a implements ITideModule {
		
		public function init(tide:Tide):void {
			tide.addComponents([MyComponentModuleA1]);
		}
	}
}