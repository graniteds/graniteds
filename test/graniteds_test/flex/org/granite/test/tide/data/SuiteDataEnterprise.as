package org.granite.test.tide.data
{
	[Suite]
	[RunWith("org.flexunit.runners.Suite")]
	public class SuiteDataEnterprise
	{
		public var test1:org.granite.test.tide.data.TestChangeSetEntity;
		public var test2:org.granite.test.tide.data.TestChangeSetEntity2;
		public var test3:org.granite.test.tide.data.TestChangeSetEntity3;
		public var test4:org.granite.test.tide.data.TestChangeSetEntity4;
		public var test5:org.granite.test.tide.data.TestChangeSetEntityAssociation;		
		public var test5b:org.granite.test.tide.data.TestChangeSetEntityDeep;
		public var test5c:org.granite.test.tide.data.TestChangeSetEntityRemote;
        public var test6:org.granite.test.tide.data.TestApplyRemoteChange;
		public var test7:org.granite.test.tide.data.TestResetEntityColl;
	}
}