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

import java.util.ArrayList;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.ui.wizards.NewWizardMessages;
import org.eclipse.jdt.internal.ui.wizards.TypedViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.granite.builder.properties.Gas3Source;
import org.granite.builder.util.SWTUtil;

/**
 * @author Franck WOLFF
 */
@SuppressWarnings("all")
public class Dialogs {

	public static String[] addPackageTranslator(Shell shell, String title) {
		PackageTranslatorDialog dialog = new PackageTranslatorDialog(shell, null, null);
    	
		dialog.setTitle(title);
    	dialog.setHelpAvailable(false);
    	dialog.create();
    	
    	if (dialog.open() == Window.OK) {
    		Object[] result = dialog.getResult();
    		if (result != null && result.length == 2)
    			return (String[])result;
    	}
    	
    	return null;
	}

	public static String[] editPackageTranslator(Shell shell, String title, String initialJavaPath, String initialAs3Path) {
		PackageTranslatorDialog dialog = new PackageTranslatorDialog(shell, initialJavaPath, initialAs3Path);
    	
		dialog.setTitle(title);
    	dialog.setHelpAvailable(false);
    	dialog.create();
    	
    	if (dialog.open() == Window.OK) {
    		Object[] result = dialog.getResult();
    		if (result != null && result.length == 2)
    			return (String[])result;
    	}
    	
    	return null;
	}

	public static String[] editTemplateUris(Shell shell, String title, String initialTemplateUri, String initialBaseTemplateUri) {
		TemplateUrisDialog dialog = new TemplateUrisDialog(shell, initialTemplateUri, initialBaseTemplateUri);
    	
		dialog.setTitle(title);
    	dialog.setHelpAvailable(false);
    	dialog.create();
    	
    	if (dialog.open() == Window.OK) {
    		Object[] result = dialog.getResult();
    		if (result != null && result.length == 2)
    			return (String[])result;
    	}
    	
    	return null;
	}

	public static String prompt(Shell shell, String title, String message, String initialValue, Pattern valuePattern) {
		PromptDialog dialog = new PromptDialog(shell, initialValue, valuePattern);
    	
		dialog.setTitle(title);
    	dialog.setMessage(message);
    	dialog.setHelpAvailable(false);
    	dialog.create();
    	dialog.getOkButton().setEnabled(initialValue != null && initialValue.trim().length() > 0);
    	
    	if (dialog.open() == Window.OK) {
    		Object[] result = dialog.getResult();
    		if (result != null && result.length > 0)
    			return (String)result[0];
    	}
    	
    	return null;
	}
	
	public static Gas3Source editSourceFolderAttributes(IJavaProject project, Shell shell, Gas3Source source) {
		String[] initialValues = new String[] {source.getIncludes(), source.getExcludes(), source.getOutput()};
		
		IncludeExcludeOutputDialog dialog = new IncludeExcludeOutputDialog(shell, initialValues);
		dialog.setTitle("Source Folder Configuration");
		dialog.setHelpAvailable(false);
		
		if (dialog.open() == Window.OK) {
			String[] result = (String[])dialog.getResult();
			if (result != null) {
				source.setIncludes(result[0]);
				source.setExcludes(result[1]);
				source.setOutput(result[2]);
			}
		}
		return source;
	}
	
	public static IPath[] chooseSourceFolderEntries(IJavaProject project, Shell shell, IPath initialSelection, IPath[] usedEntries) {
		if (usedEntries == null)
			throw new IllegalArgumentException();
		String title= "Source Folder Selection"; 
		String message= "Choose source folder to be add to generation process"; 
		return internalChooseFolderEntries(project, shell, initialSelection, usedEntries, title, message);
	}
	
		
	private static IPath[] internalChooseFolderEntries(IJavaProject project, Shell shell, IPath initialSelection, IPath[] usedEntries, String title, String message) {	
		Class[] acceptedClasses= new Class[] { IProject.class, IFolder.class };
		ArrayList usedContainers= new ArrayList(usedEntries.length);
		IWorkspaceRoot root= ResourcesPlugin.getWorkspace().getRoot();
		for (int i= 0; i < usedEntries.length; i++) {
			IResource resource= root.findMember(usedEntries[i]);
			if (resource instanceof IContainer) {
				usedContainers.add(resource);
			}
		}
		
		IResource focus= initialSelection != null ? root.findMember(initialSelection) : null;
		Object[] used= usedContainers.toArray();
		
		MultipleFolderSelectionDialog dialog= new MultipleFolderSelectionDialog(
			shell, new WorkbenchLabelProvider(), new WorkbenchContentProvider(), false);
		dialog.setExisting(used);
		dialog.setTitle(title); 
		dialog.setMessage(message); 
		dialog.setHelpAvailable(false);
		dialog.addFilter(new JavaFoldersViewerFilter(project));
		dialog.setInput(root);
		dialog.setInitialFocus(focus);
		
		if (dialog.open() == Window.OK) {
			Object[] elements= dialog.getResult();
			IPath[] res= new IPath[elements.length];
			for (int i= 0; i < res.length; i++) {
				IResource elem= (IResource) elements[i];
				res[i]= elem.getFullPath();
			}
			return res;
		}
		return null;		
	}
	
	public static IPath[] chooseProjectEntries(IJavaProject project, Shell shell, IPath initialSelection, IPath[] usedEntries) {
		if (usedEntries == null)
			throw new IllegalArgumentException();
		String title= "Other Granite Project Selection"; 
		String message= "Choose granite project to be used in the generation process"; 
		return internalProjectEntries(project, shell, initialSelection, usedEntries, title, message);
	}
	
		
	private static IPath[] internalProjectEntries(IJavaProject project, Shell shell, IPath initialSelection, IPath[] usedEntries, String title, String message) {	
		Class[] acceptedClasses= new Class[] { IProject.class };
		ArrayList usedContainers= new ArrayList(usedEntries.length);
		IWorkspaceRoot root= ResourcesPlugin.getWorkspace().getRoot();
		for (int i= 0; i < usedEntries.length; i++) {
			IResource resource= root.findMember(usedEntries[i]);
			if (resource instanceof IContainer)
				usedContainers.add(resource);
		}
		
		IResource focus= initialSelection != null ? root.findMember(initialSelection) : null;
		Object[] used= usedContainers.toArray();
		
		MultipleFolderSelectionDialog dialog= new MultipleFolderSelectionDialog(
			shell, new WorkbenchLabelProvider(), new WorkbenchContentProvider(), false);
		dialog.setExisting(used);
		dialog.setTitle(title); 
		dialog.setMessage(message); 
		dialog.setHelpAvailable(false);
		dialog.addFilter(new GraniteProjectsViewerFilter(project));
		dialog.setInput(root);
		dialog.setInitialFocus(focus);
		
		if (dialog.open() == Window.OK) {
			Object[] elements= dialog.getResult();
			IPath[] res= new IPath[elements.length];
			for (int i= 0; i < res.length; i++) {
				IResource elem= (IResource) elements[i];
				res[i]= elem.getFullPath();
			}
			return res;
		}
		return null;		
	}
}
