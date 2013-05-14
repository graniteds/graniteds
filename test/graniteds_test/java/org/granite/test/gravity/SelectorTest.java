/**
 * 
 */
package org.granite.test.gravity;

import org.granite.gravity.selector.GravityMessageSelector;
import org.junit.Assert;
import org.junit.Test;

import flex.messaging.messages.AsyncMessage;

public class SelectorTest {
	
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