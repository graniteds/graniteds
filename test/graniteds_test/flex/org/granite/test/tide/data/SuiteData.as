package org.granite.test.tide.data
{
	import org.granite.test.tide.data.TestCollectionEnumGDS588;
	import org.granite.test.tide.data.TestDestroyContext;
	import org.granite.test.tide.data.TestDirtyCheckEntity;
	import org.granite.test.tide.data.TestDirtyCheckEntityBidir;
	import org.granite.test.tide.data.TestDirtyCheckEntityBigNumber;
	import org.granite.test.tide.data.TestDirtyCheckEntityEmbedded;
	import org.granite.test.tide.data.TestDirtyCheckEntityEnum;
	import org.granite.test.tide.data.TestDirtyCheckEntityGDS614;
	import org.granite.test.tide.data.TestDirtyCheckEntityLazy;
	import org.granite.test.tide.data.TestDirtyCheckNewEntity;
	import org.granite.test.tide.data.TestEntityRefs;
	import org.granite.test.tide.data.TestManagedEntity;
	import org.granite.test.tide.data.TestMergeCollection;
	import org.granite.test.tide.data.TestMergeCollection2;
	import org.granite.test.tide.data.TestMergeCollection3;
	import org.granite.test.tide.data.TestMergeCollection4;
	import org.granite.test.tide.data.TestMergeCollection5;
	import org.granite.test.tide.data.TestMergeCollectionOfElementsGDS501;
	import org.granite.test.tide.data.TestMergeCollectionOfEntities;
	import org.granite.test.tide.data.TestMergeCollectionSort;
	import org.granite.test.tide.data.TestMergeConflictEntity;
	import org.granite.test.tide.data.TestMergeConflictEntityCollection;
	import org.granite.test.tide.data.TestMergeConflictEntityConversation;
	import org.granite.test.tide.data.TestMergeContexts;
	import org.granite.test.tide.data.TestMergeDirtyEntity;
	import org.granite.test.tide.data.TestMergeEntityArray;
	import org.granite.test.tide.data.TestMergeEntityCollection;
	import org.granite.test.tide.data.TestMergeEntityConversation;
	import org.granite.test.tide.data.TestMergeEntityConversation2;
	import org.granite.test.tide.data.TestMergeEntityConversation3;
	import org.granite.test.tide.data.TestMergeEntityConversation4;
	import org.granite.test.tide.data.TestMergeEntityEmbedded;
	import org.granite.test.tide.data.TestMergeEntityMap;
	import org.granite.test.tide.data.TestMergeEntityXML;
	import org.granite.test.tide.data.TestMergeLazyEntity;
	import org.granite.test.tide.data.TestMergeLazyEntity2;
	import org.granite.test.tide.data.TestMergeMap;
	import org.granite.test.tide.data.TestMergeMap2;
	import org.granite.test.tide.data.TestMergeMap3;
	import org.granite.test.tide.data.TestResetEntityBigNumber;
	import org.granite.test.tide.data.TestResetEntityEnum;
	import org.granite.test.tide.data.TestResetEntityEnum2;
	import org.granite.test.tide.data.TestResetEntityGDS453;
	import org.granite.test.tide.data.TestResetEntityGDS667;
	import org.granite.test.tide.data.TestResetEntityGDS668;
	
	[Suite]
	[RunWith("org.flexunit.runners.Suite")]
	public class SuiteData
	{
		public var test1:org.granite.test.tide.data.TestCollectionEnumGDS588;
		public var test2:org.granite.test.tide.data.TestDestroyContext;
		public var test3:org.granite.test.tide.data.TestDirtyCheckEntity;
		public var test4:org.granite.test.tide.data.TestDirtyCheckEntityBigNumber;
		public var test5:org.granite.test.tide.data.TestDirtyCheckEntityEmbedded;
		public var test6:org.granite.test.tide.data.TestDirtyCheckEntityEnum;
		public var test7:org.granite.test.tide.data.TestDirtyCheckEntityGDS614;
		public var test7b:org.granite.test.tide.data.TestDirtyCheckNewEntity;
		public var test7c:org.granite.test.tide.data.TestDirtyCheckEntityBidir;
		public var test7d:org.granite.test.tide.data.TestDirtyCheckEntityLazy;
		public var test8:org.granite.test.tide.data.TestEntityRefs;
		public var test8b:org.granite.test.tide.data.TestManagedEntity;
		public var test9:org.granite.test.tide.data.TestMergeCollection;
		public var test10:org.granite.test.tide.data.TestMergeCollection2;
		public var test11:org.granite.test.tide.data.TestMergeCollection3;
		public var test12:org.granite.test.tide.data.TestMergeCollection4;
		public var test13:org.granite.test.tide.data.TestMergeCollection5;
		public var test13b:org.granite.test.tide.data.TestMergeCollectionSort;
		public var test14:org.granite.test.tide.data.TestMergeCollectionOfElementsGDS501;
		public var test15:org.granite.test.tide.data.TestMergeCollectionOfEntities;
		public var test16:org.granite.test.tide.data.TestMergeConflictEntity;
		public var test17:org.granite.test.tide.data.TestMergeConflictEntityConversation;
		public var test17b:org.granite.test.tide.data.TestMergeConflictEntityCollection;
		public var test18:org.granite.test.tide.data.TestMergeContexts;
		public var test19:org.granite.test.tide.data.TestMergeDirtyEntity;
		public var test20:org.granite.test.tide.data.TestMergeEntityArray;
		public var test21:org.granite.test.tide.data.TestMergeEntityCollection;
		public var test22:org.granite.test.tide.data.TestMergeEntityConversation;
		public var test23:org.granite.test.tide.data.TestMergeEntityConversation2;
		public var test24:org.granite.test.tide.data.TestMergeEntityConversation3;
		public var test25:org.granite.test.tide.data.TestMergeEntityConversation4;
		public var test26:org.granite.test.tide.data.TestMergeEntityEmbedded;
		public var test27:org.granite.test.tide.data.TestMergeEntityMap;
		public var test28:org.granite.test.tide.data.TestMergeEntityXML;
		public var test29:org.granite.test.tide.data.TestMergeLazyEntity;
		public var test30:org.granite.test.tide.data.TestMergeLazyEntity2;
		public var test31:org.granite.test.tide.data.TestMergeMap;
		public var test32:org.granite.test.tide.data.TestMergeMap2;
		public var test33:org.granite.test.tide.data.TestMergeMap3;
		public var test34:org.granite.test.tide.data.TestResetEntityBigNumber;
		public var test35:org.granite.test.tide.data.TestResetEntityEnum;
		public var test36:org.granite.test.tide.data.TestResetEntityEnum2;
		public var test37:org.granite.test.tide.data.TestResetEntityGDS453;
		public var test38:org.granite.test.tide.data.TestResetEntityGDS667;
		public var test39:org.granite.test.tide.data.TestResetEntityGDS668;
		
	}
}