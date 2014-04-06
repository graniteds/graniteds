/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2014 GRANITE DATA SERVICES S.A.S.
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
package org.granite.generator.as3;

import java.io.File;

import org.granite.generator.Listener;
import org.granite.generator.gsp.GroovyTemplate;

/**
 * @author Franck WOLFF
 */
public class JavaAs3GroovyTransformer extends JavaClientGroovyTransformer {

	private static final String GENERATED_FILE_EXTENSION = "as";
	
	public JavaAs3GroovyTransformer() {
	}

	public JavaAs3GroovyTransformer(JavaAs3GroovyConfiguration config, Listener listener) {
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
