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

package org.granite.generator.java;

import java.io.File;

import org.granite.generator.Listener;
import org.granite.generator.as3.ClientType;
import org.granite.generator.as3.JavaAs3GroovyConfiguration;
import org.granite.generator.as3.JavaClientGroovyTransformer;
import org.granite.generator.as3.JavaAs3Input;
import org.granite.generator.gsp.GroovyTemplate;

/**
 * @author Franck WOLFF
 */
public class JavaGroovyTransformer extends JavaClientGroovyTransformer {

	private static final String GENERATED_FILE_EXTENSION = "java";
	
	public JavaGroovyTransformer() {
	}

	public JavaGroovyTransformer(JavaAs3GroovyConfiguration config, Listener listener) {
		super(config, listener);
	}

    @Override
	protected File getOutputFile(JavaAs3Input input, GroovyTemplate template, File outputDir) {
    	ClientType clientType = input.getJavaType().getClientType();

        StringBuilder sb = new StringBuilder()
            .append(outputDir.getAbsolutePath())
            .append(File.separatorChar)
            .append(clientType.getQualifiedName().replace('.', File.separatorChar))
            .append(getOutputFileSuffix(input, template))
            .append('.')
            .append(GENERATED_FILE_EXTENSION);

        return new File(sb.toString());
    }
}
