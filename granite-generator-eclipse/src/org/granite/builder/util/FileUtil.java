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
