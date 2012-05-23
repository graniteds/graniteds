package org.granite.test.util;

import java.net.URI;

import org.granite.util.URIUtil;

import org.junit.Assert;
import org.junit.Test;


public class URIUtilTest {

	@Test
	public void testNormalize() throws Exception {
		Assert.assertEquals(null, URIUtil.normalize(null));
		Assert.assertEquals("", URIUtil.normalize(""));
		Assert.assertEquals("/path/to/file.ext", URIUtil.normalize("/path/to/file.ext"));
		Assert.assertEquals("/path/to/my/file.ext", URIUtil.normalize("//path///to////my/////file.ext"));
		Assert.assertEquals("file:/path/to/file.ext", URIUtil.normalize("file:/path/to/file.ext"));
		Assert.assertEquals("file:c:/path/to/file.ext", URIUtil.normalize("file:c:/path/to/file.ext"));
		Assert.assertEquals("file:/path/to/file.ext", URIUtil.normalize("file:///path/to/file.ext"));
		Assert.assertEquals("c:/path/to/file.ext", URIUtil.normalize("c:\\path\\to\\file.ext"));
		Assert.assertEquals("c:/path/to/file.ext", URIUtil.normalize("c:\\\\\\path\\to\\file.ext"));
		Assert.assertEquals("file:c:/path/to/file.ext", URIUtil.normalize("file:c:\\path\\to\\file.ext"));
		Assert.assertEquals("file:c:/path/to/file.ext", URIUtil.normalize("file:c:\\\\\\path\\to\\file.ext"));
	}

	@Test
	public void testIsFileURI() throws Exception {
		Assert.assertTrue(URIUtil.isFileURI(""));
		Assert.assertTrue(URIUtil.isFileURI("/path/to/file.ext"));
		Assert.assertTrue(URIUtil.isFileURI("file:/path/to/file.ext"));
		Assert.assertTrue(URIUtil.isFileURI("c:/path/to/file.ext"));
		Assert.assertTrue(URIUtil.isFileURI("file:c:/path/to/file.ext"));
		Assert.assertTrue(URIUtil.isFileURI("file:c:\\path\\to\\file.ext"));
		Assert.assertTrue(URIUtil.isFileURI("file:to/file.ext"));
		Assert.assertTrue(URIUtil.isFileURI("to/file.ext"));

		Assert.assertFalse(URIUtil.isFileURI("class:org/granite/template.gsp"));
		Assert.assertFalse(URIUtil.isFileURI("http://www.graniteds.org/index.html"));
	}

	@Test
	public void testGetSchemeSpecificPart() throws Exception {
		Assert.assertEquals("", URIUtil.getSchemeSpecificPart(""));
		Assert.assertEquals("/path/to/file.ext", URIUtil.getSchemeSpecificPart("/path/to/file.ext"));
		Assert.assertEquals("/path/to/my/file.ext", URIUtil.getSchemeSpecificPart("//path///to////my/////file.ext"));
		Assert.assertEquals("/path/to/file.ext", URIUtil.getSchemeSpecificPart("file:/path/to/file.ext"));
		Assert.assertEquals("c:/path/to/file.ext", URIUtil.getSchemeSpecificPart("file:c:/path/to/file.ext"));
		Assert.assertEquals("/path/to/file.ext", URIUtil.getSchemeSpecificPart("file:///path/to/file.ext"));
		Assert.assertEquals("c:/path/to/file.ext", URIUtil.getSchemeSpecificPart("c:\\path\\to\\file.ext"));
		Assert.assertEquals("c:/path/to/file.ext", URIUtil.getSchemeSpecificPart("c:\\\\\\path\\to\\file.ext"));
		Assert.assertEquals("c:/path/to/file.ext", URIUtil.getSchemeSpecificPart("file:c:\\path\\to\\file.ext"));
		Assert.assertEquals("c:/path/to/file.ext", URIUtil.getSchemeSpecificPart("file:c:\\\\\\path\\to\\file.ext"));
	}

	@Test
	public void testIsAbsolute() throws Exception {
		Assert.assertFalse(URIUtil.isAbsolute(""));
		Assert.assertFalse(URIUtil.isAbsolute("file:path/to/file.ext"));
		Assert.assertFalse(URIUtil.isAbsolute("file:path\\to\\file.ext"));
		Assert.assertFalse(URIUtil.isAbsolute("path/to/file.ext"));
		Assert.assertFalse(URIUtil.isAbsolute("path\\to\\file.ext"));

		Assert.assertTrue(URIUtil.isAbsolute("/path/to/file.ext"));
		Assert.assertTrue(URIUtil.isAbsolute("\\path\\to\\file.ext"));
		Assert.assertTrue(URIUtil.isAbsolute("file:/path/to/file.ext"));
		Assert.assertTrue(URIUtil.isAbsolute("file:\\path\\to\\file.ext"));
		Assert.assertTrue(URIUtil.isAbsolute("file:c:/path/to/file.ext"));
		Assert.assertTrue(URIUtil.isAbsolute("file:c:\\path\\to\\file.ext"));
		Assert.assertTrue(URIUtil.isAbsolute("file:c:path/to/file.ext"));
		Assert.assertTrue(URIUtil.isAbsolute("file:c:path\\to\\file.ext"));
		Assert.assertTrue(URIUtil.isAbsolute("c:path/to/file.ext"));
		Assert.assertTrue(URIUtil.isAbsolute("c:path\\to\\file.ext"));
		Assert.assertTrue(URIUtil.isAbsolute("c:/path/to/file.ext"));
		Assert.assertTrue(URIUtil.isAbsolute("c:/path\\to\\file.ext"));
	}
}
