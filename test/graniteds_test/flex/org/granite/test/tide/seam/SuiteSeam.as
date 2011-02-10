package org.granite.test.tide.seam
{
	import org.granite.test.tide.seam.TestSeamClientConversation;
	import org.granite.test.tide.seam.TestSeamClientPagedQuery;
	import org.granite.test.tide.seam.TestSeamContextCleanAfterFault;
	import org.granite.test.tide.seam.TestSeamContextCleanAfterFault2;
	import org.granite.test.tide.seam.TestSeamContextUpdatedAfterFault;
	import org.granite.test.tide.seam.TestSeamConversationScope;
	import org.granite.test.tide.seam.TestSeamConversationScope2;
	import org.granite.test.tide.seam.TestSeamCreateConversation;
	import org.granite.test.tide.seam.TestSeamDestroyConversation;
	import org.granite.test.tide.seam.TestSeamDestroyConversationGDS719;
	import org.granite.test.tide.seam.TestSeamIdentityGDS746;
	import org.granite.test.tide.seam.TestSeamInjectedCall;
	import org.granite.test.tide.seam.TestSeamInjectedCallGDS508;
	import org.granite.test.tide.seam.TestSeamInjectedEntityCall;
	import org.granite.test.tide.seam.TestSeamMergeArray;
	import org.granite.test.tide.seam.TestSeamMergeCollection;
	import org.granite.test.tide.seam.TestSeamMergeCollectionAdd;
	import org.granite.test.tide.seam.TestSeamMergeCollectionChange;
	import org.granite.test.tide.seam.TestSeamMergeCollectionOut;
	import org.granite.test.tide.seam.TestSeamMergeCollectionUpdate;
	import org.granite.test.tide.seam.TestSeamMergeEntity;
	import org.granite.test.tide.seam.TestSeamMergeEntityColl;
	import org.granite.test.tide.seam.TestSeamMergeEntityLazy;
	import org.granite.test.tide.seam.TestSeamMergeEntityLazyRef;
	import org.granite.test.tide.seam.TestSeamMergeEntityLazyVersion;
	import org.granite.test.tide.seam.TestSeamOutjectedCall;
	import org.granite.test.tide.seam.TestSeamOutjectedEntityCall;
	import org.granite.test.tide.seam.TestSeamServerPagedQuery;
	import org.granite.test.tide.seam.TestSeamSimpleCall;
	
	[Suite]
	[RunWith("org.flexunit.runners.Suite")]
	public class SuiteSeam
	{
		public var test1:org.granite.test.tide.seam.TestSeamClientConversation;
		public var test2:org.granite.test.tide.seam.TestSeamClientPagedQuery;
		public var test3:org.granite.test.tide.seam.TestSeamContextCleanAfterFault;
		public var test4:org.granite.test.tide.seam.TestSeamContextCleanAfterFault2;
		public var test5:org.granite.test.tide.seam.TestSeamContextUpdatedAfterFault;
		public var test6:org.granite.test.tide.seam.TestSeamConversationScope;
		public var test7:org.granite.test.tide.seam.TestSeamConversationScope2;
		public var test8:org.granite.test.tide.seam.TestSeamCreateConversation;
		public var test9:org.granite.test.tide.seam.TestSeamDestroyConversation;
		public var test10:org.granite.test.tide.seam.TestSeamDestroyConversationGDS719;
		public var test11:org.granite.test.tide.seam.TestSeamIdentityGDS746;
		public var test12:org.granite.test.tide.seam.TestSeamInjectedCall;
		public var test13:org.granite.test.tide.seam.TestSeamInjectedCallGDS508;
		public var test14:org.granite.test.tide.seam.TestSeamInjectedEntityCall;
		public var test15:org.granite.test.tide.seam.TestSeamMergeArray;
		public var test16:org.granite.test.tide.seam.TestSeamMergeCollection;
		public var test17:org.granite.test.tide.seam.TestSeamMergeCollectionAdd;
		public var test18:org.granite.test.tide.seam.TestSeamMergeCollectionChange;
		public var test19:org.granite.test.tide.seam.TestSeamMergeCollectionOut;
		public var test20:org.granite.test.tide.seam.TestSeamMergeCollectionUpdate;
		public var test21:org.granite.test.tide.seam.TestSeamMergeEntity;
		public var test22:org.granite.test.tide.seam.TestSeamMergeEntityColl;
		public var test23:org.granite.test.tide.seam.TestSeamMergeEntityLazy;
		public var test24:org.granite.test.tide.seam.TestSeamMergeEntityLazyRef;
		public var test25:org.granite.test.tide.seam.TestSeamMergeEntityLazyVersion;
		public var test26:org.granite.test.tide.seam.TestSeamOutjectedCall;
		public var test27:org.granite.test.tide.seam.TestSeamOutjectedEntityCall;
		public var test28:org.granite.test.tide.seam.TestSeamServerPagedQuery;
		public var test29:org.granite.test.tide.seam.TestSeamSimpleCall;
		
	}
}