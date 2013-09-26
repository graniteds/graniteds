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

import org.granite.builder.util.StringUtil;
import org.granite.generator.TemplateUri;
import org.granite.generator.as3.reflect.JavaType.Kind;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * @author Franck WOLFF
 */
public class Gas3Template implements Validable {
	
	@XStreamAsAttribute
	private Kind kind;

	@XStreamAsAttribute
	private String uris;
	
	private transient TemplateUri[] templateUris;

	public Gas3Template(Kind kind, String uris) {
		this.kind = kind;
		this.uris = uris;
	}

	public Kind getKind() {
		return kind;
	}
	public void setKind(Kind kind) {
		this.kind = kind;
	}

	public String getUris() {
		return uris;
	}
	public void setUris(String uris) {
		this.uris = uris;
		this.templateUris = null;
	}
	public void setUri(String uri, boolean base) {
		String[] uriArray = StringUtil.split(uris, ';');
		if (!base)
			uriArray[0] = uri;
		else if (uriArray.length > 1)
			uriArray[1] = uri;
		else
			uriArray = new String[]{uriArray[0], uri};
		setUris(StringUtil.join(uriArray, ';'));
	}

	public TemplateUri[] getTemplateUris() {
		if (templateUris == null) {
			if (uris.length() == 0 || uris.charAt(0) == ';')
				templateUris = new TemplateUri[0];
			else {
				String[] uriArray = StringUtil.split(uris, ';');
				templateUris = new TemplateUri[uriArray.length];
				for (int i = uriArray.length - 1; i >= 0; i--)
					templateUris[i] = new TemplateUri(uriArray[i], i > 0);
			}
		}
		return templateUris;
	}
	
	@Override
	public void validate(ValidationResults results) {
		if (kind == null || uris == null)
			results.getErrors().add("templates: kind and uris cannot be null");
	}

	@Override
	public boolean equals(Object o) {
		return (o instanceof Gas3Template && kind.equals(((Gas3Template)o).kind));
	}

	@Override
	public int hashCode() {
		return kind.hashCode();
	}
}
