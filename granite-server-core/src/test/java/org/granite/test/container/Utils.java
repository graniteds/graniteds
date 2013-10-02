/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
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
package org.granite.test.container;

import java.io.File;
import java.io.FileFilter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;

/**
 * Created by william on 01/10/13.
 */
public class Utils {

    public static File findJarContainingResource(String resource) {
        ClassLoader loader = Utils.class.getClassLoader();
        URL url = loader.getResource(resource);
        try {
            String urlPath = URLDecoder.decode(url.getFile(), "UTF-8");
            if (urlPath.indexOf('!') > 0)
                urlPath = urlPath.substring(0, urlPath.indexOf('!'));
            if (urlPath.startsWith("file:"))
                urlPath = urlPath.substring("file:".length());
            return new File(urlPath);
        }
        catch (UnsupportedEncodingException e) {
            // Cannot happen, UTF-8 always supported
            throw new RuntimeException("Could not find jar for resource " + resource, e);
        }
    }

    public static class ArtifactFileFilter implements FileFilter {
        @Override
        public boolean accept(File file) {
            return !file.getName().endsWith("-sources.jar") && !file.getName().endsWith("-javadoc.jar");
        }
    }
}
