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

package org.granite.builder.util;

import static org.granite.builder.GraniteBuilder.FLEX_BUILDER_ID;
import static org.granite.builder.GraniteBuilder.GRANITE_BUILDER_ID;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.granite.builder.GraniteActivator;
import org.granite.util.ClassUtil;

/**
 * @author Franck WOLFF
 */
public class ProjectUtil {

	public static final Comparator<IPath> IPATH_COMPARATOR = new Comparator<IPath>() {
		@Override
		public int compare(IPath path1, IPath path2) {
			return path1.toString().compareTo(path2.toString());
		}
	};
	
	public static IProject[] getAllProjects(IProject project) {
		if (project == null)
			return new IProject[0];
		return project.getWorkspace().getRoot().getProjects();
	}
	
	public static IProject[] getAllOtherGraniteProjects(IProject project) throws CoreException {
		if (project == null)
			return new IProject[0];
		List<IProject> projects = new ArrayList<IProject>();
		for (IProject p : getAllProjects(project)) {
			if (!p.equals(project) && isGraniteProject(p))
				projects.add(p);
		}
		return projects.toArray(new IProject[projects.size()]);
	}
	
	public static IProject getProject(IProject project, String name) {
		if (project == null || name == null)
			return null;
		return project.getWorkspace().getRoot().getProject(name);
	}
	
	public static boolean isGraniteProject(IProject project) throws CoreException {
		if (project != null && project.exists() && project.isOpen()) {
			for (ICommand command : project.getDescription().getBuildSpec()) {
				if (GRANITE_BUILDER_ID.equals(command.getBuilderName()))
					return true;
			}
		}
		return false;
	}
	
	public static boolean isFlexBuilderProject(IProject project) throws CoreException {
		if (project != null && project.exists() && project.isOpen()) {
			for (ICommand command : project.getDescription().getBuildSpec()) {
				if (FLEX_BUILDER_ID.equals(command.getBuilderName()))
					return true;
			}
		}
		return false;
	}

    public static List<IPath> makeRelative(List<IPath> paths) {
    	if (paths == null)
    		return null;
    	return Arrays.asList(makeRelative(paths.toArray(new IPath[paths.size()])));
    }
    
    public static IPath[] makeRelative(IPath[] paths) {
    	if (paths != null) {
	    	for (int i = 0; i < paths.length; i++)
	    		paths[i] = paths[i].makeRelative();
    	}
    	return paths;
    }
    
    public static File getWorkspaceFile(IProject project) {
        IWorkspace workspace = project.getWorkspace();
        return workspace.getRoot().getLocation().toFile().getAbsoluteFile();
    }
    
    public static URI getWorkspaceURI(IProject project) {
        return getWorkspaceFile(project).toURI();
    }
    
    public static File getProjectFile(IProject project) {
        try {
			return FileUtil.getLocationFile(project).getAbsoluteFile();
		} catch (CoreException e) {
			throw new RuntimeException("Could not get " + project + " location file", e);
		}
    }
    
    public static URI getProjectURI(IProject project) {
        try {
			return FileUtil.getLocationURI(project);
		} catch (CoreException e) {
			throw new RuntimeException("Could not get " + project + " location URI", e);
		}
    }
    
    public static List<CpEntry> getFullResolvedClasspath(IJavaProject project) throws CoreException {
    	return getFullClasspath(project, project.getResolvedClasspath(true));
    }
    
    public static List<CpEntry> getFullClasspath(IJavaProject project) throws CoreException {
    	return getFullClasspath(project, project.getRawClasspath());
    }
        
    public static List<CpEntry> getFullClasspath(IJavaProject project, IClasspathEntry[] classpathEntries) throws CoreException {
    	List<CpEntry> classpath = new ArrayList<CpEntry>();

    	IWorkspaceRoot workspace = project.getProject().getWorkspace().getRoot();
        IPath path = null;
        try {

        	// Output locations.
        	if (project.getOutputLocation() != null) {
                path = project.getOutputLocation();
                File file = workspace.findMember(path).getLocation().toFile();
                classpath.add(new CpEntry(path.makeRelative().toString(), path, file, CpEntry.CpeKind.PROJECT_OUTPUT_DIR));
	    	}
	    	for (IClasspathEntry cpe : classpathEntries) {
	    		if (cpe.getEntryKind() == IClasspathEntry.CPE_SOURCE && cpe.getOutputLocation() != null) {
                    path = cpe.getOutputLocation();
                    File file = workspace.findMember(path).getLocation().toFile();
                    classpath.add(new CpEntry(path.makeRelative().toString(), path, file, CpEntry.CpeKind.SOURCE_OUTPUT_DIR));
	    		}
	    	}
        	
        	// Project jars.
            for (IClasspathEntry cpe : classpathEntries) {
                if (cpe.getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
                    path = cpe.getPath();
                    if (path != null) {
                    	IResource member = workspace.findMember(path);
                    	String description = path.lastSegment();
		    			if (path.segmentCount() > 1)
		    				description += " - " + path.removeLastSegments(1).makeRelative();
                    	if (member != null)
                    		classpath.add(new CpEntry(description, path, member.getLocation().toFile(), CpEntry.CpeKind.LIB_JAR));
                    	else
                    		classpath.add(new CpEntry(description, path, path.toFile(), CpEntry.CpeKind.LIB_JAR));
                    }
                }
            }
            
            // Containers jars/directories.
            for (IClasspathEntry cpe : classpathEntries) {
                if (cpe.getEntryKind() == IClasspathEntry.CPE_CONTAINER) {
	    			path = cpe.getPath();
	    			IClasspathContainer container = JavaCore.getClasspathContainer(path, project);
	    			String description = container.getDescription();
	    			CpEntry entry = new CpEntry(description, path, (path != null ? path.toFile() : null), CpEntry.CpeKind.CONTAINER_JAR);
		    		
	    			for (IClasspathEntry ccpe : container.getClasspathEntries()) {
		    			path = ccpe.getPath();
		    			String label = path.lastSegment();
		    			if (path.segmentCount() > 1)
		    				label += " - " + path.removeLastSegments(1).makeRelative();
		    			
		    			File file = path.toFile();
		    			IResource resource = workspace.findMember(path);
		    			if (resource != null)
		    				file = resource.getLocation().toFile();

		    			entry.getChildren().add(new CpEntry(label, path, file, CpEntry.CpeKind.LIB_JAR));
		    		}
	    			
	    			classpath.add(entry);
                }
            }
	    	
        } catch (Exception e) {
            String msg = "Problem with classpath location: " + path;
            throw new CoreException(createErrorStatus(msg, e));
        }
    	
    	return classpath;
    }

    public static List<IPath> getSourceFolders(IJavaProject project) throws CoreException {
        List<IPath> paths = new ArrayList<IPath>();

        for (IClasspathEntry cpe : project.getRawClasspath()) {
            if (cpe.getEntryKind() == IClasspathEntry.CPE_SOURCE)
            	paths.add(cpe.getPath());
        }
        
        return paths;
    }
    
    public static IPath getOutputFolder(IJavaProject project, IClasspathEntry cpe) throws CoreException {
    	if (cpe.getOutputLocation() != null)
    		return cpe.getOutputLocation();
    	return project.getOutputLocation();
    }
    
    public static List<IPath> getOutputFolders(IJavaProject project) throws CoreException {
        List<IPath> paths = new ArrayList<IPath>();

        for (IClasspathEntry cpe : project.getRawClasspath()) {
            if (cpe.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
                IPath output = cpe.getOutputLocation();
                if (output != null)
                    paths.add(output);
            }
        }
        IPath output = project.getOutputLocation();
        if (output != null)
            paths.add(output);
        
        return paths;
    }
    
    public static IPath getJavaPath(IJavaProject project, IFile classFile, boolean keepInner) throws CoreException {
    	IPath classPath = classFile.getFullPath();
    	
    	List<IPath> outputFolders = getOutputFolders(project);
    	IPath classOutputFolder = null;
    	for (IPath outputFolder : outputFolders) {
    		if (outputFolder.isPrefixOf(classPath)) {
    			classOutputFolder = outputFolder;
    			break;
    		}
    	}
    	if (classOutputFolder == null)
    		return null;
    	
    	classPath = classPath.removeFirstSegments(classOutputFolder.segmentCount());
    	
    	String sPath = classPath.toString();
    	sPath = sPath.substring(0, sPath.length() - classPath.getFileExtension().length() - 1);
    	if (!keepInner) {
	    	int iDollar = sPath.indexOf('$');
	    	if (iDollar != -1)
	    		sPath = sPath.substring(iDollar);
    	}
    	sPath += ".java";
    	
    	return new Path(sPath);
    }

    public static JavaClassInfo getJavaClassInfo(IJavaProject project, Class<?> clazz) {
    	try {
	    	URI clazzUri = ClassUtil.findResource(clazz).toURI();
	    	URI projectUri = getProjectURI(project.getProject());
	    	URI relativeURI = projectUri.relativize(clazzUri);
	    	return getJavaClassInfo(project, project.getProject().getFile(relativeURI.toString()));
    	} catch (Exception e) {
    		return null;
    	}
    }
    
    public static JavaClassInfo getJavaClassInfo(IJavaProject project, IFile resource) throws CoreException {
    	// classPath = "<project name>/<output folder>/<package>/<file>.class"
    	IPath classPath = resource.getFullPath().makeRelative();
    	
    	// Find output folder: "<project name>/<output folder>".
    	IPath outputFolder = null;
    	if (project.getOutputLocation() != null && project.getOutputLocation().isPrefixOf(classPath))
    		outputFolder = project.getOutputLocation();
    	else {
    		for (IClasspathEntry cpe : project.getRawClasspath()) {
                if (cpe.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
                    IPath output = cpe.getOutputLocation();
                    if (output != null && output.isPrefixOf(classPath)) {
                    	outputFolder = output;
                    	break;
                    }
                }
    		}
    	}
    	
    	if (outputFolder == null)
    		return null;
    	
    	// Compute class name.
    	IPath relativeClassPath = classPath.removeFirstSegments(outputFolder.segmentCount());
    	IPath relativeClassName = relativeClassPath.removeFileExtension();
    	String className = relativeClassName.toPortableString().replace('/', '.');
    	
    	// Compute java source path: "<package>/<class name>[$<inner class>].java".
    	String javaFullPath = relativeClassName.addFileExtension("java").toPortableString();
    	String javaFilePath = javaFullPath;
    	int iDollar = javaFilePath.indexOf('$');
    	if (iDollar != -1)
    		javaFilePath = javaFilePath.substring(0, iDollar) + ".java";

    	IJavaElement javaElement = project.findElement(new Path(javaFilePath));
    	if (javaElement == null)
    		return null;

    	IJavaElement sourceFolder = javaElement.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
    	if (sourceFolder == null)
    		return null;
    	
    	IPath folderPath = sourceFolder.getPath().makeRelative();
    	if (folderPath.segmentCount() > 0)
    		folderPath = folderPath.removeFirstSegments(1);
    	
    	// Fix issues with project not in the workspace directory.
    	outputFolder = project.getProject().getWorkspace().getRoot().findMember(outputFolder).getLocation();
    	
    	return new JavaClassInfo(
    		folderPath.toPortableString(),
    		javaFullPath,
    		className,
    		new File(outputFolder.toPortableString(), relativeClassPath.toPortableString())
    	);
    }
    
    public static IStatus createErrorStatus(String message) {
        return new Status(IStatus.ERROR, GraniteActivator.PLUGIN_ID, message);
    }
    
    public static IStatus createErrorStatus(String message, Throwable t) {
        return new Status(IStatus.ERROR, GraniteActivator.PLUGIN_ID, IStatus.OK, message, t);
    }
    
    public static class CpEntry {
    	
    	public enum CpeKind {
    		PROJECT_OUTPUT_DIR,
    		SOURCE_OUTPUT_DIR,
    		CONTAINER_JAR,
    		LIB_JAR
    	}

    	private final String description;
    	private final IPath path;
    	private final File file;
    	private final CpeKind kind;
    	private final List<CpEntry> children = new ArrayList<CpEntry>();

    	public CpEntry(String description, IPath path, File file, CpeKind kind) {
    		this.description = description;
			this.path = path;
			this.file = file;
			this.kind = kind;
		}

		public String getDescription() {
			return description;
		}

		public IPath getPath() {
			return path;
		}

		public File getFile() {
			return file;
		}

		public CpeKind getKind() {
			return kind;
		}
		
		public List<CpEntry> getChildren() {
			return children;
		}

		public boolean exists() {
			return (file != null && file.exists());
		}
		
		public URL toURL() throws IOException {
			if (file == null)
				return null;
			return file.getCanonicalFile().toURI().toURL();
		}

		@Override
		public String toString() {
			return "[" + kind + "] " + path + " -> " + file;
		}
    }
}
