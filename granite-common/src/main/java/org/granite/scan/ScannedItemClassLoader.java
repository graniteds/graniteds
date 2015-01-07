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
import java.net.URL;
import java.net.URLClassLoader;

/**
 * @author Franck WOLFF
 */
public class ScannedItemClassLoader extends URLClassLoader {

    public ScannedItemClassLoader(URLClassLoader parent) {
        this(new URL[0], parent);
    }

    public ScannedItemClassLoader(URL[] urls, URLClassLoader parent) {
        super(urls, parent);
    }

    public Class<?> loadClass(ScannedItem item) throws IOException, ClassFormatError {
        String name = item.getClassName();

        Class<?> clazz = findLoadedClass(name);
        if (clazz == null) {

            SecurityManager securityManager = System.getSecurityManager();
            if (securityManager != null) {
                int iLastDot = name.lastIndexOf('.');
                if (iLastDot != -1)
                    securityManager.checkPackageAccess(name.substring(0, iLastDot));
            }

            byte[] data = item.getContent();
            clazz = defineClass(name, data, 0, data.length);
        }
        return clazz;
    }
}

