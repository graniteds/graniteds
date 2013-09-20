/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of the Granite Data Services Platform.
 *
 *   Granite Data Services is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Lesser General Public
 *   License as published by the Free Software Foundation; either
 *   version 2.1 of the License, or (at your option) any later version.
 *
 *   Granite Data Services is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 *   General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public
 *   License along with this library; if not, write to the Free Software
 *   Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301,
 *   USA, or see <http://www.gnu.org/licenses/>.
 */
package org.granite.test.tide.seam
{
	import org.granite.test.tide.seam.*;
	
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
		public var test30:org.granite.test.tide.seam.TestSeamInputValidator;
		public var test31:org.granite.test.tide.seam.TestSeamControlValidator;
		public var test32:org.granite.test.tide.seam.TestSeamEntityValidator;
		
	}
}