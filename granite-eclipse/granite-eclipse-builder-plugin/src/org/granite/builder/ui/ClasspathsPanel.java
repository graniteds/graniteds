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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.granite.builder.GraniteBuilderContext;
import org.granite.builder.properties.Gas3Classpath;
import org.granite.builder.properties.GraniteProperties;
import org.granite.builder.util.ProjectUtil;
import org.granite.builder.util.SWTUtil;
import org.granite.builder.util.ProjectUtil.CpEntry;

/**
 * @author Franck WOLFF
 */
public class ClasspathsPanel extends Composite {

	private final GraniteBuilderContext context;
	private final GraniteProperties properties;
	
	private Tree classpathsTree = null;
	private boolean initialized = false;

	public ClasspathsPanel(Composite parent, GraniteBuilderContext context) throws CoreException {
        super(parent, SWT.NONE);
        this.context = context;
        this.properties = context.getProperties();
        initializeComponents();
	}

	public List<Gas3Classpath> getClasspaths() {
		if (!initialized)
			return properties.getGas3().getClasspaths();
		
		List<Gas3Classpath> classpaths = new ArrayList<Gas3Classpath>(classpathsTree.getItemCount() - 1);
		for (int i = 1; i < classpathsTree.getItemCount(); i++) {
			TreeItem item = classpathsTree.getItem(i);
			classpaths.add(new Gas3Classpath((String)item.getData()));
		}
		return classpaths;
	}

	@Override
	public Rectangle getClientArea() {
		initializeContent();
		return super.getClientArea();
	}

	private void initializeContent() {
		if (!initialized) {
			try {
		    	TreeItem projectItem = SWTUtil.addTreeItem(classpathsTree, SWTUtil.IMG_PKG_FOLDER, "Project classpath (can't be removed)", null, null);

		    	// Output locations.
		    	List<CpEntry> cpEntries = ProjectUtil.getFullClasspath(context.getJavaProject());
		    	for (CpEntry entry : cpEntries) {
		    		if (entry.getKind() == CpEntry.CpeKind.SOURCE_OUTPUT_DIR || entry.getKind() == CpEntry.CpeKind.PROJECT_OUTPUT_DIR)
		    			SWTUtil.addTreeItem(projectItem, SWTUtil.IMG_PKG_FOLDER, entry.getDescription(), null, null);
		    	}

		    	// Project jars.
		    	for (CpEntry entry : cpEntries) {
		    		if (entry.getKind() == CpEntry.CpeKind.LIB_JAR)
		    			SWTUtil.addTreeItem(projectItem, SWTUtil.IMG_JAR, entry.getDescription(), null, null);
		    	}

		    	// Containers jars.
	    		for (CpEntry entry : cpEntries) {
	    			if (entry.getKind() == CpEntry.CpeKind.CONTAINER_JAR) {
	    	    		TreeItem jreItem = SWTUtil.addTreeItem(projectItem, SWTUtil.IMG_LIBRARY, entry.getDescription(), null, null);
	    	    		for (CpEntry cEntry : entry.getChildren())
	    	    			SWTUtil.addTreeItem(jreItem, SWTUtil.IMG_JAR_LIBRARY, cEntry.getDescription(), null, null);
	    			}
	    		}

	    		// Added jars.
		    	for (Gas3Classpath classpath : properties.getGas3().getClasspaths()) {
		    		File path = new File(classpath.getPath());
					TreeItem item;
		    		if (!path.exists())
		    			item = SWTUtil.addTreeItem(classpathsTree, SWTUtil.IMG_WARNING, classpath.getPath(), null, null);
		    		else if (path.isDirectory())
		    			item = SWTUtil.addTreeItem(classpathsTree, SWTUtil.IMG_PKG_FOLDER, classpath.getPath(), null, null);
		    		else {
						String label = path.getName();
						if (path.getParent() != null)
							label += " - " + path.getParent().toString();
						item = SWTUtil.addTreeItem(classpathsTree, SWTUtil.IMG_JAR, label, null, null);
					}
					item.setData(classpath.getPath());
		    	}
			} catch (CoreException e) {
				throw new RuntimeException(e);
			}
			initialized = true;
		}
	}
	
	private void initializeComponents() {
        setLayout(new GridLayout(2, false));
        
        Label text = new Label(this, SWT.NONE);
        text.setText("Jars and class folders on the classpath:");
        text.setLayoutData(SWTUtil.newGridData(SWT.NONE, 2));
        
        classpathsTree = new Tree(this, SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        classpathsTree.setLayoutData(new GridData(
            GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL |
            GridData.VERTICAL_ALIGN_FILL | GridData.GRAB_VERTICAL
        ));
        
		
		Composite buttons = new Composite(this, SWT.NONE);
		buttons.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
		buttons.setLayout(new FillLayout(SWT.VERTICAL));
		
		SWTUtil.newButton(buttons, "Add Jar...", true, new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				FileDialog dlg = new FileDialog(e.display.getActiveShell(), SWT.OPEN | SWT.MULTI);
				dlg.setFilterPath(context.getJavaProject().getProject().getLocation().toString());
			    dlg.setText("Jar File Dialog");
			    dlg.setFilterExtensions(new String[]{"*.jar", "*.zip"});

				dlg.open();

				for (String jar : dlg.getFileNames()) {
					TreeItem item = SWTUtil.addTreeItem(classpathsTree, SWTUtil.IMG_JAR, jar + " - " + dlg.getFilterPath(), null, null);
					item.setData(dlg.getFilterPath() + File.separatorChar + jar);
				}
			}
		});
		
		SWTUtil.newButton(buttons, "Add Folder...", true, new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dlg = new DirectoryDialog(e.display.getActiveShell());
				dlg.setFilterPath(context.getJavaProject().getProject().getLocation().toString());
			    dlg.setText("Class Folder Dialog");
			    dlg.setMessage("Select a class folder to add");
				
			    String dir = dlg.open();
				
			    if (dir != null) {
			    	TreeItem item = SWTUtil.addTreeItem(classpathsTree, SWTUtil.IMG_PKG_FOLDER, dir, null, null);
			    	item.setData(dir);
			    }
			}
		});
		
		final Button removeButton = SWTUtil.newButton(buttons, "Remove", false, new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				// Remove selected root items.
				for (TreeItem item : classpathsTree.getSelection()) {
					if (item.getParentItem() == null && !item.equals(classpathsTree.getItem(0)))
						item.dispose();
				}
			}
		});
		
		classpathsTree.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// Enable/Disable buttons based on selected tree items.
				boolean removeEnabled = classpathsTree.getSelectionCount() > 0;
				for (TreeItem item : classpathsTree.getSelection()) {
					while (item.getParentItem() != null)
						item = item.getParentItem();
					if (item.equals(classpathsTree.getItem(0))) {
						removeEnabled = false;
						break;
					}
				}
				removeButton.setEnabled(removeEnabled);
			}
		});
	}
}
