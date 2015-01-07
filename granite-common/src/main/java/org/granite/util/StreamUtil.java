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
package org.granite.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Franck WOLFF
 */
public class StreamUtil {

	public static byte[] getResourceAsBytes(String path, ClassLoader loader) throws IOException {
		if (loader == null)
			loader = Thread.currentThread().getContextClassLoader();
        InputStream is = loader.getResourceAsStream(path);
        if (is == null)
            throw new FileNotFoundException("Resource not found: " + path);

        ByteArrayOutputStream baos = new ByteArrayOutputStream(512);
        try {
            int b = -1;
            while ((b = is.read()) != -1)
                baos.write(b);
        } finally {
            is.close();
        }
        return baos.toByteArray();
    }
	
    public static ByteArrayInputStream getResourceAsStream(String path, ClassLoader loader) throws IOException {
        return new ByteArrayInputStream(getResourceAsBytes(path, loader));
    }

    public static String getResourceAsString(String path, ClassLoader loader) throws IOException {
        return new String(getResourceAsBytes(path, loader));
    }

    public static String getStreamAsString(InputStream is) throws IOException {
        if (is == null)
            return null;

        ByteArrayOutputStream baos = new ByteArrayOutputStream(512);
        try {
            int b = -1;
            while ((b = is.read()) != -1)
                baos.write(b);
        } finally {
            is.close();
        }
        return new String(baos.toByteArray());
    }
}
