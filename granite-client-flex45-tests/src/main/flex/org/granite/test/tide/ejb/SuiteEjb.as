package org.granite.test.tide.ejb
{
	import org.granite.test.tide.ejb.TestEjbClientPagedQuery;
	import org.granite.test.tide.ejb.TestEjbServerPagedQuery;
	
	[Suite]
	[RunWith("org.flexunit.runners.Suite")]
	public class SuiteEjb
	{
		public var test1:org.granite.test.tide.ejb.TestEjbClientPagedQuery;
		public var test2:org.granite.test.tide.ejb.TestEjbServerPagedQuery;
		
	}
}