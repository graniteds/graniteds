/*
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
package org.granite.test.tide.spring
{
	[Suite]
	[RunWith("org.flexunit.runners.Suite")]
	public class SuiteSpring
	{
		public var test1:org.granite.test.tide.spring.TestSpringFaultExtendedData;
		public var test2:org.granite.test.tide.spring.TestSpringFaultValidation;
		public var test3:org.granite.test.tide.spring.TestSpringPagedQueryRefresh;
		public var test4:org.granite.test.tide.spring.TestSpringExceptionHandler;
		public var test5:org.granite.test.tide.spring.TestSpringRetryAfterFault;
		public var test6:org.granite.test.tide.spring.TestSpringClientPagedQuery;
		public var test7:org.granite.test.tide.spring.TestSpringClientPagedQueryUI;
		public var test8:org.granite.test.tide.spring.TestSpringPagedQueryController;
		public var test9:org.granite.test.tide.spring.TestSpringServerPagedQuery;
		public var test10:org.granite.test.tide.spring.TestGrailsPagedQuery;
        public var test11:org.granite.test.tide.spring.TestSpringUninitPreprocessor;
		public var test12:org.granite.test.tide.spring.TestSpringComponentRestrict;
	}
}
