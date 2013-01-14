package org.granite.test.builder;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.granite.generator.TemplateUri;
import org.granite.generator.as3.As3TypeFactory;
import org.granite.generator.as3.DefaultAs3TypeFactory;
import org.granite.generator.as3.DefaultEntityFactory;
import org.granite.generator.as3.DefaultRemoteDestinationFactory;
import org.granite.generator.as3.EntityFactory;
import org.granite.generator.as3.JavaAs3GroovyConfiguration;
import org.granite.generator.as3.JavaAs3Input;
import org.granite.generator.as3.PackageTranslator;
import org.granite.generator.as3.RemoteDestinationFactory;
import org.granite.generator.as3.reflect.JavaType.Kind;
import org.granite.generator.gsp.GroovyTemplateFactory;
import org.granite.generator.template.StandardTemplateUris;

public class MockJavaAs3GroovyConfiguration implements JavaAs3GroovyConfiguration {
	
	private GroovyTemplateFactory groovyTemplateFactory = new GroovyTemplateFactory();
	private As3TypeFactory as3TypeFactory = new DefaultAs3TypeFactory();
	private EntityFactory entityFactory = new DefaultEntityFactory();
	private RemoteDestinationFactory remoteDestinationFactory = new DefaultRemoteDestinationFactory();
	private boolean tide = false;
	private Set<Class<?>> fileSetClasses = new HashSet<Class<?>>();
	
	
	public void setTide(boolean tide) {
		this.tide = tide;
	}
	
	public GroovyTemplateFactory getGroovyTemplateFactory() {
		return groovyTemplateFactory;
	}

	public ClassLoader getClassLoader() {
		return getClass().getClassLoader();
	}

	public String getUid() {
		return "uid";
	}

	public boolean isGenerated(Class<?> clazz) {
		return fileSetClasses.contains(clazz);
	}
	
	public void addFileSetClasses(Class<?>... classes) {
		for (Class<?> clazz : classes)
			fileSetClasses.add(clazz);
	}

	public As3TypeFactory getAs3TypeFactory() {
		return as3TypeFactory;
	}

	public List<PackageTranslator> getTranslators() {
		return Collections.emptyList();
	}

	public PackageTranslator getPackageTranslator(String packageName) {
		return null;
	}

	public EntityFactory getEntityFactory() {
		return entityFactory;
	}

	public RemoteDestinationFactory getRemoteDestinationFactory() {
		return remoteDestinationFactory;
	}

	public TemplateUri[] getTemplateUris(Kind kind, Class<?> clazz) {
		switch (kind) {
		case ENTITY:
			return new TemplateUri[] { new TemplateUri(tide ? StandardTemplateUris.TIDE_ENTITY_BASE : StandardTemplateUris.ENTITY_BASE, true), new TemplateUri(StandardTemplateUris.ENTITY, false) };
		case INTERFACE:
        	return new TemplateUri[] { new TemplateUri(StandardTemplateUris.INTERFACE, false) };
		case ENUM:
        	return new TemplateUri[] { new TemplateUri(StandardTemplateUris.ENUM, false) };
		case BEAN:
        	return new TemplateUri[] { new TemplateUri(tide ? StandardTemplateUris.TIDE_BEAN_BASE : StandardTemplateUris.BEAN_BASE, true), new TemplateUri(StandardTemplateUris.BEAN, false) };
		case REMOTE_DESTINATION:
        	return new TemplateUri[] { new TemplateUri(tide ? StandardTemplateUris.TIDE_REMOTE_BASE : StandardTemplateUris.REMOTE_BASE, true), new TemplateUri(StandardTemplateUris.REMOTE, false) };
		default:
			throw new IllegalArgumentException("Unknown template kind: " + kind + " / " + clazz);
		}
	}

	@Override
	public File getOutputDir(JavaAs3Input input) {
		return new File(System.getProperty("java.io.tmpdir"));
	}

	@Override
	public File getBaseOutputDir(JavaAs3Input input) {
		return new File(System.getProperty("java.io.tmpdir"));
	}

	@Override
	public File getWorkingDirectory() {
		return new File(System.getProperty("java.io.tmpdir"));
	}
}
