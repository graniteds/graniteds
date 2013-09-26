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

package org.granite.builder.properties;

import org.granite.builder.util.XStreamUtil;
import org.granite.generator.as3.DefaultAs3TypeFactory;
import org.granite.generator.as3.DefaultEntityFactory;
import org.granite.generator.as3.DefaultRemoteDestinationFactory;
import org.granite.generator.as3.JavaAs3GroovyTransformer;
import org.granite.generator.as3.reflect.JavaType.Kind;
import org.granite.generator.template.StandardTemplateUris;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * @author Franck WOLFF
 */
@XStreamAlias(value="graniteProperties")
public class GraniteProperties implements Validable {

	private transient long timestamp = -1L;
	
	public static final String VERSION_1_0 = "1.0";
	public static final String VERSION_2_0 = "2.0";
	public static final String CURRENT_VERSION = VERSION_2_0;
	
	@XStreamAsAttribute
	private String version = VERSION_2_0;
	
	private Gas3 gas3;
	
	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public Gas3 getGas3() {
		return gas3;
	}

	public void setGas3(Gas3 gas3) {
		this.gas3 = gas3;
	}
	
	@Override
	public void validate(ValidationResults results) {
		if (gas3 != null)
			gas3.validate(results);
		
		if (!CURRENT_VERSION.equals(version)) {
			if (VERSION_1_0.equals(version)) {
				Gas3Template template = gas3.getTemplate(Kind.INTERFACE);
				gas3.getTemplates().remove(template);
				gas3.getTemplates().add(new Gas3Template(Kind.INTERFACE, StandardTemplateUris.INTERFACE));
				results.getWarnings().add("Base template for interfaces is deprecated (ignored)");
			}
			else
				results.getWarnings().add("Unknown graniteProperties version: " + version);
		}
		
		if (gas3.getTemplate(Kind.REMOTE_DESTINATION) == null) {
			StringBuilder uris = new StringBuilder(StandardTemplateUris.REMOTE);
			uris.append(';');
			if (gas3.getTemplate(Kind.ENTITY).getUris().endsWith(StandardTemplateUris.TIDE_ENTITY_BASE))
				uris.append(StandardTemplateUris.TIDE_REMOTE_BASE);
			else
				uris.append(StandardTemplateUris.REMOTE_BASE);
			gas3.getTemplates().add(new Gas3Template(Kind.REMOTE_DESTINATION, uris.toString()));
		}
		
		if (gas3.getEntityFactory() == null)
			gas3.setEntityFactory(DefaultEntityFactory.class.getName());
		
		if (gas3.getRemoteDestinationFactory() == null)
			gas3.setRemoteDestinationFactory(DefaultRemoteDestinationFactory.class.getName());
	}
	
	@Override
	public String toString() {
		return XStreamUtil.toString(this);
	}

	public static GraniteProperties getDefaultProperties() {
		Gas3 gas3 = new Gas3("uid", DefaultAs3TypeFactory.class.getName(), DefaultEntityFactory.class.getName(), DefaultRemoteDestinationFactory.class.getName());

		gas3.getTemplates().add(new Gas3Template(
			Kind.BEAN,
			StandardTemplateUris.BEAN + ";" + StandardTemplateUris.BEAN_BASE
		));

		gas3.getTemplates().add(new Gas3Template(
			Kind.ENTITY,
			StandardTemplateUris.ENTITY + ";" + StandardTemplateUris.ENTITY_BASE
		));

		gas3.getTemplates().add(new Gas3Template(
			Kind.INTERFACE,
			StandardTemplateUris.INTERFACE
		));

		gas3.getTemplates().add(new Gas3Template(
			Kind.ENUM,
			StandardTemplateUris.ENUM
		));
		
		gas3.getTemplates().add(new Gas3Template(
			Kind.REMOTE_DESTINATION,
			StandardTemplateUris.REMOTE + ";" + StandardTemplateUris.REMOTE_BASE
		));
		
		gas3.getTransformers().add(new Gas3Transformer(JavaAs3GroovyTransformer.class.getName()));
		
		GraniteProperties properties = new GraniteProperties();
		properties.setGas3(gas3);
		return properties;
	}
}
