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

package org.granite.generator.as3;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.granite.generator.Generator;
import org.granite.generator.Input;
import org.granite.generator.Listener;
import org.granite.generator.TemplateUri;
import org.granite.generator.as3.reflect.JavaAbstractType.GenerationType;
import org.granite.generator.as3.reflect.JavaBean;
import org.granite.generator.as3.reflect.JavaEnum;
import org.granite.generator.as3.reflect.JavaFieldProperty;
import org.granite.generator.as3.reflect.JavaImport;
import org.granite.generator.as3.reflect.JavaInterface;
import org.granite.generator.as3.reflect.JavaProperty;
import org.granite.generator.as3.reflect.JavaRemoteDestination;
import org.granite.generator.as3.reflect.JavaType;
import org.granite.generator.as3.reflect.JavaType.Kind;
import org.granite.generator.as3.reflect.JavaTypeFactory;
import org.granite.generator.exception.TemplateException;
import org.granite.generator.exception.TemplateUriException;
import org.granite.generator.gsp.AbstractGroovyTransformer;
import org.granite.generator.gsp.GroovyTemplate;
import org.granite.util.ClassUtil;

/**
 * @author Franck WOLFF
 */
public class JavaAs3GroovyTransformer
	extends AbstractGroovyTransformer<JavaAs3Input, JavaAs3Output, JavaAs3GroovyConfiguration>
	implements JavaTypeFactory {

	private static final String GENERATED_FILE_EXTENSION = "as";
	private static final String GENERATED_BASE_SUFFIX = "Base";
	
	protected final Map<Class<?>, JavaType> javaTypes = new HashMap<Class<?>, JavaType>();
    protected final Map<Class<?>, JavaImport> javaImports = new HashMap<Class<?>, JavaImport>();

	public JavaAs3GroovyTransformer() {
	}

	public JavaAs3GroovyTransformer(JavaAs3GroovyConfiguration config, Listener listener) {
		super(config, listener);
	}

	@Override
	public boolean accept(Input<?> input) {
		return (input instanceof JavaAs3Input);
	}

	@Override
	protected JavaAs3Output[] getOutputs(JavaAs3Input input) throws IOException, TemplateUriException {
		JavaType javaType = getJavaType(input.getType());
		input.setJavaType(javaType);
		TemplateUri[] templateUris = getTemplateUris(javaType);
		boolean hasBaseTemplate = templateUris.length > 1;
		
		JavaAs3Output[] outputs = new JavaAs3Output[templateUris.length];
		
		for (int i = 0; i < templateUris.length; i++) {
			GroovyTemplate template = getTemplate(templateUris[i]);
			File dir = getOutputDir(input, template);
			File file = getOutputFile(input, template, dir);
			boolean outdated = isOutdated(input, template, file, hasBaseTemplate);
			String status = getOutputStatus(input, template, file, hasBaseTemplate);
			
			outputs[i] = new JavaAs3Output(
				javaType,
				template,
				dir,
				file,
				outdated,
				status
			);
		}
		
		return outputs;
	}

	@Override
	protected void generate(JavaAs3Input input, JavaAs3Output output) throws IOException, TemplateException {
		Map<String, Object> bindings = getBindings(input, output);
		
		// Write in memory (step 1).
		PublicByteArrayOutputStream pbaos = new PublicByteArrayOutputStream(8192);
		output.getTemplate().execute(bindings, new PrintWriter(new OutputStreamWriter(pbaos, "UTF-8")));
		
		// If no exceptions were raised, write to file (step 2).
		OutputStream stream = null;
		try {
			stream = output.openStream();
			stream.write(pbaos.getBytes(), 0, pbaos.size());
		} finally {
			if (stream != null)
				stream.close();
		}
	}

    protected Map<String, Object> getBindings(JavaAs3Input input, JavaAs3Output output) {
        Map<String, Object> bindings = new HashMap<String, Object>();
        bindings.put("gVersion", Generator.VERSION);
        bindings.put("jClass", input.getJavaType());
        bindings.put("fAttributes", input.getAttributes());
        return bindings;
    }

    protected TemplateUri[] getTemplateUris(JavaType javaType) {
        return getConfig().getTemplateUris(getKind(javaType.getType()), javaType.getClass());
    }
    
    protected File getOutputDir(JavaAs3Input input, GroovyTemplate template) {
    	return (template.isBase() ? getConfig().getBaseOutputDir(input) : getConfig().getOutputDir(input));
    }

    protected File getOutputFile(JavaAs3Input input, GroovyTemplate template, File outputDir) {
    	As3Type as3Type = input.getJavaType().getAs3Type();

        StringBuilder sb = new StringBuilder()
            .append(outputDir.getAbsolutePath())
            .append(File.separatorChar)
            .append(as3Type.getQualifiedName().replace('.', File.separatorChar))
            .append(getOutputFileSuffix(input, template))
            .append('.')
            .append(GENERATED_FILE_EXTENSION);

        return new File(sb.toString());
    }
    
    protected String getOutputFileSuffix(JavaAs3Input input, GroovyTemplate template) {
    	return template.isBase() ? GENERATED_BASE_SUFFIX : "";
    }
    
    protected boolean isOutdated(JavaAs3Input input, GroovyTemplate template, File outputFile, boolean hasBaseTemplate) {
    	if (!outputFile.exists())
    		return true;
    	if (outputFile.lastModified() > System.currentTimeMillis()) {
    		getListener().warn(
				outputFile.getAbsolutePath() +
				" has a last modified time in the future: " +
				DateFormat.getInstance().format(new Date(outputFile.lastModified()))
    		);
    	}
    	if (!template.isBase() && hasBaseTemplate)
    		return false;
    	return input.getFile().lastModified() > outputFile.lastModified();
    }
    
    protected String getOutputStatus(JavaAs3Input input, GroovyTemplate template, File outputFile, boolean hasBaseTemplate) {
    	if (!outputFile.exists())
    		return Listener.MSG_FILE_NOT_EXISTS;
    	if (!template.isBase() && hasBaseTemplate)
    		return Listener.MSG_FILE_EXISTS_NO_OVER;
    	if (input.getFile().lastModified() > outputFile.lastModified())
    		return Listener.MSG_FILE_OUTDATED;
    	return Listener.MSG_FILE_UPTODATE;
    }

	public As3Type getAs3Type(Class<?> clazz) {
        As3Type as3Type = getConfig().getAs3TypeFactory().getAs3Type(clazz);
        if (getConfig().getTranslators().isEmpty() || clazz.getPackage() == null)
            return as3Type;

        String packageName = clazz.getPackage().getName();
        PackageTranslator translator = getConfig().getPackageTranslator(packageName);

        if (translator != null)
            as3Type = new As3Type(translator.translate(packageName), as3Type.getName());

        return as3Type;
	}

	public JavaImport getJavaImport(Class<?> clazz) {
        JavaImport javaImport = javaImports.get(clazz);
        if (javaImport == null) {
            URL url = ClassUtil.findResource(clazz);
            javaImport = new JavaImport(this, clazz, url);
            javaImports.put(clazz, javaImport);
        }
        return javaImport;
	}

	public JavaType getJavaType(Class<?> clazz) {
		JavaType javaType = javaTypes.get(clazz);
		if (javaType == null && getConfig().isGenerated(clazz)) {
			URL url = ClassUtil.findResource(clazz);
			Kind kind = getKind(clazz);
			switch (kind) {
			case ENUM:
	            javaType = new JavaEnum(this, clazz, url);
	            break;
			case REMOTE_DESTINATION:
				if (getConfig().getRemoteDestinationFactory() != null)
					javaType = getConfig().getRemoteDestinationFactory().newRemoteDestination(this, clazz, url);
				else
					throw new RuntimeException("Remote destination could not be handled for " + clazz);
				break;
			case INTERFACE:
	            javaType = new JavaInterface(this, clazz, url);
	            break;
			case ENTITY:
				javaType = getConfig().getEntityFactory().newEntity(this, clazz, url);
	            break;
			case BEAN:
	            javaType = new JavaBean(this, clazz, url);
	            break;
	        default:
	        	throw new RuntimeException("Uknown class kind: " + kind);
			}
	        javaTypes.put(clazz, javaType);
		}
		return javaType;
	}
	
	public Kind getKind(Class<?> clazz) {
        if (clazz.isEnum() || Enum.class.getName().equals(clazz.getName()))
            return Kind.ENUM;
        if (getConfig().getRemoteDestinationFactory() != null) {
        	if (getConfig().getRemoteDestinationFactory().isRemoteDestination(clazz))
        		return Kind.REMOTE_DESTINATION;
        }
        if (clazz.isInterface())
            return Kind.INTERFACE;
        if (getConfig().getEntityFactory().isEntity(clazz))
       		return Kind.ENTITY;
        return Kind.BEAN;
	}
	
	protected GenerationType getGenerationType(Class<?> clazz) {
		return getGenerationType(getKind(clazz), clazz);
	}
	public GenerationType getGenerationType(Kind kind, Class<?> clazz) {
		if (!getConfig().isGenerated(clazz))
			return GenerationType.NOT_GENERATED;
		TemplateUri[] uris = getConfig().getTemplateUris(kind, clazz);
		if (uris == null || uris.length == 0)
			return GenerationType.NOT_GENERATED;
		return uris.length == 1 ? GenerationType.GENERATED_SINGLE : GenerationType.GENERATED_WITH_BASE;
	}

	public List<JavaInterface> getJavaTypeInterfaces(Class<?> clazz) {
        List<JavaInterface> interfazes = new ArrayList<JavaInterface>();
        for (Class<?> interfaze : clazz.getInterfaces()) {
            if (getConfig().isGenerated(interfaze)) {
            	JavaType javaType = getJavaType(interfaze);
            	if (javaType instanceof JavaRemoteDestination)
            		javaType = ((JavaRemoteDestination)javaType).convertToJavaInterface();
                interfazes.add((JavaInterface)javaType);
            }
        }
        return interfazes;
	}

	public JavaType getJavaTypeSuperclass(Class<?> clazz) {
        Class<?> superclass = clazz.getSuperclass();
        if (superclass != null && getConfig().isGenerated(superclass))
            return getJavaType(superclass);
        return null;
	}

	public boolean isId(JavaFieldProperty fieldProperty) {
		return getConfig().getEntityFactory().isId(fieldProperty);
	}

	public boolean isUid(JavaProperty property) {
    	return getConfig().getUid() == null
			? "uid".equals(property.getName())
			: getConfig().getUid().equals(property.getName());
	}
	
	public boolean isVersion(JavaProperty property) {
		return getConfig().getEntityFactory().isVersion(property);
	}
	
	public boolean isLazy(JavaProperty property) {
		return getConfig().getEntityFactory().isLazy(property);
	}
	
	static class PublicByteArrayOutputStream extends ByteArrayOutputStream {
		public PublicByteArrayOutputStream() {
		}
		public PublicByteArrayOutputStream(int size) {
			super(size);
		}
		public byte[] getBytes() {
			return buf; // no copy...
		}
	}
}
