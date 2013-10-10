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

import java.util.Arrays;
import java.util.TreeSet;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.granite.builder.GraniteBuilderContext;
import org.granite.builder.properties.Gas3Project;
import org.granite.builder.properties.GraniteProperties;
import org.granite.builder.util.ProjectUtil;
import org.granite.builder.util.SWTUtil;

/**
 * @author Franck WOLFF
 */
public class ProjectsPanel extends Composite {
	
	private final GraniteBuilderContext context;
	private final GraniteProperties properties;
	
	private Tree projectsTree = null;
	private boolean initialized = false;
	
	public ProjectsPanel(Composite parent, GraniteBuilderContext context) throws CoreException {
        super(parent, SWT.NONE);
        if (parent == null || context == null)
        	throw new NullPointerException("parent and context cannot be null");
        this.context = context;
        this.properties = context.getProperties();
        initializeComponents();
	}
	
	public TreeSet<Gas3Project> getProjects() {
		if (!initialized)
			return properties.getGas3().getProjects();
		
		TreeSet<Gas3Project> projects = new TreeSet<Gas3Project>();
		for (TreeItem root : projectsTree.getItems())
			projects.add((Gas3Project)root.getData());
		return projects;
	}

	@Override
	public Rectangle getClientArea() {
		initializeContent();
		return super.getClientArea();
	}
	
	private void initializeContent() {
		if (!initialized) {
	        for (Gas3Project project : properties.getGas3().getProjects())
	        	addProjectFolderTreeItem(project);
			initialized = true;
		}
	}
    
	private void initializeComponents() {
        setLayout(new GridLayout(2, false));
        
        Label text = new Label(this, SWT.NONE);
        text.setText("Other granite projects used for generation:");
        text.setLayoutData(SWTUtil.newGridData(SWT.NONE, 2));
        
        projectsTree = new Tree(this, SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        projectsTree.setLayoutData(new GridData(
            GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL |
            GridData.VERTICAL_ALIGN_FILL | GridData.GRAB_VERTICAL
        ));

		Composite buttons = new Composite(this, SWT.NONE);
		buttons.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
		buttons.setLayout(new FillLayout(SWT.VERTICAL));
		
		final Button addButton = SWTUtil.newButton(buttons, "Add Project...", true, new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				addProjectsHandler(e);
			}
		});
		
		final Button removeButton = SWTUtil.newButton(buttons, "Remove", false, new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// Remove selected root items.
				for (TreeItem item : projectsTree.getSelection()) {
					if (item.getParentItem() == null)
						item.dispose();
				}
				// Disable remove button if there is no more root nodes.
				if (projectsTree.getItemCount() == 0)
					((Button)e.getSource()).setEnabled(false);
			}
		});
		
		projectsTree.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// Enable/Disable buttons based on selected tree item.
				boolean isRoot = (((TreeItem)e.item).getParentItem() == null);
				removeButton.setEnabled(isRoot);
				addButton.setEnabled(isRoot);
			}
		});
	}
	
	private TreeItem addProjectFolderTreeItem(Gas3Project project) {
		try {
			IProject dependentProject = ProjectUtil.getProject(context.getJavaProject().getProject(), project.getPath());
			
			String displayedPath = dependentProject.getFullPath().makeRelative().toString();
			String icon = SWTUtil.IMG_GPROJECT_ERROR;
			if (!dependentProject.exists())
				displayedPath += " (does not exist)";
			else if (!dependentProject.isOpen())
				displayedPath += " (not opened)";
			else if (!ProjectUtil.isGraniteProject(dependentProject))
				displayedPath += " (not a granite project)";
			else
				icon = SWTUtil.IMG_GPROJECT;
			
	        TreeItem root = SWTUtil.addTreeItem(projectsTree, icon, displayedPath, null, null);
	        root.setData(project);
	        
	        return root;
		} catch (CoreException e) {
			throw new RuntimeException(e);
		}
	}
	
	private void addProjectsHandler(SelectionEvent event) {
		// Get currently configured projects.
		TreeItem[] roots = projectsTree.getItems();
        IPath[] usedEntries = new IPath[roots.length];
        for (int i = 0; i < usedEntries.length; i++)
        	usedEntries[i] = new Path(roots[i].getText());

        // Run add projects dialog.
		IPath[] selectedPaths = Dialogs.chooseProjectEntries(
			context.getJavaProject(),
			getDisplay().getActiveShell(),
			null,
			usedEntries
		);
		
		// Rebuild sorted projects list.
		if (selectedPaths != null && selectedPaths.length > 0) {
			selectedPaths = ProjectUtil.makeRelative(selectedPaths);
			
			IPath[] entries = new IPath[usedEntries.length + selectedPaths.length];
			System.arraycopy(usedEntries, 0, entries, 0, usedEntries.length);
			System.arraycopy(selectedPaths, 0, entries, usedEntries.length, selectedPaths.length);
			Arrays.sort(entries, ProjectUtil.IPATH_COMPARATOR);
			
			for (TreeItem root : projectsTree.getItems())
				root.dispose();

			for (IPath path : entries)
				addProjectFolderTreeItem(new Gas3Project(path.toString()));
		}
	}
}
