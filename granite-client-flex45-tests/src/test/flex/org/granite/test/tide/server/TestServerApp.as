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