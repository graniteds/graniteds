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

import org.granite.gravity.selector.GravityMessageSelector;
import org.junit.Assert;
import org.junit.Test;

import flex.messaging.messages.AsyncMessage;

public class TestSelector {
	
	@Test
	public void testSelector() {
		GravityMessageSelector selectorUser = new GravityMessageSelector("user = 'user'");
		AsyncMessage message = new AsyncMessage();
		message.setHeader("user", "test");
		
		Assert.assertFalse("Simple non matching selector", selectorUser.accept(message));
		
		message.setHeader("user", "user");
		Assert.assertTrue("Simple matching selector", selectorUser.accept(message));
		
		message.setHeader("user", "user");
		message.setHeader("client", "client");
		Assert.assertTrue("Simple matching selector", selectorUser.accept(message));
		
		GravityMessageSelector selectorClient = new GravityMessageSelector("client = 'client'");
		Assert.assertTrue("Simple matching selector", selectorClient.accept(message));
	}
	
	@Test
	public void testSelector2() {
		GravityMessageSelector selectorUser = new GravityMessageSelector("user = 'user' AND (test IS NULL OR test = 'test')");
		AsyncMessage message = new AsyncMessage();
		message.setHeader("user", "test");
		
		Assert.assertFalse("Simple non matching selector", selectorUser.accept(message));
		
		message.setHeader("user", "user");
		Assert.assertTrue("Simple matching selector", selectorUser.accept(message));
		
		message.setHeader("user", "user");
		message.setHeader("test", "bla");
		Assert.assertFalse("Double non matching selector", selectorUser.accept(message));

		message.setHeader("user", "user");
		message.setHeader("test", "test");
		Assert.assertTrue("Double matching selector", selectorUser.accept(message));
	}
}