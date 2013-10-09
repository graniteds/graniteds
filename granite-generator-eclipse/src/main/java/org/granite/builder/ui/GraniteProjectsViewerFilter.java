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

package org.granite.builder.ui;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.granite.builder.util.ProjectUtil;

/**
 * @author Franck WOLFF
 */
public class GraniteProjectsViewerFilter extends ViewerFilter {

	private final IJavaProject project;
	
	public GraniteProjectsViewerFilter(IJavaProject project) {
		this.project = project;
	}

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (IProject.class.isInstance(element) && !element.equals(project.getProject())) {
			try {
				return ProjectUtil.isGraniteProject((IProject)element);
			} catch (Exception e) {
				// fallback...
			}
		}
		return false;
	}
}
