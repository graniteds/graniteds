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

package org.granite.builder;

import java.io.File;
import java.io.FileFilter;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.granite.builder.properties.Gas3Source;
import org.granite.builder.properties.Gas3Transformer;
import org.granite.builder.properties.GranitePropertiesLoader;
import org.granite.builder.ui.AddNatureWizard;
import org.granite.builder.util.BuilderUtil;
import org.granite.builder.util.FlexConfigGenerator;
import org.granite.builder.util.JavaClassInfo;
import org.granite.builder.util.ProjectUtil;
import org.granite.generator.Generator;
import org.granite.generator.Listener;
import org.granite.generator.Output;
import org.granite.generator.Transformer;
import org.granite.generator.as3.JavaAs3Input;
import org.granite.generator.as3.JavaAs3Output;
import org.granite.generator.as3.PackageTranslator;

/**
 * @author Franck WOLFF
 */
public class GraniteBuilder extends IncrementalProjectBuilder {

    
    public static final String JAVA_BUILDER_ID = "org.eclipse.jdt.core.javabuilder";
    public static final String FLEX_BUILDER_ID = "com.adobe.flexbuilder.project.flexbuilder";
    public static final String GRANITE_BUILDER_ID = "org.granite.builder.granitebuilder";

    private static final int PROGRESS_TOTAL = 100;

    private final Generator generator;
    private final BuilderListener listener;
    private BuilderConfiguration config;

    ///////////////////////////////////////////////////////////////////////////
    // Constructor.

    public GraniteBuilder() {
		super();
		this.generator = new Generator();
		this.listener = new BuilderListener();
	}

    ///////////////////////////////////////////////////////////////////////////
    // Build.

	@SuppressWarnings("rawtypes")
    @Override
    protected IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException {
    	listener.title("Building project \"" + getProject().getName() + "\" (" + DateFormat.getInstance().format(new Date()) + ")...");
    	long t0 = System.currentTimeMillis();
    	
    	
    	GenerationResult result = null;
    	try {
        	if (!GranitePropertiesLoader.exists(getProject())) {
        		BuilderConsole.activate();
    	    	AddNatureWizard.run(getProject());
	    		config = null;
	    		generator.clear();
        	} else if (args.containsKey(GraniteRebuildJob.RESET_KEY) || (config != null && config.isOutdated())) {
	    		config = null;
	    		generator.clear();
	    	}
	    	
	        config = getConfig();
	        config.resetClassLoader();
	        config.getGroovyTemplateFactory().cleanOutdated();
	        
	        generator.setConfig(config);
	        
	        BuilderConsole.setDebugEnabled(config.getProperties().getGas3().isDebugEnabled());
	                
	        if (generator.isEmpty()) {
	        	for (Gas3Transformer gas3Transformer : config.getProperties().getGas3().getTransformers()) {
	            	try {
	            		Transformer<?,?,?> transformer = BuilderUtil.newInstance(Transformer.class, gas3Transformer.getType(), config.getClassLoader());
	            		transformer.setListener(listener);
	            		generator.add(transformer);
	            	} catch (Exception e) {
		            	listener.error("Could not load transformer: " + gas3Transformer.getType(), e);
		            	
		            	if (e instanceof CoreException)
		            		throw (CoreException)e;
		            	if (e.getCause() instanceof CoreException)
		            		throw (CoreException)e.getCause();
		                throw new CoreException(ProjectUtil.createErrorStatus(
		                	"Could not load transformer: " + gas3Transformer.getType(), null
		                ));
	            	}
	        	}
	        }
	
	    	
	    	if (monitor == null)
	            monitor = new NullProgressMonitor();
	
	        try {
	            if (kind == FULL_BUILD)
	            	result = fullBuild(monitor);
	            else {
	                IResourceDelta delta = getDelta(getProject());
	                if (delta == null)
	                	result = fullBuild(monitor);
	                else
	                	result = incrementalBuild(delta, monitor);
	            }
	        } catch (CoreException e) {
	            throw e;
	        } catch (Exception e) {
	            throw new CoreException(ProjectUtil.createErrorStatus("Granite Build Failed", e));
	        }
	        
	        boolean refreshFlexConfig = false;
	        try {
	        	if (result.generateFlexConfig && config.getProperties().getGas3().isFlexConfig())
	        		refreshFlexConfig = FlexConfigGenerator.generateFlexConfig(config, listener, getProject());
	        }
	        catch (Exception e) {
	        	listener.warn("Could not generate Flex Builder configuration", e);
	        }
	
	        File projectDir = ProjectUtil.getProjectFile(getProject());
	        for (File dir : result.dirsToRefresh) {
	        	StringBuilder relativePath = new StringBuilder();
	        	while (dir != null && !dir.equals(projectDir)) {
	        		relativePath.insert(0, '/').insert(1, dir.getName());
	        		dir = dir.getParentFile();
	        	}
	        	getProject().getFolder(relativePath.toString()).refreshLocal(IResource.DEPTH_INFINITE, monitor);
	        }
	        if (refreshFlexConfig)
	        	getProject().getFile(FlexConfigGenerator.FILE_NAME).refreshLocal(IResource.DEPTH_ZERO, monitor);
    	}
    	finally {
	        long t1 = System.currentTimeMillis();
	        
	        if (result != null) {
		        listener.title(
		        	"Done (" + (result.affectedFiles > 0 ? result.affectedFiles + " affected files" : "nothing to do") +
		        	" - " + (t1 - t0) + "ms)."
		        );
	        } else
		        listener.title("Done (error) - " + (t1 - t0) + "ms).");
	        
	        listener.title("");
    	}
        
        return null;
    }
	
	class GenerationResult {
		public int affectedFiles = 0;
		public Set<File> dirsToRefresh = new HashSet<File>();
		public boolean generateFlexConfig = false;
	}

    ///////////////////////////////////////////////////////////////////////////
    // Full Build.

    private GenerationResult fullBuild(final IProgressMonitor monitor) throws CoreException {
        monitor.beginTask("Granite Full Build", PROGRESS_TOTAL);
        FullBuildVisitor visitor = new FullBuildVisitor(monitor);
        try {
            getProject().accept(visitor);
        } finally {
            monitor.done();
        }
        return visitor.getResult();
    }

    class FullBuildVisitor implements IResourceVisitor {

        private IProgressMonitor monitor;
        private GenerationResult result = new GenerationResult();

        public FullBuildVisitor(IProgressMonitor monitor) {
            this.monitor = monitor;
        }

        public boolean visit(IResource resource) throws CoreException {
            if (!resource.isAccessible() || resource.isPhantom())
                return false;

            Output<?>[] outputs = generate(resource, monitor);
            if (outputs != null) {
            	for (Output<?> output : outputs) {
            		if (output.isOutdated()) {
            			result.affectedFiles++;
            			result.dirsToRefresh.add(((JavaAs3Output)output).getDir());
            		}
            	}
            }
			result.generateFlexConfig = true;

            return true;
        }

		public GenerationResult getResult() {
			return result;
		}
    }

    ///////////////////////////////////////////////////////////////////////////
    // Incremental Build.

    /*
     * TODO: this part should be refactored, it assumes several things about
     * generated (.as extension, Base suffix, etc.) which should be deferred
     * to transformers...
     */
    
    private GenerationResult incrementalBuild(IResourceDelta delta, IProgressMonitor monitor) throws CoreException {
        monitor.beginTask("Granite Incremental Build", PROGRESS_TOTAL);
        IncrementalBuildVisitor visitor = new IncrementalBuildVisitor(monitor);
        try {
            delta.accept(visitor);
        } finally {
            monitor.done();
        }
        return visitor.getResult();
    }

    class IncrementalBuildVisitor implements IResourceDeltaVisitor {

        private IProgressMonitor monitor;
        private GenerationResult result = new GenerationResult();

        public IncrementalBuildVisitor(IProgressMonitor monitor) {
            this.monitor = monitor;
        }

        public boolean visit(IResourceDelta delta) throws CoreException {
            IResource resource = delta.getResource();
            String extension = resource.getFileExtension();

            Output<?>[] outputs = null;
            
            switch (delta.getKind()) {
            case IResourceDelta.ADDED:
                if (!resource.isAccessible() || resource.isPhantom())
                    return false;
            	outputs = generate(resource, monitor);
            	if (!result.generateFlexConfig)
            		result.generateFlexConfig = "as".equals(resource.getFileExtension());
                break;
            case IResourceDelta.REMOVED:
            	if (!result.generateFlexConfig)
            		result.generateFlexConfig = "as".equals(extension);
            	outputs = remove(resource, monitor);
                break;
            case IResourceDelta.CHANGED:
                if (!resource.isAccessible() || resource.isPhantom())
                    return false;
            	outputs = generate(resource, monitor);
                break;
            }
            
            if (outputs != null) {
            	for (Output<?> output : outputs) {
            		if (output.isOutdated()) {
            			result.affectedFiles++;
            			result.dirsToRefresh.add(((JavaAs3Output)output).getDir());
            			result.generateFlexConfig = true;
            		}
            	}
            }

            return true;
        }

		public GenerationResult getResult() {
			return result;
		}
    }
    
    private BuilderConfiguration getConfig() {
        if (config == null || config.isOutdated())
        	config = new BuilderConfiguration(listener, getProject());
        return config;
    }

    private Output<?>[] generate(IResource resource, IProgressMonitor monitor) {
        if (resource instanceof IFile && "class".equals(resource.getFileExtension())) {
            IFile file = (IFile)resource;
                        
            try {
	            JavaClassInfo info = ProjectUtil.getJavaClassInfo(config.getJavaProject(), (IFile)resource);
	            if (info == null) {
	            	listener.warn("Could not get class informations for: " + resource.toString());
	            	return null;
	            }
	            
	            Gas3Source source = config.getProperties().getGas3().getMatchingSource(
	            	info.getSourceFolderPath(),
	            	info.getSourceFilePath()
	            );
	            
	            if (source != null) {
		            monitor.subTask("Generating AS3 code for: " + file.getProjectRelativePath().toString());
		            try {
		            	Class<?> clazz = config.getClassLoader().loadClass(info.getClassName());
		            	if (!clazz.isAnonymousClass() && config.isGenerated(clazz)) {
			            	JavaAs3Input input = new BuilderJavaAs3Input(clazz, info.getClassFile(), source);
			            	return generator.generate(input);
		            	}
		            } finally {
		                monitor.worked(1);
		            }
	            }
            } catch (Throwable t) {
            	listener.error("", t);
            }
        }
        
        return null;
    }
    
    private Output<?>[] remove(IResource resource, IProgressMonitor monitor) {
        if (resource instanceof IFile && "java".equals(resource.getFileExtension())) {
            try {
                IPath resourcePath = resource.getFullPath();
                IPath resourceSourceFolder = null;
                
	            List<IPath> sourceFolders = ProjectUtil.getSourceFolders(config.getJavaProject());
	            for (IPath sourceFolder : sourceFolders) {
	            	if (sourceFolder.isPrefixOf(resourcePath)) {
	            		resourceSourceFolder = sourceFolder;
	            		break;
	            	}
	            }
	            
	            if (resourceSourceFolder == null)
	            	return null;
            
	            String relativeJavaFile = resourcePath.makeRelativeTo(resourceSourceFolder).toPortableString();
	            Gas3Source source = config.getProperties().getGas3().getMatchingSource(
	            	resourceSourceFolder.makeRelativeTo(config.getJavaProject().getPath()).toPortableString(),
	            	relativeJavaFile
	            );
	            
	            if (source != null) {
	            	
	            	String packageName = "";
	            	String className = relativeJavaFile.substring(0, relativeJavaFile.length() - 5);
	            	
	            	int lastSlash = className.lastIndexOf('/');
	            	if (lastSlash != -1) {
	            		packageName = className.substring(0, lastSlash).replace('/', '.');
	            		PackageTranslator translator =  config.getPackageTranslator(packageName);
	            		if (translator != null)
	            			packageName = translator.translate(packageName);
	            		className = className.substring(lastSlash + 1);
	            	}
	            	
	            	
		            monitor.subTask("Removing AS3 code for: " + resource.getProjectRelativePath().toString());
		            try {
		            	JavaAs3Input input = new BuilderJavaAs3Input(null, null, source);
		            	File outputDir = config.getBaseOutputDir(input);
		            	
		            	String outputPrefix = outputDir.getName() + File.separator + packageName.replace('.', File.separatorChar) + File.separator + className;
		            	final Pattern pattern = Pattern.compile("^.*" + Pattern.quote(outputPrefix) + "(\\$.*)?(Base)?\\.as$");
		            	List<File> matches = listFiles(outputDir, new FileFilter() {
							public boolean accept(File file) {
								return pattern.matcher(file.getPath()).matches();
							}
		            	});
		            	
		            	if (!matches.isEmpty()) {
			            	Output<?>[] outputs = new Output<?>[matches.size()];
			            	for (int i = 0; i < matches.size(); i++) {
			            		File file = matches.get(i);
			            		File renameToFile = new File(file.getParentFile(), file.getName() + "." + System.currentTimeMillis() + ".hid");
			            		outputs[i] = new JavaAs3Output(null, null, renameToFile.getParentFile(), renameToFile, true, Listener.MSG_FILE_REMOVED);
			            		listener.removing(input, outputs[i]);
			            		file.renameTo(renameToFile);
			            	}
			            	
			            	return outputs;
		            	}
		            } finally {
		                monitor.worked(1);
		            }
	            }
	        } catch (Throwable t) {
            	listener.error("", t);
            }
        }
        
        return null;
    }
    
    private List<File> listFiles(File root, FileFilter filter) {
    	final List<File> files = new ArrayList<File>();
    	listFiles(files, root, filter);
    	return files;
    }

    private void listFiles(final List<File> files, final File parent, final FileFilter filter) {
    	parent.listFiles(new FileFilter() {
			public boolean accept(File file) {
				if (file.isDirectory())
					listFiles(files, file, filter);
				if (filter.accept(file))
					files.add(file);
				return false;
			}
    	});
    }
}
