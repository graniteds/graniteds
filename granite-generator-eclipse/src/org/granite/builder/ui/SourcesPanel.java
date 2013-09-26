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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.resources.IFolder;
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
import org.granite.builder.properties.Gas3Source;
import org.granite.builder.properties.GraniteProperties;
import org.granite.builder.util.ProjectUtil;
import org.granite.builder.util.SWTUtil;

/**
 * @author Franck WOLFF
 */
public class SourcesPanel extends Composite {

	private static final String INCLUDED = "Included: ";
	private static final String EXCLUDED = "Excluded: ";
	private static final String OUTPUT = "Output: ";
	private static final String ALL = "(All)";
	private static final String NONE = "(None)";
	
	private final GraniteBuilderContext context;
	private final GraniteProperties properties;
	
	private Tree sourcesTree = null;
	private boolean initialized = false;
	
	public SourcesPanel(Composite parent, GraniteBuilderContext context) throws CoreException {
        super(parent, SWT.NONE);
        if (parent == null || context == null)
        	throw new NullPointerException("parent and context cannot be null");
        this.context = context;
        this.properties = context.getProperties();
        initializeComponents();
	}
	
	public TreeSet<Gas3Source> getSources() {
		if (!initialized)
			return properties.getGas3().getSources();
		
		TreeSet<Gas3Source> sources = new TreeSet<Gas3Source>();
		for (TreeItem root : sourcesTree.getItems())
			sources.add((Gas3Source)root.getData());
		return sources;
	}

	@Override
	public Rectangle getClientArea() {
		initializeContent();
		return super.getClientArea();
	}
	
	private void initializeContent() {
		if (!initialized) {
	        for (Gas3Source source : properties.getGas3().getSources())
	        	addSourceFolderTreeItem(source);
			initialized = true;
		}
	}
    
	private void initializeComponents() {
        setLayout(new GridLayout(2, false));
        
        Label text = new Label(this, SWT.NONE);
        text.setText("Source folders used for generation:");
        text.setLayoutData(SWTUtil.newGridData(SWT.NONE, 2));
        
        sourcesTree = new Tree(this, SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        sourcesTree.setLayoutData(new GridData(
            GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL |
            GridData.VERTICAL_ALIGN_FILL | GridData.GRAB_VERTICAL
        ));

		Composite buttons = new Composite(this, SWT.NONE);
		buttons.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
		buttons.setLayout(new FillLayout(SWT.VERTICAL));
		
		final Button addButton = SWTUtil.newButton(buttons, "Add Folder...", true, new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				addFoldersHandler(e);
			}
		});
		
		final Button editButton = SWTUtil.newButton(buttons, "Edit...", false, new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				editFolderAttributesHandler(e);
			}
		});
		
		final Button removeButton = SWTUtil.newButton(buttons, "Remove", false, new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// Remove selected root items.
				for (TreeItem item : sourcesTree.getSelection()) {
					if (item.getParentItem() == null)
						item.dispose();
				}
				// Disable remove button if there is no more root nodes.
				if (sourcesTree.getItemCount() == 0)
					((Button)e.getSource()).setEnabled(false);
			}
		});
		
		sourcesTree.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// Enable/Disable buttons based on selected tree item.
				boolean isRoot = (((TreeItem)e.item).getParentItem() == null);
				removeButton.setEnabled(isRoot);
				addButton.setEnabled(isRoot);
				editButton.setEnabled(!isRoot);
			}
		});
	}
	
	private TreeItem addSourceFolderTreeItem(Gas3Source source) {
		IFolder folder = context.getJavaProject().getProject().getFolder(source.getPath());
		
		String displayedPath = folder.getFullPath().makeRelative().toString();
		String icon = folder.exists() ? SWTUtil.IMG_PKG_FOLDER : SWTUtil.IMG_PKG_FOLDER_ERROR;
		
        TreeItem root = SWTUtil.addTreeItem(sourcesTree, icon, displayedPath, null, null);
        SWTUtil.addTreeItem(root, SWTUtil.IMG_INCLUDES, source.getIncludes(), INCLUDED, ALL);
        SWTUtil.addTreeItem(root, SWTUtil.IMG_EXCLUDES, source.getExcludes(), EXCLUDED, NONE);
        SWTUtil.addTreeItem(root, SWTUtil.IMG_OUT_FOLDER, source.getOutput(), OUTPUT, null);
        root.setData(source);
        return root;
	}
	
	private void addFoldersHandler(SelectionEvent event) {
		// Get currently configured source folders.
		TreeItem[] roots = sourcesTree.getItems();
        IPath[] usedEntries = new IPath[roots.length];
        for (int i = 0; i < usedEntries.length; i++)
        	usedEntries[i] = new Path(roots[i].getText());
		
        // Run add folders dialog.
		IPath[] selectedPaths = Dialogs.chooseSourceFolderEntries(
			context.getJavaProject(),
			getDisplay().getActiveShell(),
			null,
			usedEntries
		);

		// Rebuild sorted source folders list.
		if (selectedPaths != null && selectedPaths.length > 0) {
			selectedPaths = ProjectUtil.makeRelative(selectedPaths);
			IPath projectPath = context.getJavaProject().getPath().makeRelative();
			
			Set<IPath> newSourceFolders = new TreeSet<IPath>(ProjectUtil.IPATH_COMPARATOR);
			newSourceFolders.addAll(Arrays.asList(usedEntries));
			List<IPath> jSourceFolders = ProjectUtil.makeRelative(getSourceFolders());
			for (IPath selectedPath : selectedPaths) {
				if (selectedPath.equals(projectPath)) {
					newSourceFolders.addAll(jSourceFolders);
					break;
				}
				for (IPath jSourceFolder : jSourceFolders) {
					if (jSourceFolder.matchingFirstSegments(selectedPath) >= 2)
						newSourceFolders.add(jSourceFolder);
				}
			}
			
			Map<IPath, TreeItem> rootMap = new HashMap<IPath, TreeItem>(roots.length);
			for (TreeItem root : roots)
				rootMap.put(new Path(root.getText()), root);
			
			String defaultOutput = "as3";
			for (IPath newSourceFolder : newSourceFolders) {
				TreeItem root = rootMap.get(newSourceFolder);
				if (root != null)
					addSourceFolderTreeItem((Gas3Source)root.getData());
				else {
					String path = newSourceFolder.removeFirstSegments(1).makeRelative().toString();
					TreeItem item = addSourceFolderTreeItem(new Gas3Source(path, null, null, defaultOutput));
					item.setExpanded(true);
				}
			}
			
			for (TreeItem root : roots)
				root.dispose();
		}
	}
	
	private void editFolderAttributesHandler(SelectionEvent event) {
		TreeItem[] selection = sourcesTree.getSelection();
		if (selection.length == 1 && selection[0].getParentItem() != null && selection[0].getParentItem().getParentItem() == null) {
			Gas3Source source = (Gas3Source)selection[0].getParentItem().getData();
			source = Dialogs.editSourceFolderAttributes(context.getJavaProject(), getDisplay().getActiveShell(), source);
			SWTUtil.setTreeItemText(selection[0].getParentItem().getItem(0), source.getIncludes());
			SWTUtil.setTreeItemText(selection[0].getParentItem().getItem(1), source.getExcludes());
			SWTUtil.setTreeItemText(selection[0].getParentItem().getItem(2), source.getOutput());
			selection[0].getParentItem().setData(source);
		}
	}
	
	private List<IPath> getSourceFolders() {
		try {
			return ProjectUtil.getSourceFolders(context.getJavaProject());
		} catch (CoreException e) {
			return Collections.emptyList();
		}
	}
}
