package org.granite.test.builder;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.granite.generator.TemplateUri;
import org.granite.generator.as3.As3TypeFactory;
import org.granite.generator.as3.DefaultEntityFactory;
import org.granite.generator.as3.DefaultRemoteDestinationFactory;
import org.granite.generator.as3.EntityFactory;
import org.granite.generator.as3.JavaAs3GroovyConfiguration;
import org.granite.generator.as3.JavaAs3Input;
import org.granite.generator.as3.PackageTranslator;
import org.granite.generator.as3.RemoteDestinationFactory;
import org.granite.generator.as3.reflect.JavaType.Kind;
import org.granite.generator.gsp.GroovyTemplateFactory;
import org.granite.generator.javafx.DefaultJavaFXTypeFactory;
import org.granite.generator.javafx.template.JavaFXTemplateUris;


public class MockJavaFXGroovyConfiguration implements JavaAs3GroovyConfiguration {
	
	private GroovyTemplateFactory groovyTemplateFactory = new GroovyTemplateFactory();
	private As3TypeFactory as3TypeFactory = new DefaultJavaFXTypeFactory();
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

	private List<PackageTranslator> translators = new ArrayList<PackageTranslator>();
	
	public List<PackageTranslator> getTranslators() {
		return translators;
	}
	public void addTranslator(String java, String client) {
		translators.add(new PackageTranslator(java, client));
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
			return new TemplateUri[] { new TemplateUri(tide ? JavaFXTemplateUris.TIDE_ENTITY_BASE : JavaFXTemplateUris.ENTITY_BASE, true), new TemplateUri(JavaFXTemplateUris.ENTITY, false) };
		case INTERFACE:
        	return new TemplateUri[] { new TemplateUri(JavaFXTemplateUris.INTERFACE, false) };
		case ENUM:
        	return new TemplateUri[] { new TemplateUri(JavaFXTemplateUris.ENUM, false) };
		case BEAN:
        	return new TemplateUri[] { new TemplateUri(tide ? JavaFXTemplateUris.TIDE_BEAN_BASE : JavaFXTemplateUris.BEAN_BASE, true), new TemplateUri(JavaFXTemplateUris.BEAN, false) };
		case REMOTE_DESTINATION:
        	return new TemplateUri[] { new TemplateUri(tide ? JavaFXTemplateUris.TIDE_REMOTE_BASE : JavaFXTemplateUris.REMOTE_BASE, true), new TemplateUri(JavaFXTemplateUris.REMOTE, false) };
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
