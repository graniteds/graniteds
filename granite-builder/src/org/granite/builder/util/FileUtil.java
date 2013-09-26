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

package org.granite.builder.util;

import java.io.File;
import java.net.URI;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * @author Franck WOLFF
 */
public class FileUtil {

	public static URI getLocationURI(IResource resource) throws CoreException {
		File file = getLocationFile(resource);
		return (file != null ? file.toURI() : null);
	}

	public static String getLocationPath(IResource resource) throws CoreException {
		File file = getLocationFile(resource);
		return (file != null ? file.toString() : null);
	}

	public static File getLocationFile(IResource resource) throws CoreException {
		if (resource == null)
			return null;
		URI uri = resource.getLocationURI();
		IFileStore store = EFS.getStore(uri);
		return store.toLocalFile(0, null);
	}
	
	public static IPath makeRelativeTo(IPath parent, IPath child) {
		String parentFullPath = parent.makeAbsolute().toPortableString();
		String childFullPath = child.makeAbsolute().toPortableString();
		
		if (childFullPath.startsWith(parentFullPath)) {
			String relativePath = childFullPath.substring(parentFullPath.length());
			if (relativePath.startsWith("/"))
				relativePath = relativePath.substring(1, relativePath.length());
			return new Path(relativePath);
		}
		return child;
 	}
}
