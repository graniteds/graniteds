/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2015 GRANITE DATA SERVICES S.A.S.
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
package org.granite.test.gravity;

import org.granite.gravity.selector.TopicMatcher;
import org.junit.Assert;
import org.junit.Test;

public class TestTopicMatcher {

	@Test
	public void testMatch() {
		Assert.assertTrue("* : test", TopicMatcher.matchesTopic("*", "Test"));
		Assert.assertFalse("France/* : US", TopicMatcher.matchesTopic("France/*", "US"));
		Assert.assertTrue("US/* : US", TopicMatcher.matchesTopic("US/*", "US"));
		Assert.assertTrue("US/* : US/NYC", TopicMatcher.matchesTopic("US/*", "US/NYC"));
		Assert.assertTrue("US/** : US/NYC/Wall St", TopicMatcher.matchesTopic("US/**", "US/NYC/Wall St"));
		Assert.assertFalse("US/** : UK/London", TopicMatcher.matchesTopic("US/**", "UK/London"));
		Assert.assertFalse("US/* : US/NYC/Wall St", TopicMatcher.matchesTopic("US/*", "US/NYC/Wall St"));
	}
}
