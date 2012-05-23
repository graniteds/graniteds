/*
  GRANITE DATA SERVICES
  Copyright (C) 2011 GRANITE DATA SERVICES S.A.S.

  This file is part of Granite Data Services.

  Granite Data Services is free software; you can redistribute it and/or modify
  it under the terms of the GNU Library General Public License as published by
  the Free Software Foundation; either version 2 of the License, or (at your
  option) any later version.

  Granite Data Services is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
  for more details.

  You should have received a copy of the GNU Library General Public License
  along with this library; if not, see <http://www.gnu.org/licenses/>.
*/

package org.granite.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.jar.JarEntry;
import java.util.regex.Pattern;

/**
 * @author Franck WOLFF
 */
public class URIUtil {

    public static final String CLASSPATH_SCHEME = "class";
    public static final String FILE_SCHEME = "file";
    public static final Pattern WINDOWS_DRIVE_PATTERN = Pattern.compile("^[a-zA-Z]\\:.*$");

    public static String normalize(String uri) {
    	if (uri == null)
    		return null;
    	uri = uri.replace('\\', '/').replace(" ", "%20");
    	while (uri.indexOf("//") != -1)
    		uri = uri.replace("//", "/");
    	return uri;
    }

    public static boolean isFileURI(String path) throws URISyntaxException {
    	return isFileURI(new URI(normalize(path)));
    }

    public static boolean isFileURI(URI uri) {
    	// scheme.length() == 1 -> assume windows drive letter.
    	return uri.getScheme() == null || uri.getScheme().length() <= 1 || FILE_SCHEME.equals(uri.getScheme());
    }
    
    public static boolean isAbsolute(String path) throws URISyntaxException {
    	return isAbsolute(new URI(normalize(path)));
    }
    
    public static boolean isAbsolute(URI uri) {
    	String schemeSpecificPart = uri.getSchemeSpecificPart();
    	if (schemeSpecificPart == null || schemeSpecificPart.length() == 0)
    		return uri.isAbsolute();
    	
    	String scheme = uri.getScheme();
    	if (scheme == null)
    		return schemeSpecificPart.charAt(0) == '/';
    	if (FILE_SCHEME.equals(scheme))
    		return schemeSpecificPart.charAt(0) == '/' || WINDOWS_DRIVE_PATTERN.matcher(schemeSpecificPart).matches();
    	return true;
    }
    
    public static String getSchemeSpecificPart(String path) throws URISyntaxException {
    	return getSchemeSpecificPart(new URI(normalize(path)));
    }
    
    public static String getSchemeSpecificPart(URI uri) {
    	if (uri.getScheme() != null && uri.getScheme().length() <= 1)
    		return uri.getScheme() + ":" + uri.getSchemeSpecificPart();
    	return uri.getSchemeSpecificPart();
    }
    
    public static InputStream getInputStream(URI uri, ClassLoader loader) throws IOException {
        InputStream is = null;

        String scheme = uri.getScheme();
        String path = getSchemeSpecificPart(uri);
        if (CLASSPATH_SCHEME.equals(scheme)) {
        	if (loader != null)
        		is = loader.getResourceAsStream(path);
        	if (is == null) {
        		is = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
	            if (is == null)
	                throw new IOException("Resource not found exception: " + uri);
        	}
        }
        else if (isFileURI(uri))
            is = new FileInputStream(path);
        else
            is = uri.toURL().openStream();

        return is;
    }

    public static InputStream getInputStream(URI uri) throws IOException {
        return getInputStream(uri, null);
    }

    public static String getContentAsString(URI uri) throws IOException {
        return getContentAsString(uri, Charset.defaultCharset());
    }
    public static String getContentAsString(URI uri, Charset charset) throws IOException {
        InputStream is = null;
        try {
            is = getInputStream(uri);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, charset));

            StringBuilder sb = new StringBuilder(1024);

            char[] chars = new char[256];
            int count = -1;
            while ((count = reader.read(chars)) != -1)
                sb.append(chars, 0, count);

            return sb.toString();
        } finally {
            if (is != null)
                is.close();
        }
    }

    public static byte[] getContentAsBytes(URI uri) throws IOException {
        InputStream is = null;
        try {
            is = new BufferedInputStream(getInputStream(uri));

            ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);

            int b = 0;
            while ((b = is.read()) != -1)
                baos.write(b);

            return baos.toByteArray();
        } finally {
            if (is != null)
                is.close();
        }
    }

    public static long lastModified(URI uri) throws IOException {
        if (uri == null)
            return -1L;

        String scheme = uri.getScheme();
        if (scheme == null || scheme.length() <= 1)
            return new File(uri).lastModified();
        return lastModified(uri.toURL());
    }

    public static long lastModified(URL url) throws IOException {
        long lastModified = -1L;

        if (url != null) {
            URLConnection connection = url.openConnection();
            if (connection instanceof JarURLConnection) {
                JarEntry entry = ((JarURLConnection)connection).getJarEntry();
                if (entry != null)
                    lastModified = entry.getTime();
            }
            if (lastModified == -1L)
                lastModified = connection.getLastModified();
        }

        return (lastModified == 0L ? -1L : lastModified);
    }
}
