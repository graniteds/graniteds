/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of Granite Data Services.
 *
 *   Granite Data Services is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU Library General Public License as published by
 *   the Free Software Foundation; either version 2 of the License, or (at your
 *   option) any later version.
 *
 *   Granite Data Services is distributed in the hope that it will be useful, but
 *   WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *   FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
 *   for more details.
 *
 *   You should have received a copy of the GNU Library General Public License
 *   along with this library; if not, see <http://www.gnu.org/licenses/>.
 */
package org.granite.test.util;

import org.granite.util.URIUtil;
import org.junit.Assert;
import org.junit.Test;


public class TestURIUtil {

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
