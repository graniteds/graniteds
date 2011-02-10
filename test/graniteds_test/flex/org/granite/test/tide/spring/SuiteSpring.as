package org.granite.test.tide.spring
{
	import org.granite.test.tide.spring.TestGrailsPagedQuery;
	import org.granite.test.tide.spring.TestSpringClientPagedQuery;
	import org.granite.test.tide.spring.TestSpringPagedQueryController;
	import org.granite.test.tide.spring.TestSpringRetryAfterFault;
	import org.granite.test.tide.spring.TestSpringServerPagedQuery;
	
	[Suite]
	[RunWith("org.flexunit.runners.Suite")]
	public class SuiteSpring
	{
		public var test1:org.granite.test.tide.spring.TestGrailsPagedQuery;
		public var test2:org.granite.test.tide.spring.TestSpringClientPagedQuery;
		public var test3:org.granite.test.tide.spring.TestSpringPagedQueryController;
		public var test4:org.granite.test.tide.spring.TestSpringRetryAfterFault;
		public var test5:org.granite.test.tide.spring.TestSpringServerPagedQuery;
		
	}
}