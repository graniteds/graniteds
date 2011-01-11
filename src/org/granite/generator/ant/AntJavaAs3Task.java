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

package org.granite.generator.ant;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;
import org.granite.generator.Generator;
import org.granite.generator.Output;
import org.granite.generator.TemplateUri;
import org.granite.generator.Transformer;
import org.granite.generator.as3.As3TypeFactory;
import org.granite.generator.as3.DefaultAs3TypeFactory;
import org.granite.generator.as3.DefaultRemoteDestinationFactory;
import org.granite.generator.as3.EntityFactory;
import org.granite.generator.as3.DefaultEntityFactory;
import org.granite.generator.as3.JavaAs3GroovyConfiguration;
import org.granite.generator.as3.JavaAs3GroovyTransformer;
import org.granite.generator.as3.JavaAs3Input;
import org.granite.generator.as3.PackageTranslator;
import org.granite.generator.as3.RemoteDestinationFactory;
import org.granite.generator.as3.reflect.JavaType.Kind;
import org.granite.generator.gsp.GroovyTemplateFactory;
import org.granite.generator.template.StandardTemplateUris;

/**
 * @author Franck WOLFF
 */
public class AntJavaAs3Task extends Task implements JavaAs3GroovyConfiguration {

    ///////////////////////////////////////////////////////////////////////////
    // Configurable fields (xml attributes).

    private String outputdir = ".";
    private String baseoutputdir = null;

    private String uid = "uid";

    private String entitytemplate = null;
    private String entitybasetemplate = null;

    private String beantemplate = null;
    private String beanbasetemplate = null;

    private String interfacetemplate = null;

    private String enumtemplate = null;

    private String remotetemplate = null;
    private String remotebasetemplate = null;

    private boolean tide = false;

    private String as3typefactory = null;
    private String entityfactory = null;
    private String remotedestinationfactory = null;
    private String transformer = null;

    private boolean externalizelong = false;
    private boolean externalizebiginteger = false;
    private boolean externalizebigdecimal = false;
    
    private Path classpath = null;
    private List<FileSet> fileSets = new ArrayList<FileSet>();

    private List<PackageTranslator> translators = new ArrayList<PackageTranslator>();

    ///////////////////////////////////////////////////////////////////////////
    // Configuration implementation fields.

    private File outputDirFile = null;
    private File baseOutputDirFile = null;
    
    private As3TypeFactory as3TypeFactoryImpl = null;
    private Transformer<?, ?, ?> transformerImpl = null;
    private EntityFactory entityFactoryImpl = null;
    private RemoteDestinationFactory remoteDestinationFactoryImpl = null;
    
    private GroovyTemplateFactory groovyTemplateFactory = null;
    
    private TemplateUri[] entityTemplateUris = null;
    private TemplateUri[] interfaceTemplateUris = null;
    private TemplateUri[] beanTemplateUris = null;
    private TemplateUri[] enumTemplateUris = null;
    private TemplateUri[] remoteTemplateUris = null;
    
    private Map<Class<?>, File> filesetClasses = null;
    
    ///////////////////////////////////////////////////////////////////////////
    // Task attributes.

    public void setOutputdir(String outputdir) {
        this.outputdir = outputdir;
    }

    public void setBaseoutputdir(String baseoutputdir) {
		this.baseoutputdir = baseoutputdir;
	}

	public void setAs3typefactory(String as3typefactory) {
        this.as3typefactory = as3typefactory;
    }

	public void setEntityfactory(String entityfactory) {
        this.entityfactory = entityfactory;
    }

	public void setRemotedestinationfactory(String remotedestinationfactory) {
        this.remotedestinationfactory = remotedestinationfactory;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setEntitytemplate(String entitytemplate) {
    
    	this.entitytemplate = entitytemplate;
    }
    public void setEntitybasetemplate(String entitybasetemplate) {
        this.entitybasetemplate = entitybasetemplate;
    }

    public void setBeantemplate(String beantemplate) {
        this.beantemplate = beantemplate;
    }
    public void setBeanbasetemplate(String beanbasetemplate) {
        this.beanbasetemplate = beanbasetemplate;
    }

    public void setInterfacetemplate(String interfacetemplate) {
        this.interfacetemplate = interfacetemplate;
    }

    public void setEnumtemplate(String enumtemplate) {
        this.enumtemplate = enumtemplate;
    }

    public void setRemotetemplate(String remotetemplate) {
        this.remotetemplate = remotetemplate;
    }
    public void setRemotebasetemplate(String remotebasetemplate) {
        this.remotebasetemplate = remotebasetemplate;
    }

    public void setTide(boolean tide) {
        this.tide = tide;
    }

    public void setExternalizelong(boolean externalizelong) {
		this.externalizelong = externalizelong;
	}

    public void setExternalizebiginteger(boolean externalizebiginteger) {
		this.externalizebiginteger = externalizebiginteger;
	}

	public void setExternalizebigdecimal(boolean externalizebigdecimal) {
		this.externalizebigdecimal = externalizebigdecimal;
	}

	public void setTransformer(String transformer) {
		this.transformer = transformer;
	}

    ///////////////////////////////////////////////////////////////////////////
    // Task inner elements.

	public void addFileset(FileSet fileSet) {
        fileSets.add(fileSet);
    }

    public void setClasspath(Path path) {
        if (classpath == null)
            classpath = path;
        else
            classpath.append(path);
    }

    public Path createClasspath() {
        if (classpath == null)
            classpath = new Path(getProject());
        return classpath.createPath();
    }

    public void setClasspathRef(Reference r) {
        createClasspath().setRefid(r);
    }

    public void addTranslator(PackageTranslator translator) {
        translators.add(translator);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Task execution.

    @Override
    public void execute() throws BuildException {

        log("Using output dir: " + outputdir, Project.MSG_INFO);
        log("Using classpath: " + classpath, Project.MSG_INFO);

        AntClassLoader loader = new AntClassLoader(AntJavaAs3Task.class.getClassLoader(), getProject(), classpath, true);
        try {
            loader.setThreadContextLoader();

            // Build a Set of all ".class" files in filesets (ignoring nested classes).
            log("Loading all Java classes referenced by inner fileset(s) {", Project.MSG_INFO);

            filesetClasses = new HashMap<Class<?>, File>();
            for (FileSet fileSet : fileSets) {
                DirectoryScanner scanner = fileSet.getDirectoryScanner(getProject());
                scanner.setCaseSensitive(true);
                scanner.scan();

                StringBuilder sb = new StringBuilder("    ");
                String[] names = scanner.getIncludedFiles();
                for (String name : names) {
                    if (name.endsWith(".class")) {
                        log(name, Project.MSG_VERBOSE);
                        try {
                            File jFile = new File(scanner.getBasedir(), name);
                            if (!jFile.exists())
                                throw new FileNotFoundException(jFile.toString());

                            String jClassName = name.substring(0, name.length() - 6).replace(File.separatorChar, '.');
                            Class<?> jClass = loader.loadClass(jClassName);
                            
                            if (!jClass.isMemberClass() || jClass.isEnum()) {
	                            sb.setLength(4);
	                            sb.append(jClass.toString());
	                            log(sb.toString(), Project.MSG_INFO);
	
	                            filesetClasses.put(jClass, jFile);
                            }
                        } catch (Exception e) {
                            log(getStackTrace(e));
                            throw new BuildException("Could not load Java class file: " + name, e);
                        }
                    }
                    else
                        log("Skipping non class file: " + name, Project.MSG_WARN);
                }
            }
            log("}", Project.MSG_INFO);

            log("Setting up the generator...", Project.MSG_INFO);

            // As3TypeFactory.
            if (as3typefactory == null) {
            	as3TypeFactoryImpl = new DefaultAs3TypeFactory();
            	as3TypeFactoryImpl.configure(externalizelong, externalizebiginteger, externalizebigdecimal);
            }
            else {
                log("Instantiating custom As3TypeFactory class: " + as3typefactory, Project.MSG_INFO);
                as3TypeFactoryImpl = newInstance(loader, as3typefactory);
            }
            
            // EntityFactory
            if (entityfactory == null)
            	entityFactoryImpl = new DefaultEntityFactory();
            else {
                log("Instantiating custom EntityFactory class: " + entityfactory, Project.MSG_INFO);
                entityFactoryImpl = newInstance(loader, entityfactory);
            }
            
            // RemoteDestinationFactory
            if (remotedestinationfactory == null)
            	remoteDestinationFactoryImpl = new DefaultRemoteDestinationFactory();
            else {
                log("Instantiating custom RemoteDestinationFactory class: " + remotedestinationfactory, Project.MSG_INFO);
                remoteDestinationFactoryImpl = newInstance(loader, remotedestinationfactory);
            }

            // Listener.
            AntListener listener = new AntListener(this);

            // Transformer.
            if (transformer == null)
            	transformerImpl = new JavaAs3GroovyTransformer();
            else {
                log("Instantiating custom Transformer class: " + transformer, Project.MSG_INFO);
                transformerImpl = newInstance(loader, transformer);
            }
            transformerImpl.setListener(listener);
            
            // Enum templates.
            String baseTemplateUri = null;
            String templateUri = StandardTemplateUris.ENUM;
            if (enumtemplate != null) {
            	log("Using custom enum template: " + enumtemplate, Project.MSG_INFO);
            	templateUri = enumtemplate;
            }
            enumTemplateUris = createTemplateUris(baseTemplateUri, templateUri);
            
            // Interface templates.
            templateUri = StandardTemplateUris.INTERFACE;
            if (interfacetemplate != null) {
            	log("Using custom interface template: " + interfacetemplate, Project.MSG_INFO);
            	templateUri = interfacetemplate;
            }
            interfaceTemplateUris = createTemplateUris(baseTemplateUri, templateUri);
            
            // Entity templates.
            baseTemplateUri = StandardTemplateUris.ENTITY_BASE;
            templateUri = StandardTemplateUris.ENTITY;
            if (entitytemplate != null) {
            	log("Using custom entity template: " + entitytemplate, Project.MSG_INFO);
            	templateUri = entitytemplate;
            }
            if (entitybasetemplate != null) {
            	log("Using custom entity base template: " + entitybasetemplate, Project.MSG_INFO);
            	baseTemplateUri = entitybasetemplate;
            }
            else if (tide) {
            	log("Using tide entity base template.", Project.MSG_INFO);
            	baseTemplateUri = StandardTemplateUris.TIDE_ENTITY_BASE;
            }
            entityTemplateUris = createTemplateUris(baseTemplateUri, templateUri);
            
            // Other bean templates.
            baseTemplateUri = StandardTemplateUris.BEAN_BASE;
            templateUri = StandardTemplateUris.BEAN;
            if (beantemplate != null) {
            	log("Using custom bean template: " + beantemplate, Project.MSG_INFO);
            	templateUri = beantemplate;
            }
            if (beanbasetemplate != null) {
            	log("Using custom bean base template: " + beanbasetemplate, Project.MSG_INFO);
            	baseTemplateUri = beanbasetemplate;
            }
        	beanTemplateUris = createTemplateUris(baseTemplateUri, templateUri);
            
        	// Remote service templates.
            baseTemplateUri = StandardTemplateUris.REMOTE_BASE;
            templateUri = StandardTemplateUris.REMOTE;
            if (remotetemplate != null) {
            	log("Using custom remote template: " + remotetemplate, Project.MSG_INFO);
            	templateUri = remotetemplate;
            }
            if (remotebasetemplate != null) {
            	log("Using custom remote base template: " + remotebasetemplate, Project.MSG_INFO);
            	baseTemplateUri = remotebasetemplate;
            }
            else if (tide) {
            	log("Using tide remote destination base template.", Project.MSG_INFO);
            	baseTemplateUri = StandardTemplateUris.TIDE_REMOTE_BASE;
            }
        	remoteTemplateUris = createTemplateUris(baseTemplateUri, templateUri);
        	
        	// Create the generator.
            Generator generator = new Generator(this);
            generator.add(transformerImpl);
            
            // Call the generator for each ".class".
            log("Calling the generator for each Java class {", Project.MSG_INFO);
            int count = 0;
            for (Map.Entry<Class<?>, File> classFile : filesetClasses.entrySet()) {
            	if (classFile.getKey().isAnonymousClass())
            		continue;
                try {
                	JavaAs3Input input = new JavaAs3Input(classFile.getKey(), classFile.getValue());
                    for (Output<?> output : generator.generate(input)) {
                    	if (output.isOutdated())
                    		count++;
                    }
                } catch (Exception e) {
                    log(getStackTrace(e));
                    throw new BuildException("Could not generate AS3 beans for: " + classFile.getKey(), e);
                }
            }

            log("}", Project.MSG_INFO);
            log("Files affected: " + count +  (count == 0 ? " (nothing to do)." : "."));
        } finally {
            loader.resetThreadContextLoader();
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Configuration implementation methods.
    
    public As3TypeFactory getAs3TypeFactory() {
		return as3TypeFactoryImpl;
	}
    
    public EntityFactory getEntityFactory() {
    	return entityFactoryImpl;
    }
    
    public RemoteDestinationFactory getRemoteDestinationFactory() {
    	return remoteDestinationFactoryImpl;
    }

	public File getBaseOutputDir(JavaAs3Input input) {
		if (baseOutputDirFile == null)
			baseOutputDirFile = new File(baseoutputdir != null ? baseoutputdir : outputdir);
		return baseOutputDirFile;
	}

	public File getOutputDir(JavaAs3Input input) {
		if (outputDirFile == null)
			outputDirFile = new File(outputdir);
		return outputDirFile;
	}

	public TemplateUri[] getTemplateUris(Kind kind, Class<?> clazz) {
		switch (kind) {
		case ENTITY:
        	return entityTemplateUris;
		case INTERFACE:
			return interfaceTemplateUris;
		case ENUM:
			return enumTemplateUris;
		case BEAN:
			return beanTemplateUris;
		case REMOTE_DESTINATION:
			return remoteTemplateUris;
		default:
			throw new IllegalArgumentException("Unknown template kind: " + kind + " / " + clazz);
		}
	}

	public List<PackageTranslator> getTranslators() {
		return translators;
	}

	public String getUid() {
		return uid;
	}

	public boolean isGenerated(Class<?> clazz) {
		return filesetClasses.containsKey(clazz);
	}

	public ClassLoader getClassLoader() {
		return Thread.currentThread().getContextClassLoader();
	}

	public GroovyTemplateFactory getGroovyTemplateFactory() {
		if (groovyTemplateFactory == null)
			groovyTemplateFactory = new GroovyTemplateFactory();
		return groovyTemplateFactory;
	}

    ///////////////////////////////////////////////////////////////////////////
    // Utilities.

	@SuppressWarnings("unchecked")
	private <T> T newInstance(ClassLoader loader, String className) {
		try {
            return (T)loader.loadClass(className).newInstance();
        } catch (Exception e) {
            log(getStackTrace(e));
            throw new BuildException("Could not instantiate custom class: " + className, e);
        }
    }

    private static String getStackTrace(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }
	
	private TemplateUri[] createTemplateUris(String baseUri, String uri) {
		TemplateUri[] templateUris = new TemplateUri[baseUri == null ? 1 : 2];
		int i = 0;
		if (baseUri != null)
			templateUris[i++] = new TemplateUri(baseUri, true);
		templateUris[i] = new TemplateUri(uri, false);
		return templateUris;
	}
}
