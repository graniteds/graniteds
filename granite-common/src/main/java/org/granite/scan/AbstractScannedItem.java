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
package org.granite.scan;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author Franck WOLFF
 */
public abstract class AbstractScannedItem implements ScannedItem {

    private final Scanner scanner;
    private final ScannedItem marker;

    private Class<?> clazz = null;
    private Properties properties = null;

    public AbstractScannedItem(Scanner scanner, ScannedItem marker) {
        this.scanner = scanner;
        this.marker = marker;
    }

    public ScannedItem getMarker() {
        return marker;
    }

    public Scanner getScanner() {
        return scanner;
    }

    public String getExtension() {
        String name = getName();
        int lastDot = name.indexOf('.');
        return lastDot >= 0 ? name.substring(lastDot + 1) : null;
    }

    public String getClassName() {
        if (!"class".equals(getExtension()))
            throw new RuntimeException("Not a valid class name: " + getAbsolutePath());
        return getRelativePath().substring(0, getRelativePath().length() - 6).replace('/', '.');
    }

    public byte[] getContent() throws IOException {
        long size = getSize();
        if (size > Integer.MAX_VALUE)
            throw new IOException("Size over Integer.MAX_VALUE: " + size);

        InputStream is = null;
        try {
            is = getInputStream();
            byte[] data = new byte[(int)size];
            is.read(data);
            return data;
        } finally {
            if (is != null)
                is.close();
        }
    }

    public Class<?> loadAsClass() throws ClassNotFoundException, IOException, ClassFormatError {
        if (clazz == null) {
            ClassLoader loader = scanner.getLoader();
            if (loader instanceof ScannedItemClassLoader)
                clazz = ((ScannedItemClassLoader)loader).loadClass(this);
            else
                clazz = loader.loadClass(getClassName());
        }
        return clazz;
    }

    public Properties loadAsProperties() throws IOException, IllegalArgumentException {
        if (properties == null) {
            InputStream is = null;
            try {
                is = getInputStream();
                properties = new Properties();
                properties.load(getInputStream());
            } finally {
                if (is != null)
                    is.close();
            }
        }
        return properties;
    }

    @Override
    public String toString() {
        if (marker != null)
            return getAbsolutePath() + " [marker=" + marker + "]";
        return getAbsolutePath();
    }
}
