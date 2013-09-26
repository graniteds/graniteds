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

package org.granite.builder.ui;

import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.granite.builder.util.ProjectUtil;

/**
 * @author Franck WOLFF
 */
public class JavaFoldersViewerFilter extends ViewerFilter {

	private final IJavaProject project;
	private final List<IPath> sourceFolderPaths;
	
	public JavaFoldersViewerFilter(IJavaProject project) {
		this.project = project;
		this.sourceFolderPaths = getSourceFolders(project);
	}
	
	private static List<IPath> getSourceFolders(IJavaProject project) {
		try {
			return ProjectUtil.getSourceFolders(project);
		} catch (CoreException e) {
			return Collections.emptyList();
		}
	}

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (IProject.class.isInstance(element) && element.equals(project.getProject()))
			return true;
		if (IFolder.class.isInstance(element)) {
			IPath path = ((IFolder)element).getFullPath();
			for (IPath sPath : sourceFolderPaths) {
				if (path.equals(sPath) || path.isPrefixOf(sPath))
					return true;
			}
		}
		return false;
	}
}
