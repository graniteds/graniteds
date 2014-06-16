/*
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2014 GRANITE DATA SERVICES S.A.S.
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
package org.granite.test.tide.server
{
	import org.granite.tide.service.IServerApp;
	import org.granite.tide.service.SimpleServerApp;
	
	import org.flexunit.Assert;
	import org.flexunit.async.Async;

	public class TestServerApp {
		
		[Test]
		public function testServerApp():void {
			var s:SimpleServerApp = new SimpleServerApp();
			s.parseUrl("http://localhost:8080/myapp");
			
			Assert.assertEquals("", s.contextRoot);
			Assert.assertEquals("localhost", s.serverName);
			Assert.assertEquals("8080", s.serverPort);
			Assert.assertFalse(s.secure);
			
			s = new SimpleServerApp();
			s.parseUrl("http://localhost:8080/myapp/myapp.swf");
			
			Assert.assertEquals("/myapp", s.contextRoot);
			Assert.assertEquals("localhost", s.serverName);
			Assert.assertEquals("8080", s.serverPort);
			Assert.assertFalse(s.secure);
		
			s = new SimpleServerApp();
			s.parseUrl("https://localhost:8080/myapp");
			
			Assert.assertEquals("", s.contextRoot);
			Assert.assertEquals("localhost", s.serverName);
			Assert.assertEquals("8080", s.serverPort);
			Assert.assertTrue(s.secure);
			
			s = new SimpleServerApp();
			s.parseUrl("https://localhost:8080/myapp/myapp.swf");
			
			Assert.assertEquals("/myapp", s.contextRoot);
			Assert.assertEquals("localhost", s.serverName);
			Assert.assertEquals("8080", s.serverPort);
			Assert.assertTrue(s.secure);
			
			s = new SimpleServerApp();
			s.parseUrl("http://localhost:8080/myapp.swf");
			
			Assert.assertEquals("", s.contextRoot);
			Assert.assertEquals("localhost", s.serverName);
			Assert.assertEquals("8080", s.serverPort);
			Assert.assertFalse(s.secure);

			s = new SimpleServerApp();
			s.parseUrl("http://bla.mydomain.com/myapp.swf");
			
			Assert.assertEquals("", s.contextRoot);
			Assert.assertEquals("bla.mydomain.com", s.serverName);
			Assert.assertEquals("80", s.serverPort);
			Assert.assertFalse(s.secure);
			
			s = new SimpleServerApp();
			s.parseUrl("https://bla.mydomain.com/myapp.swf");
			
			Assert.assertEquals("", s.contextRoot);
			Assert.assertEquals("bla.mydomain.com", s.serverName);
			Assert.assertEquals("443", s.serverPort);
			Assert.assertTrue(s.secure);
			
			s = new SimpleServerApp();
			s.parseUrl("https://bla.mydomain.com/blo/myapp.swf");
			
			Assert.assertEquals("/blo", s.contextRoot);
			Assert.assertEquals("bla.mydomain.com", s.serverName);
			Assert.assertEquals("443", s.serverPort);
			Assert.assertTrue(s.secure);
		}
	}
}