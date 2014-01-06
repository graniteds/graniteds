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

package org.granite.builder.properties;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.granite.builder.properties.GraniteProperties;
import org.granite.builder.util.FileUtil;
import org.granite.builder.util.XStreamUtil;

/**
 * @author Franck WOLFF
 */
public class GranitePropertiesLoader {

    private static final String FILE_NAME = ".granite";
    private static final String DEFAULT_CHARSET = "UTF-8";

	public static GraniteProperties load(IProject project) throws IOException {
		File file = getPropertiesFile(project);
        if (file.exists()) {
        	GraniteProperties properties = XStreamUtil.load(file, GraniteProperties.class, DEFAULT_CHARSET);
        	ValidationResults results = new ValidationResults();
        	properties.validate(results);
        	if (results.hasErrors())
        		throw new IOException("Illegal '.granite' file in your project. " + results);
        	properties.setTimestamp(file.lastModified());
        	return properties;
        }
        return GraniteProperties.getDefaultProperties();
	}
	
	public static void save(IProject project, GraniteProperties properties) throws IOException {
		File file = getPropertiesFile(project);
		XStreamUtil.save(file, properties, DEFAULT_CHARSET);
	}
	
	public static boolean isOutdated(IProject project, GraniteProperties properties) {
		File file = getPropertiesFile(project);
		return properties == null || (file.exists() && file.lastModified() > properties.getTimestamp());
	}
	
    public static File getPropertiesFile(IProject project) {
    	try {
    		return FileUtil.getLocationFile(project.getFile(FILE_NAME));
    	}
    	catch (CoreException e) {
    		throw new RuntimeException("Could not get " + FILE_NAME + " location file", e);
    	}
    }
    
    public static boolean exists(IProject project) {
    	File propertiesFile = getPropertiesFile(project);
    	return propertiesFile != null && propertiesFile.exists();
    }
}
