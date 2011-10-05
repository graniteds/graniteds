package org.granite.test.tide.cdi
{
	import org.granite.test.tide.cdi.TestCdiInjectedCall;
	import org.granite.test.tide.cdi.TestCdiInjectedEntityCall;
	
	[Suite]
	[RunWith("org.flexunit.runners.Suite")]
	public class SuiteCdi
	{
		public var test1:org.granite.test.tide.cdi.TestCdiInjectedCall;
		public var test2:org.granite.test.tide.cdi.TestCdiInjectedEntityCall;
        public var test3:org.granite.test.tide.cdi.TestCdiRemoting;

	}
}