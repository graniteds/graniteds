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
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.granite.builder.properties.Gas3Classpath;
import org.granite.builder.properties.Gas3Project;
import org.granite.builder.properties.Gas3Source;
import org.granite.builder.properties.Gas3Translator;
import org.granite.builder.properties.GraniteProperties;
import org.granite.builder.properties.GranitePropertiesLoader;
import org.granite.builder.util.BuilderUtil;
import org.granite.builder.util.JavaClassInfo;
import org.granite.builder.util.ProjectUtil;
import org.granite.builder.util.ProjectUtil.CpEntry;
import org.granite.builder.util.ProjectUtil.CpEntry.CpeKind;
import org.granite.generator.Listener;
import org.granite.generator.TemplateUri;
import org.granite.generator.as3.As3TypeFactory;
import org.granite.generator.as3.DefaultAs3TypeFactory;
import org.granite.generator.as3.DefaultEntityFactory;
import org.granite.generator.as3.EntityFactory;
import org.granite.generator.as3.JavaAs3GroovyConfiguration;
import org.granite.generator.as3.JavaAs3Input;
import org.granite.generator.as3.PackageTranslator;
import org.granite.generator.as3.RemoteDestinationFactory;
import org.granite.generator.as3.reflect.JavaType.Kind;
import org.granite.generator.gsp.GroovyTemplateFactory;

/**
 * @author Franck WOLFF
 */
public class BuilderConfiguration implements JavaAs3GroovyConfiguration {

	private final IProject project;
    private final Map<Class<?>, Boolean> generatedClassCache = new HashMap<Class<?>, Boolean>();
	private final Map<String, BuilderConfiguration> dependentProjectConfigurations = new HashMap<String, BuilderConfiguration>();
    
	private IJavaProject javaProject = null;
    private GraniteProperties properties = null;
    private As3TypeFactory clientTypeFactory = null;
    private EntityFactory entityFactory = null;
    private RemoteDestinationFactory remoteDestinationFactory = null;
    private GroovyTemplateFactory groovyTemplateFactory = null;
    private ClassLoader loader = null;
    private List<PackageTranslator> translators = null;
    private Listener listener = null;
	
	public BuilderConfiguration(Listener listener, IProject project) {
		this.listener = listener;
		this.project = project;
	}

	public boolean isOutdated() {
		return GranitePropertiesLoader.isOutdated(project, getProperties());
	}
	
	public IProject getProject() {
		return project;
	}

	public IJavaProject getJavaProject() {
		if (javaProject == null) {
			javaProject = JavaCore.create(project);
	        if (javaProject == null) {
	        	throw new RuntimeException(
	        		new CoreException(ProjectUtil.createErrorStatus("Not a Java Project: " + project, null))
	            );
	        }
		}
		return javaProject;
	}

	public GraniteProperties getProperties() {
    	if (properties == null) {
    		try {
    			properties = GranitePropertiesLoader.load(project);
    		} catch (IOException e) {
    			throw new RuntimeException(
	                new CoreException(
	                	ProjectUtil.createErrorStatus("Could not load Granite properties for: " + project,
	                	e
	                ))
	            );
            }
    	}
        return properties;
	}
	
	@Override
	public As3TypeFactory getAs3TypeFactory() {
		if (clientTypeFactory == null) {
			String factoryClass = getProperties().getGas3().getAs3TypeFactory();
			if (factoryClass != null) {
				try {
					clientTypeFactory = BuilderUtil.newInstance(As3TypeFactory.class, factoryClass, getClassLoader());
					clientTypeFactory.configure(
						getProperties().getGas3().isExternalizeLong(),
						getProperties().getGas3().isExternalizeBigInteger(),
						getProperties().getGas3().isExternalizeBigDecimal()
					);
				} catch (Exception e) {
					throw new RuntimeException(
		                new CoreException(
		                	ProjectUtil.createErrorStatus("Could not load As3TypeFactory class: " + factoryClass,
		                	e
		                ))
		            );
				}
			}
			else
				clientTypeFactory = new DefaultAs3TypeFactory();
		}
		return clientTypeFactory;
	}
	
	@Override
	public EntityFactory getEntityFactory() {
		if (entityFactory == null) {
			String factoryClass = getProperties().getGas3().getEntityFactory();
			if (factoryClass != null) {
				try {
					entityFactory = BuilderUtil.newInstance(EntityFactory.class, factoryClass, getClassLoader());
				} catch (Exception e) {
					throw new RuntimeException(
		                new CoreException(
		                	ProjectUtil.createErrorStatus("Could not load EntityFactory class: " + factoryClass,
		                	e
		                ))
		            );
				}
			}
			else
				entityFactory = new DefaultEntityFactory();
		}
		return entityFactory;
	}
	
	@Override
	public RemoteDestinationFactory getRemoteDestinationFactory() {
		if (remoteDestinationFactory == null) {
			String factoryClass = getProperties().getGas3().getRemoteDestinationFactory();
			if (factoryClass != null) {
				try {
					remoteDestinationFactory = BuilderUtil.newInstance(RemoteDestinationFactory.class, factoryClass, getClassLoader());
				} catch (Exception e) {
					throw new RuntimeException(
		                new CoreException(
		                	ProjectUtil.createErrorStatus("Could not load RemoteDestinationFactory class: " + factoryClass,
		                	e
		                ))
		            );
				}
			}
		}
		return remoteDestinationFactory;
	}

	@Override
	public File getBaseOutputDir(JavaAs3Input input) {
		BuilderJavaClientInput builderInput = (BuilderJavaClientInput)input;
		return new File(ProjectUtil.getProjectFile(project), builderInput.getGas3Source().getBaseOutputDir(true));
	}

	@Override
	public File getOutputDir(JavaAs3Input input) {
		BuilderJavaClientInput builderInput = (BuilderJavaClientInput)input;
		return new File(ProjectUtil.getProjectFile(project), builderInput.getGas3Source().getOutputDir());
	}

	@Override
	public TemplateUri[] getTemplateUris(Kind kind, Class<?> clazz) {
		return getProperties().getGas3().getMatchingTemplateUris(kind);
	}

	@Override
	public List<PackageTranslator> getTranslators() {
		if (translators == null) {
			if (getProperties().getGas3().getTranslators().isEmpty())
				translators = Collections.emptyList();
			else {
				translators = new ArrayList<PackageTranslator>(getProperties().getGas3().getTranslators().size());
				for (Gas3Translator translator : getProperties().getGas3().getTranslators())
					translators.add(translator.getPackageTranslator());
			}
		}
		return translators;
	}
	
	public PackageTranslator getPackageTranslator(String packageName) {
        PackageTranslator translator = null;

        int weight = 0;
        for (PackageTranslator t : getTranslators()) {
            int w = t.match(packageName);
            if (w > weight) {
                weight = w;
                translator = t;
            }
        }
		
        return translator;
	}

	@Override
	public String getUid() {
		return getProperties().getGas3().getUid();
	}

	@Override
	public boolean isGenerated(Class<?> clazz) {
		if (!getClassLoader().equals(clazz.getClassLoader()) || (clazz.isMemberClass() && !clazz.isEnum()))
			return false;
		
		Boolean generated = generatedClassCache.get(clazz);
		if (generated == null) {
			generated = Boolean.FALSE;
			
			JavaClassInfo info = ProjectUtil.getJavaClassInfo(getJavaProject(), clazz);
			if (info != null) {
				Gas3Source source = getProperties().getGas3().getMatchingSource(
		        	info.getSourceFolderPath(),
		        	info.getSourceFilePath()
		        );
				generated = Boolean.valueOf(source != null);
			}
			else {
				for (Gas3Project gas3Project : getProperties().getGas3().getProjects()) {
					IProject dependentProject = ProjectUtil.getProject(getJavaProject().getProject(), gas3Project.getPath());
					try {
						if (ProjectUtil.isGraniteProject(dependentProject)) {
							BuilderConfiguration configuration = dependentProjectConfigurations.get(dependentProject.getName());
							if (configuration == null) {
								configuration = new BuilderConfiguration(listener, dependentProject);
								dependentProjectConfigurations.put(dependentProject.getName(), configuration);
							}
							info = ProjectUtil.getJavaClassInfo(configuration.getJavaProject(), clazz);
							if (info != null) {
								Gas3Source source = configuration.getProperties().getGas3().getMatchingSource(
						        	info.getSourceFolderPath(),
						        	info.getSourceFilePath()
						        );
								generated = Boolean.valueOf(source != null);
							}
						}
					} catch (Exception e) {
						// ignore???
					}
				}
			}
			generatedClassCache.put(clazz, generated);
		}
		
		return generated.booleanValue();
	}

	@Override
	public GroovyTemplateFactory getGroovyTemplateFactory() {
		if (groovyTemplateFactory == null)
			groovyTemplateFactory = new GroovyTemplateFactory();
		return groovyTemplateFactory;
	}

	public void resetClassLoader() {
		generatedClassCache.clear();
		dependentProjectConfigurations.clear();
		loader = null;
	}
	
	@Override
	public ClassLoader getClassLoader() {
		if (loader == null) {
			try {
				List<URL> classpath = new ArrayList<URL>();
				for (CpEntry entry : ProjectUtil.getFullResolvedClasspath(getJavaProject())) {
					if (entry.getKind() == CpeKind.CONTAINER_JAR) {
						for (CpEntry cEntry : entry.getChildren())
							addToClasspath(classpath, cEntry.toURL());
					}
					else
						addToClasspath(classpath, entry.toURL());
				}
				
				for (Gas3Classpath gas3Classpath : getProperties().getGas3().getClasspaths()) {
					File file = new File(gas3Classpath.getPath());
					if (file.exists()) {
						try {
							addToClasspath(classpath, file.toURI().toURL());
						} catch (MalformedURLException e) {
							// Should never happen...
						}
					}
				}
				
				for (Gas3Project gas3Project : getProperties().getGas3().getProjects()) {
					IProject dependentProject = ProjectUtil.getProject(getJavaProject().getProject(), gas3Project.getPath());
					if (ProjectUtil.isGraniteProject(dependentProject)) {
						for (CpEntry entry : ProjectUtil.getFullResolvedClasspath(JavaCore.create(dependentProject))) {
							if (entry.getKind() == CpeKind.CONTAINER_JAR) {
								for (CpEntry cEntry : entry.getChildren())
									addToClasspath(classpath, cEntry.toURL());
							}
							else
								addToClasspath(classpath, entry.toURL());
						}
					}
				}
				
				if (getProperties().getGas3().isDebugEnabled()) {
					listener.debug("Using classpath: {");
					for (URL url : classpath)
						listener.debug("  " + (url != null ? url.toString() : "<null>"));
					listener.debug("}");
				}
				
				loader = URLClassLoader.newInstance(
					classpath.toArray(new URL[classpath.size()]),
					new BuilderParentClassLoader()
				);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return loader;
	}
	
	private void addToClasspath(List<URL> classpath, URL url) {
		if (!classpath.contains(url))
			classpath.add(url);
	}

	@Override
	public File getWorkingDirectory() {
		return ProjectUtil.getProjectFile(project);
	}
}
